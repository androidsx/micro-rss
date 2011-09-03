package com.androidsx.microrss;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wimm.framework.service.NetworkService;

public class WIMMCompatibleHelper {
    public static final String TAG = "WIMMCompatibleHelper";
    
    /** We will use WIMM API for always ask if the network connection is 
     * alive when true */
    public static final boolean RUN_WITH_SYNC_MANAGER = false;
    
    public static void requestSync(Context context) {
        if (WIMMCompatibleHelper.RUN_WITH_SYNC_MANAGER == true) {
            NetworkService network = new NetworkService(context);
            if ( network.isNetworkAvailable() ) {
                Log.i(TAG, "Network is available, start the update service");
                context.startService(new Intent(context, UpdateService.class)); // if already started, does nothing
            } 
            else {
                // if we arrive here from the force sync button, on the incoming
                // broadcast it will allow the sync even it has not passed more 
                // than the e.g. 6 hours from the last sync.
                Log.i(TAG, "Request the network connection to sync the feeds");
                network.requestNetworkConnection();
            }
        } else {
            Log.i(TAG, "Start the update service (unless already started)");
            context.startService(new Intent(context, UpdateService.class)); // if already started, does nothing
        }
    }
}
