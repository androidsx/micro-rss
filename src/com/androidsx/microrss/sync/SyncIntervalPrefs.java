/*
 * Copyright (C) 2011 WIMM Labs Incorporated
 */

package com.androidsx.microrss.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.androidsx.microrss.UpdateService;

public class SyncIntervalPrefs {
    private static final String SYNC_STATUS = "SYNC_STATUS";
    private static final String LAST_SYNC_ATTEMPT = "LAST_SYNC_ATTEMPT";
    public static final String LAST_SUCCESSFUL_SYNC = "LAST_SUCCESSFUL_SYNC";
    private static final String FORCE_SYNC = "FORCE_SYNC";

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

    public long getLastSyncAttempt() {
        return mPrefs.getLong(LAST_SYNC_ATTEMPT, 0);
    }

    public long getLastSuccessfulSync() {
        return mPrefs.getLong(LAST_SUCCESSFUL_SYNC, 0);
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

    // update sync status
    public void willBeginSync() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SYNC_STATUS, true);
        editor.commit();
    }

    // update sync status
    public void willForceSync(boolean force) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(FORCE_SYNC, force);
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
}
