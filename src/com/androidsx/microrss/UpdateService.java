package com.androidsx.microrss;

import java.util.LinkedList;
import java.util.Queue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.sync.SyncIntervalPrefs;
import com.androidsx.microrss.webservice.FeedProcessingException;
import com.androidsx.microrss.webservice.WebserviceHelper;
import com.androidsx.microrss.wimm.Constants;
import com.wimm.framework.service.NetworkService;

/**
 * Background service to build any requested feed updates. Uses a single background thread to walk
 * through an update queue, querying {@link WebserviceHelper} as needed to fill database. Also
 * handles scheduling of future updates, usually in 6-hour increments.
 */
public class UpdateService extends Service implements Runnable {
    protected static final String TAG = "UpdateService";

    /**
     * Maxium of the number of stories that we read from the RSS source for a feed for every update
     * request. Note that, after several sync operations, we may have more (or less) stories for a
     * given feed stored in the database.
     */
    private static final int MAX_STORIES_PER_REQUEST = 15;

    /**
     * Maximum of the number of stories that we keep in the database for a feed. It must be higher
     * than {@link #STORIES_PER_REQUEST}.
     */
    private static final int MAX_STORIES_IN_DATABASE = 20;

    /**
     * Default update interval, in milliseconds. Every update period, the update service wakes up,
     * checks whether any feed needs updating, sets a new alarm, and sleeps again.
     */
    public static final long DEFAULT_UPDATE_INTERVAL_MILLIS = 6 * DateUtils.HOUR_IN_MILLIS;

    /**
     * Specific {@link Intent#setAction(String)} used when performing a full update of all feeds,
     * usually when an update alarm goes off.
     */
    public static final String ACTION_UPDATE_ALL = "com.androidsx.anyrss.UPDATE_ALL";

    /**
     * Lock used when maintaining queue of requested updates.
     */
    private static Object queueLock = new Object();

    /**
     * Flag if there is an update thread already running. We only launch a new thread if one isn't
     * already running.
     */
    private static boolean threadIsRunning = false;

    /**
     * Internal queue of requested feed updates. You <b>must</b> access through
     * {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make sure your access is
     * correctly synchronized.
     */
    private static Queue<Integer> queuedFeedIds = new LinkedList<Integer>();
    
    /** Used to postpone the network connection if the background tasks has not been finished */
    private NetworkService networkService;
    
    /** Stores sync information */
    private SyncIntervalPrefs syncPrefs;
    
    /**
     * Request updates for the given feeds. Will only queue them up, you are still responsible for
     * starting a processing thread if needed, usually by starting the parent service.
     */
    private static void requestUpdate(int[] feedIds) {
        Log.i(TAG, "Request update for feeds [" + arr2str(feedIds)
                + "] (just queued them up)");
        synchronized (queueLock) {
            for (int feedId : feedIds) {
                queuedFeedIds.add(feedId);
            }
        }
    }

    /**
     * Peek if we have more updates to perform. This method is special because it assumes you're
     * calling from the update thread, and that you will terminate if no updates remain. (It
     * atomically resets {@link #threadIsRunning} when none remain to prevent race conditions.)
     */
    private static boolean hasMoreUpdates() {
        synchronized (queueLock) {
            boolean hasMore = !queuedFeedIds.isEmpty();
            if (!hasMore) {
                threadIsRunning = false;
            }
            return hasMore;
        }
    }

    /**
     * Poll the next feed update in the queue.
     */
    private static int getNextUpdate() {
        synchronized (queueLock) {
            return queuedFeedIds.poll();
        }
    }
    
    @Override
    public void onCreate() {
        Log.d(TAG, "Create the update service");
        super.onCreate();
        
        networkService = new NetworkService(this);
        syncPrefs = new SyncIntervalPrefs(this);
        
        Log.d(TAG, "Registering com.wimm.action.NETWORK_TAKEDOWN for network connection");
        registerReceiver(networkTakedownReceiver, new IntentFilter(NetworkService.ACTION_NETWORK_TAKEDOWN));
    }

    /**
     * Start this service, creating a background processing thread, if not already running. If
     * started with {@link #ACTION_UPDATE_ALL}, will automatically add all feeds to the requested
     * update queue.
     */
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "Update service starting up");
        super.onStart(intent, startId);
        
        // Only start processing thread if not already running
        // TODO: use an async task
        synchronized (queueLock) {
            if (!threadIsRunning) {
                threadIsRunning = true;
                new Thread(this).start();
            }
        }
    }
    
    // unregister for network takedown intents
    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy the update service");
        super.onDestroy();
        
        Log.d(TAG, "Unregistering com.wimm.action.NETWORK_TAKEDOWN, we no longer need the connection");
        unregisterReceiver(networkTakedownReceiver);
    }

    /**
     * Main thread for running through any requested feed updates until none remain. Also sets
     * alarm to perform next update.
     * <p>
     * Services in Android run in the main thread of the application, so it is important to create a
     * new thread for an expensive operation such as this one.
     */
    @Override
    public void run() {
        Log.i(TAG, "Processing thread of the update service started");
        
        boolean success = true;
        try {
            syncPrefs.willBeginSync();
            
            requestUpdate(new MicroRssDao(getContentResolver()).findActiveFeedIds());
    
            boolean areThereHumans = false; // ie, are there feeds that are not zombies?
            while (hasMoreUpdates()) {
                int feedId = getNextUpdate();
    
                areThereHumans = true; // This feed is not a zombie
    
                Log.i(TAG, "Proceed to update feed [" + feedId + "]");
    
                // Last update is outside throttle window, so update again
                try {
                    Log.d(TAG, "Let's ask the WebserviceHelper");
    
                    WebserviceHelper.updateStoriesForFeed(
                            this,
                            feedId,
                            MAX_STORIES_PER_REQUEST,
                            MAX_STORIES_IN_DATABASE);
                    WebserviceHelper.retrieveFaviconFromFeed(this, feedId);
                } catch (FeedProcessingException e) {
                    Log.e(TAG, "Exception while processing content for the feed " + feedId
                            + ".", e);
                    success = false;
                } catch (Exception e) { // Let's try to avoid an ANR no matter how!
                    Log.e(TAG, "Unknown problem. Feed " + feedId
                            + ".", e);
                    //FlurryAgent.onError(FlurryConstants.ERROR_ID_UPDATE_SERVICE,
                    //        "Update service fails unexpectedly", e.getClass().toString());
                    success = false;
                }
            } // end of "while there are more updates"
    
            // schedule alarm only for non-wimm devices
            if (Constants.RUN_IN_WIMM_DEVICE == false) {
                scheduleNextSync(areThereHumans);
            }
        
        } finally {
            syncPrefs.didCompleteSync(success);
        }
        
        // No updates remaining, so stop service
        Log.i(TAG, "Stop the service, we have finished the sync: " + success);
        stopSelf();
    }

    private void scheduleNextSync(boolean areThereHumans) {
        if (areThereHumans) {
            // Schedule next update alarm
            Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
            updateIntent.setClass(this, this.getClass());
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);
   
            // Schedule alarm, and force the device awake for this update
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeForNextUpdateAlarm(getUpdateIntervalMillis()),
                    pendingIntent);
        } else {
            Log.i(TAG, "There are no more feeds in the home screen, so no need to set an alarm");
        }
    }

    /** Fetches the update interval from the shared preferences, or returns a reasonable default value */
    private long getUpdateIntervalMillis() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String updateIntervalKey = getResources().getString(R.string.pref_update_interval);
        String updateInterval = sharedPrefs.getString(updateIntervalKey, String.valueOf(DEFAULT_UPDATE_INTERVAL_MILLIS));
        Log.e(TAG, "The update interval is " + updateInterval);
        return new Long(updateInterval);
    }
    
    private long timeForNextUpdateAlarm(long minUpdateIntervalMillis) {
        Time time = new Time();
        time.set(System.currentTimeMillis() + minUpdateIntervalMillis);
        long nextUpdate = time.toMillis(false);
        long nowMillis = System.currentTimeMillis();

        // Throttle our updates just in case the math went funky
        if (nextUpdate - nowMillis < computeUpdateThrottle(minUpdateIntervalMillis)) {
            Log.d(TAG, "Calculated next update too early, throttling for a few minutes");
            nextUpdate = nowMillis + computeUpdateThrottle(minUpdateIntervalMillis);
        }

        Log.i(TAG, "Requesting next update in " + (nextUpdate - nowMillis)
                / DateUtils.MINUTE_IN_MILLIS + " minutes");

        return nextUpdate;
    }

    /**
     * Computes the throttle: if we calculated an update too quickly in the future, wait this
     * interval and try rescheduling.
     */
    private static long computeUpdateThrottle(long updateIntervalMillis) {
        return updateIntervalMillis / 12;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Represents array like {@code [1, 2, 3]}.
     */
    private static String arr2str(int[] feedIds) {
        StringBuilder builder = new StringBuilder();
        for (int element : feedIds) {
            builder.append(element);
            builder.append(", ");
        }
        if (feedIds.length > 0) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        return builder.toString();
    }

    private BroadcastReceiver networkTakedownReceiver = new BroadcastReceiver() {

        private static final String TAG = "NetworkTakedownReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received com.wimm.action.NETWORK_TAKEDOWN broadcast");

            if (threadIsRunning) {
                networkService.postponeNetworkTakedown();
                Log.i(TAG, "Postpone network takedown, we are still doing network operations syncing the stories");
            }
        }
    };
}
