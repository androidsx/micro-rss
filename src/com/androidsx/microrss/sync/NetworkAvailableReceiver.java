package com.androidsx.microrss.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.androidsx.microrss.UpdateService;
import com.wimm.framework.service.NetworkService;

public class NetworkAvailableReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkAvailableReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        // We have received notification that the network is available.
        if (NetworkService.ACTION_NETWORK_AVAILABLE.equals(action)) {
            Log.i(TAG, "Received com.wimm.action.NETWORK_AVAILABLE broadcast");

            // If we need to update our data, start our sync service.
            SyncIntervalPrefs prefs = new SyncIntervalPrefs(context);
            if (prefs.shouldSync()) {
                Log.i(TAG, "We should sync, start the update service");
                context.startService(new Intent(context, UpdateService.class));
            } else {
                Log.i(TAG, "We should not sync now, maybe later");
            }
        }
    }
}