/*
 * Copyright (C) 2011 WIMM Labs Incorporated
 */

package com.androidsx.microrss.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.androidsx.microrss.UpdateService;

public class SyncIntervalPrefs {
    public static final String SYNC_STATUS = "SYNC_STATUS";
    public static final String LAST_SYNC_ATTEMPT = "LAST_SYNC_ATTEMPT";
    public static final String LAST_SUCCESSFUL_SYNC = "LAST_SUCCESSFUL_SYNC";
    private static final String FORCE_SYNC = "FORCE_SYNC";

    public static final String SYNC_STATUS_GREADER = "SYNC_STATUS_GREADER";
    private static final String LAST_SYNC_ATTEMPT_GREADER = "LAST_SYNC_ATTEMPT_GREADER";
    public static final String LAST_SUCCESSFUL_SYNC_GREADER = "LAST_SUCCESSFUL_SYNC_GREADER";
    private static final String FORCE_SYNC_GREADER = "FORCE_SYNC_GREADER";
    private static final String ERROR_MESSAGE_GREADER = "ERROR_MESSAGE_GREADER";

    private static final long MIN_SYNC_INTERVAL = UpdateService.DEFAULT_UPDATE_INTERVAL_MILLIS; //60 * 1000; // ms 

    private final SharedPreferences mPrefs;

    // constructor
    public SyncIntervalPrefs(Context context) {
        mPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return mPrefs;
    }

    public boolean isSyncing() {
        return mPrefs.getBoolean(SYNC_STATUS, false);
    }

    public boolean isSyncingGoogleReader() {
        return mPrefs.getBoolean(SYNC_STATUS_GREADER, false);
    }

    public long getLastSyncAttempt() {
        return mPrefs.getLong(LAST_SYNC_ATTEMPT, 0);
    }

    public long getLastSyncAttemptGoogleReader() {
    	return mPrefs.getLong(LAST_SYNC_ATTEMPT_GREADER, 0);
    }

    public long getLastSuccessfulSync() {
        return mPrefs.getLong(LAST_SUCCESSFUL_SYNC, 0);
    }

    public long getLastSuccessfulSyncGoogleReader() {
        return mPrefs.getLong(LAST_SUCCESSFUL_SYNC_GREADER, 0);
    }

    // checks if our data need to be refreshed
    public boolean shouldSync() {

        long now = System.currentTimeMillis();
        long lastSync = mPrefs.getLong(LAST_SUCCESSFUL_SYNC, 0);
        boolean forceSync = mPrefs.getBoolean(FORCE_SYNC, false);
        
        // we have never sync'd
        if (lastSync == 0)
            return true;
        
        if (forceSync) {
            // we will update the flag when consumed the sync
            return true;
        }

        // we are too close to the last sync
        if ((now - lastSync) < MIN_SYNC_INTERVAL)
            return false;

        return true;
    }
    
    public boolean shouldSyncGoogleReader() {
        boolean forceSync = mPrefs.getBoolean(FORCE_SYNC_GREADER, false);
        if (forceSync) {
            // we will update the flag when consumed the sync
            return true;
        }
        return false;
    }

    // update sync status
    public void willBeginSync() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SYNC_STATUS, true);
        editor.commit();
    }

    // update sync status
    public void willBeginSyncGoogleReader() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SYNC_STATUS_GREADER, true);
        editor.commit();
    }

    // update sync status
    public void willForceSync(boolean force) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(FORCE_SYNC, force);
        editor.commit();
    }

    // update sync status
    public void willForceSyncGoogleReader(boolean force) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(FORCE_SYNC_GREADER, force);
        editor.commit();
    }

    // update sync dates
    public void didCompleteSync(boolean success) {
        long now = System.currentTimeMillis();
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putBoolean(SYNC_STATUS, false);
        editor.putLong(LAST_SYNC_ATTEMPT, now);
        if (success)
            editor.putLong(LAST_SUCCESSFUL_SYNC, now);
        
        // consume the force sync
        editor.putBoolean(FORCE_SYNC, false);

        editor.commit();
    }

    // update sync dates
    public void didCompleteSyncGoogleReader(boolean success, String reasonError) {
        long now = System.currentTimeMillis();
        SharedPreferences.Editor editor = mPrefs.edit();
        
        editor.putBoolean(SYNC_STATUS_GREADER, false);
        editor.putLong(LAST_SYNC_ATTEMPT_GREADER, now);
        if (success) {
            editor.putLong(LAST_SUCCESSFUL_SYNC_GREADER, now);
            editor.putString(ERROR_MESSAGE_GREADER, "");
        } else {
            editor.putString(ERROR_MESSAGE_GREADER, reasonError);
        }
        
        // consume the force sync
        editor.putBoolean(FORCE_SYNC_GREADER, false);
        
        editor.commit();
    }

    /** In case of failure sync, this will be not an empty string */
    public String getErrorMessageGoogleReader() {
        return mPrefs.getString(ERROR_MESSAGE_GREADER, "");
    }
}
