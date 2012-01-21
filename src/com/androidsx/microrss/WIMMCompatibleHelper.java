package com.androidsx.microrss;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.androidsx.microrss.wimm.Constants;
import com.wimm.framework.service.NetworkService;

public class WIMMCompatibleHelper {
    public static final String TAG = "WIMMCompatibleHelper";
    
    public static void requestSync(Context context) {
        requestSync(context, UpdateService.class);
    }
    
    @SuppressWarnings("unused") // We know there's dead code, because of RUN_WITH_SYNC_MANAGER
    public static void requestSync(Context context, Class<? extends Service> serviceClass) {
        if (Constants.RUN_IN_WIMM_DEVICE == true) {
            NetworkService network = new NetworkService(context);
            if ( network.isNetworkAvailable() ) {
                Log.i(TAG, "Network is available, start the service " + serviceClass.getSimpleName());
                context.startService(new Intent(context, serviceClass)); // if already started, does nothing
            } else {
                // if we arrive here from the force sync button, on the incoming
                // broadcast it will allow the sync even it has not passed more 
                // than the e.g. 6 hours from the last sync.
                Log.i(TAG, "Request the network connection for the service " + serviceClass.getSimpleName());
                network.requestNetworkConnection();
            }
        } else {
            Log.i(TAG, "Start the service " +  serviceClass.getSimpleName() + " (unless already started)");
            context.startService(new Intent(context, serviceClass)); // if already started, does nothing
        }
    }
}
