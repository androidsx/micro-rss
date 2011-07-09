package com.androidsx.microrss;

import java.util.LinkedList;
import java.util.Queue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import com.androidsx.microrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.webservice.FeedProcessingException;
import com.androidsx.microrss.webservice.WebserviceHelper;
import com.flurry.android.FlurryAgent;

/**
 * Background service to build any requested widget updates. Uses a single background thread to walk
 * through an update queue, querying {@link WebserviceHelper} as needed to fill database. Also
 * handles scheduling of future updates, usually in 6-hour increments.
 */
public class UpdateService extends Service implements Runnable {
    protected static final String TAG = "UpdateService";

    /**
     * Number of items that we read from the RSS source for every update request.
     * <p>
     * FIXME (WIMM): See {@link DefaultMaxNumItemsSaved}, where a constant and 2 strings also define
     * the maximum...
     */
    public static final int MAX_ITEMS_PER_FEED = 7;

    /**
     * Update interval. Every {@link #UPDATE_INTERVAL} milliseconds, the update service wakes up,
     * checks whether any widget needs updating, sets a new alarm, and sleeps again.
     */
    private static final long UPDATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS;

    /**
     * Specific {@link Intent#setAction(String)} used when performing a full update of all widgets,
     * usually when an update alarm goes off.
     */
    public static final String ACTION_UPDATE_ALL = "com.androidsx.anyrss.UPDATE_ALL";

    /**
     * Lock used when maintaining queue of requested updates.
     */
    private static Object sLock = new Object();

    /**
     * Flag if there is an update thread already running. We only launch a new thread if one isn't
     * already running.
     */
    private static boolean sThreadRunning = false;

    /**
     * Internal queue of requested widget updates. You <b>must</b> access through
     * {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make sure your access is
     * correctly synchronized.
     */
    private static Queue<Integer> sAppWidgetIds = new LinkedList<Integer>();

    /**
     * Request updates for the given widgets. Will only queue them up, you are still responsible for
     * starting a processing thread if needed, usually by starting the parent service.
     */
    private static void requestUpdate(int[] appWidgetIds) {
        Log.i(TAG, "Request update for widgets [" + arr2str(appWidgetIds)
                + "] (just queued them up)");
        synchronized (sLock) {
            for (int appWidgetId : appWidgetIds) {
                sAppWidgetIds.add(appWidgetId);
            }
        }
    }

    /**
     * Peek if we have more updates to perform. This method is special because it assumes you're
     * calling from the update thread, and that you will terminate if no updates remain. (It
     * atomically resets {@link #sThreadRunning} when none remain to prevent race conditions.)
     */
    private static boolean hasMoreUpdates() {
        synchronized (sLock) {
            boolean hasMore = !sAppWidgetIds.isEmpty();
            if (!hasMore) {
                sThreadRunning = false;
            }
            return hasMore;
        }
    }

    /**
     * Poll the next widget update in the queue.
     */
    private static int getNextUpdate() {
        synchronized (sLock) {
            if (sAppWidgetIds.peek() == null) {
                return AppWidgetManager.INVALID_APPWIDGET_ID;
            } else {
                return sAppWidgetIds.poll();
            }
        }
    }

    /**
     * Start this service, creating a background processing thread, if not already running. If
     * started with {@link #ACTION_UPDATE_ALL}, will automatically add all widgets to the requested
     * update queue.
     */
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "update service starting up");
        super.onStart(intent, startId);

        // Only start processing thread if not already running
        // TODO: use an async task
        synchronized (sLock) {
            if (!sThreadRunning) {
                sThreadRunning = true;
                new Thread(this).start();
            }
        }
    }

    /**
     * Main thread for running through any requested widget updates until none remain. Also sets
     * alarm to perform next update.
     * <p>
     * Services in Android run in the main thread of the application, so it is important to create a
     * new thread for an expensive operation such as this one.
     */
    @Override
    public void run() {
        Log.d(TAG, "Processing thread of the update service started");

        requestUpdate(new MicroRssDao(getContentResolver()).findFeedIds());

        boolean areThereHumans = false; // ie, are there widgets that are not zombies?
        while (hasMoreUpdates()) {
            int appWidgetId = getNextUpdate();

            areThereHumans = true; // This widget is not a zombie

            Log.i(TAG, "Let's update widget [" + appWidgetId + "]");

            // Last update is outside throttle window, so update again
            try {
                Log.i(TAG, "Let's ask the WebserviceHelper");

                int maxItemsToStoreInDb = getMaxItemsToStoreInDb(appWidgetId);
                WebserviceHelper.updateForecastsAndFeeds(this, appWidgetId,
                        Math.max(UpdateService.MAX_ITEMS_PER_FEED, maxItemsToStoreInDb),
                        maxItemsToStoreInDb);
            } catch (FeedProcessingException e) {
                Log.e(TAG, "Exception while processing content for the widget " + appWidgetId
                        + ". We'll end up in the error view.", e);
            } catch (Exception e) { // Let's try to avoid an ANR no matter how!
                Log.e(TAG, "Unknown problem. Widget " + appWidgetId
                        + ". We'll end up in the error view.", e);
                FlurryAgent.onError(FlurryConstants.ERROR_ID_UPDATE_SERVICE,
                        "Update service fails unexpectedly", e.getClass().toString());
            }
        } // end of "while there are more updates"

        if (areThereHumans) {
            // Schedule next update alarm
            Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
            updateIntent.setClass(this, this.getClass());
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, updateIntent, 0);

            // Schedule alarm, and force the device awake for this update
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeForNextUpdateAlarm(UPDATE_INTERVAL),
                    pendingIntent);
        } else {
            Log.i(TAG, "There are no more widgets in the home screen, so no need to set an alarm");
        }

        // No updates remaining, so stop service
        stopSelf();
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
    private static String arr2str(int[] appWidgetIds) {
        StringBuilder builder = new StringBuilder();
        for (int element : appWidgetIds) {
            builder.append(element);
            builder.append(", ");
        }
        if (appWidgetIds.length > 0) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        return builder.toString();
    }

    /** Gets the number of items that should be stored in the phone memory. */
    private int getMaxItemsToStoreInDb(int appWidgetId) {
        return new DefaultMaxNumItemsSaved(R.string.conf_default_num_items_saved,
                R.string.max_num_items_saved_prefs_name).getMaxNumItemsSaved(this, appWidgetId);

    }
}
