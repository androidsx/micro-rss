/*
 * Copyright (C) 2011 WIMM Labs Incorporated
 */
 
 package com.androidsx.microrss;

import java.io.IOException;

import org.jarx.android.reader.GoogleReaderClient;
import org.jarx.android.reader.ReaderClient;
import org.jarx.android.reader.ReaderException;
import org.jarx.android.reader.Subscription;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.sync.SyncIntervalPrefs;
import com.wimm.framework.service.NetworkService;

public class GoogleReaderSyncService extends Service {
	private static final String TAG = GoogleReaderSyncService.class.getSimpleName();
	
	// stores sync information
    private SyncIntervalPrefs syncPrefs;
    
    // communicates with the network service
    private NetworkService networkService;
    
    // tracks network usage between work thread and main thread
    private volatile boolean isUsingNetwork = false;

	// register for network takedown intents
    @Override
    public void onCreate() {
    	Log.d(TAG, "Create the Google Reader service");
        super.onCreate();
        
        syncPrefs = new SyncIntervalPrefs(this);
        networkService = new NetworkService(this);
        
		registerReceiver(networkTakedownReceiver, new IntentFilter(NetworkService.ACTION_NETWORK_TAKEDOWN));
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "Google Reader service starting up");
        super.onStart(intent, startId);
        
        new SyncGoogleReaderTask().execute(null);
    }

	// unregister for network takedown intents
    @Override
    public void onDestroy() {
    	Log.d(TAG, "onDestroy Google Reader service");
    	super.onDestroy();
		unregisterReceiver(networkTakedownReceiver);
    }
    
    // unused
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    

    // handle network takedown requests
    private final BroadcastReceiver networkTakedownReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received com.wimm.action.NETWORK_TAKEDOWN broadcast");

            if (isUsingNetwork) {
                networkService.postponeNetworkTakedown();
                Log.i(TAG, "Postpone network takedown, we are still doing network operations syncing Google Reader");
            }
	    }
    };

    public class SyncGoogleReaderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "Starting syncronization of your Google Reader account");
            isUsingNetwork = true;
            
            syncPrefs.willBeginSyncGoogleReader();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            try {
                GoogleReaderClient gReader = new GoogleReaderClient(getApplicationContext());

                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(GoogleReaderSyncService.this);
                String user = sharedPrefs.getString(getResources().getString(
                        R.string.pref_google_user_name), "");
                String password = sharedPrefs.getString(getResources().getString(
                        R.string.pref_google_password), "");

                if (user.equals("") || password.equals("")) {
                    return "Input your credentials";
                }

                gReader.login(user, password);

                gReader.handleSubList(new ReaderClient.SubListHandler() {
                    @Override
                    public boolean subscription(Subscription sub) {
                        if (sub.getUid().startsWith("feed/")) {
                            String url = sub.getUid().replaceFirst("feed/", "");
                            String title = sub.getTitle();
                            MicroRssDao dao = new MicroRssDao(getContentResolver());
                            dao.persistFeedCheckingUniqueKey(GoogleReaderSyncService.this,
                                    "Google Reader", title, url, false, true);
                        }
                        return true;
                    }
                }, System.currentTimeMillis());

                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean(
                        getResources().getString(R.string.pref_synced_with_google_reader), true);
                editor.commit();
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
                result = "Can't connect: login error";
            } catch (ReaderException e) {
                /*
                 * // TODO: This gets us a very nasty inflater exception. A bug in wimm? Dialog
                 * dialog = new Dialog(Preferences.this);
                 * dialog.setTitle("Can't connect: log-in error"); dialog.show();
                 */
                Log.e(TAG, "ReaderException: " + e);
                result = "Can't connect: login error";
            } catch (Exception e) {
                /*
                 * // TODO: This gets us a very nasty inflater exception. A bug in wimm? Dialog
                 * dialog = new Dialog(Preferences.this);
                 * dialog.setTitle("Can't connect: log-in error"); dialog.show();
                 */
                Log.e(TAG, "Exception: " + e);
                result = "Can't connect: unexpected error";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("")) {
                syncPrefs.didCompleteSyncGoogleReader(true, "");
                Log.i(TAG, "Completed succesfully google reader synchronization");
            } else {
                syncPrefs.didCompleteSyncGoogleReader(false, result);
                Log.w(TAG, result);
            }
            isUsingNetwork = false;

            // No updates remaining, so stop service
            Log.i(TAG, "Stop Google Reader service, we have finished the sync");
            stopSelf();
        }
    }

}
