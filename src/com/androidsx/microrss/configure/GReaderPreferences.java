package com.androidsx.microrss.configure;

import java.util.Date;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.WIMMCompatibleHelper;
import com.androidsx.microrss.sync.SyncIntervalPrefs;
import com.androidsx.microrss.view.AnyRSSHelper;
import com.androidsx.microrss.view.SwipeAwareListener;
import com.wimm.framework.app.TextInputDialog;

public class GReaderPreferences extends PreferenceActivity {
    private static final String TAG = "GReaderPreferences";

    /** FIXME: workaround: it will be true if the TextDialog has been cancelled */
    private boolean dialogHasBeenCancelled = false;

    private SharedPreferences.OnSharedPreferenceChangeListener lastSyncListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_greader);
        
        getListView().setOnTouchListener(swipeListener);
        getListView().setVerticalScrollBarEnabled(false); 
        
        enableSyncFeedsWhenCredentialsOK(); 

        ((Preference) findPreference("syncGoogleReaderFeeds"))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(GReaderPreferences.this);
                        String user = sharedPrefs.getString(getResources().getString(
                                R.string.pref_google_user_name), "");
                        String password = sharedPrefs.getString(getResources().getString(
                                R.string.pref_google_password), "");
        
                        if (user.equals("") || password.equals("")) {
                            Toast.makeText(GReaderPreferences.this, "Input your credentials", Toast.LENGTH_SHORT);
                        } else {
                            Toast.makeText(GReaderPreferences.this, "Input your credentials", Toast.LENGTH_SHORT);
                        
                            Log.i(TAG, "Request to sync Google Reader");
                            
                            SyncIntervalPrefs syncIntervalPrefs = new SyncIntervalPrefs(GReaderPreferences.this);
                            if ( syncIntervalPrefs.isSyncingGoogleReader() ) {
                                Log.i(TAG, "We are already syncing google reader");
                                Toast.makeText(GReaderPreferences.this, "We are already syncing", Toast.LENGTH_SHORT).show();
                            } else {
                                long syncTime = syncIntervalPrefs.getLastSuccessfulSyncGoogleReader();
                                if ( syncTime != 0 ) {
                                    Time t = new Time();
                                    t.set(syncTime);
                                    Log.i(TAG, "Last google reader sync success: " + t.format("%H:%M:%S") + " " +  t.format("%m/%d/%Y"));
                                } else {
                                    Log.i(TAG, "We have never succeed to sync google reader");
                                }
                                
                                Toast.makeText(GReaderPreferences.this, "Synchronizing, it may take a while", Toast.LENGTH_SHORT).show();
                            }
                            
                            syncIntervalPrefs.willForceSyncGoogleReader(true);
                            WIMMCompatibleHelper.requestSyncGoogleReader(GReaderPreferences.this);
                        }

                        return true;
                    }
                });

        // FIXME: workaround to get the event of an input text
        ((Preference) findPreference(getResources().getString(R.string.pref_google_user_name)))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final SharedPreferences sharedPrefs = PreferenceManager
                                .getDefaultSharedPreferences(GReaderPreferences.this);
                        final String storedUsername = sharedPrefs.getString(getResources()
                                .getString(R.string.pref_google_user_name), "");

                        final TextInputDialog textInput = new TextInputDialog(
                                GReaderPreferences.this);
                        textInput.setTitle("Username");
                        textInput.setText(storedUsername);
                        textInput.setOnDismissListener(new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (dialogHasBeenCancelled == false) {
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(getResources().getString(
                                            R.string.pref_google_user_name), textInput.getText());
                                    editor.commit();
                                } else {
                                    dialogHasBeenCancelled = false;
                                }
                                enableSyncFeedsWhenCredentialsOK();
                            }
                        });
                        textInput.setOnCancelListener(new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialogHasBeenCancelled = true;
                            }
                        });

                        textInput.show();

                        return true;
                    }
                });

        // FIXME: workaround to get the event of an input text
        ((Preference) findPreference(getResources().getString(R.string.pref_google_password)))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final TextInputDialog textInput = new TextInputDialog(
                                GReaderPreferences.this);
                        textInput.setTitle("Password");
                        textInput.setOnDismissListener(new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (dialogHasBeenCancelled == false) {
                                    final SharedPreferences sharedPrefs = PreferenceManager
                                            .getDefaultSharedPreferences(GReaderPreferences.this);
                                    SharedPreferences.Editor editor = sharedPrefs.edit();
                                    editor.putString(getResources().getString(
                                            R.string.pref_google_password), textInput.getText());
                                    editor.commit();
                                } else {
                                    dialogHasBeenCancelled = false;
                                }
                                enableSyncFeedsWhenCredentialsOK();
                            }
                        });
                        textInput.setOnCancelListener(new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialogHasBeenCancelled = true;
                            }
                        });

                        textInput.show();

                        return true;
                    }
                });

        ((Preference) findPreference("syncGoogleReaderMessage")).setTitle(getLastSyncMessage());
        lastSyncListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
                if (key.equals(SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC_GREADER)
                        || key.equals(SyncIntervalPrefs.SYNC_STATUS_GREADER)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((Preference) findPreference("syncGoogleReaderMessage"))
                            .setTitle(getLastSyncMessage());
                        }
                    });
                }
            }
        };
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(lastSyncListener);

    }

    private void enableSyncFeedsWhenCredentialsOK() {
        String username = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getResources().getString(R.string.pref_google_user_name), "");
        String password = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getResources().getString(R.string.pref_google_password), "");
        ((Preference) findPreference("syncGoogleReaderFeeds")).setEnabled(!username.equals("")
                && !password.equals(""));
    }

    public void onGoBackClick(View target) {
        Intent intent = IntentHelper.createIntent(GReaderPreferences.this, null, Preferences.class);
        startActivity(intent);
    }
    
    private View.OnTouchListener swipeListener = new SwipeAwareListener() {

        @Override
        public void onTopToBottomSwipe() {
        }

        @Override
        public void onRightToLeftSwipe() {
            Intent intent = IntentHelper.createIntent(GReaderPreferences.this, null,
                    Preferences.class);
            startActivity(intent);
            GReaderPreferences.this.overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        }

        @Override
        public void onLeftToRightSwipe() {
        }

        @Override
        public void onBottomToTopSwipe() {
        }
    };
    
    private String getLastSyncMessage() {
        String msg = "";
        SyncIntervalPrefs syncIntervalPrefs = new SyncIntervalPrefs(
                GReaderPreferences.this);
        if (syncIntervalPrefs.isSyncingGoogleReader()) {
            msg = getString(R.string.synchronizing);
        } else {
            String errorMessage = syncIntervalPrefs.getErrorMessageGoogleReader();
            if (!errorMessage.equals("")) {
                msg = errorMessage;                
            } else {
                long syncTime = syncIntervalPrefs.getLastSuccessfulSyncGoogleReader();
                if (syncTime != 0) {
                    msg = getString(R.string.last_sync) + " "
                            + AnyRSSHelper.toRelativeDateString(new Date(syncTime));
                } else {
                    msg = getString(R.string.never_synced);
                }
            }
        }
        return msg;
    }
}
