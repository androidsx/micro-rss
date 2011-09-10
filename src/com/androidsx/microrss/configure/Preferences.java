package com.androidsx.microrss.configure;

import java.util.Date;

import android.content.Context;
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
import com.androidsx.microrss.view.FeedActivity;
import com.androidsx.microrss.view.SwipeAwareListener;

public class Preferences extends PreferenceActivity {
    private static final String TAG = "Preferences";
    
    private SharedPreferences.OnSharedPreferenceChangeListener lastSyncListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        getListView().setOnTouchListener(swipeListener);
        getListView().setVerticalScrollBarEnabled(false); 
        
        boolean isSyncedWithGoogleReader = PreferenceManager.getDefaultSharedPreferences(
                this).getBoolean(getResources().getString(R.string.pref_synced_with_google_reader),
                false);
        ((Preference) findPreference("chooseGoogleReaderFeeds")).setEnabled(isSyncedWithGoogleReader);

        ((Preference) findPreference("chooseSampleFeeds")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, ChooseSampleFeedsActivity.class));
                Preferences.this
                .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
        });
        
        ((Preference) findPreference("googleReaderAccount")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, GReaderPreferences.class));
                Preferences.this
                .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
        });

        ((Preference) findPreference("chooseGoogleReaderFeeds")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, ChooseGoogleReaderFeedsActivity.class));
                Preferences.this
                .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
        });
        
        ((Preference) findPreference("syncStories")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.i(TAG, "Force the syncronization");
                
                SyncIntervalPrefs syncIntervalPrefs = new SyncIntervalPrefs(Preferences.this);
                if ( syncIntervalPrefs.isSyncing() ) {
                    Log.i(TAG, "We are already syncing");
                    Toast.makeText(Preferences.this, "We are already syncing", Toast.LENGTH_SHORT).show();
                } else {
                    long syncTime = syncIntervalPrefs.getLastSuccessfulSync();
                    if ( syncTime != 0 ) {
                        Time t = new Time();
                        t.set(syncTime);
                        Log.i(TAG, "Last sync success: " + t.format("%H:%M:%S") + " " +  t.format("%m/%d/%Y"));
                    } else {
                        Log.i(TAG, "We have never succeed to sync");
                    }
                    
                    Toast.makeText(Preferences.this, "Force the sync, it may take a while", Toast.LENGTH_SHORT).show();
                }
                
                syncIntervalPrefs.willForceSync(true);
                WIMMCompatibleHelper.requestSync(Preferences.this);
                
                return true;
            }
        });
        
        
        ((Preference) findPreference("syncStoriesMessage")).setTitle(getLastSyncMessage());
        lastSyncListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
                if (key.equals(SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC)
                        || key.equals(SyncIntervalPrefs.SYNC_STATUS)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((Preference) findPreference("syncStoriesMessage"))
                            .setTitle(getLastSyncMessage());
                        }
                    });
                }
            }
        };
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(lastSyncListener);

        // FIXME: Missing: when we change the update interval, do some kind of refresh. For instance, 
        // if it is set to 24 hours and you change it down to 3 minutes, you have to wait another 17 hours
        // to get your 3 minutes! Maybe with WIMM this will radically change
    }
    
    public void onGoBackClick(View target) {
        Intent intent = IntentHelper.createIntent(Preferences.this, null, FeedActivity.class);
        startActivity(intent);
    }
    
    private View.OnTouchListener swipeListener = new SwipeAwareListener() {

        @Override
        public void onTopToBottomSwipe() {
        }

        @Override
        public void onRightToLeftSwipe() {
            Intent intent = IntentHelper.createIntent(Preferences.this, null,
                    FeedActivity.class);
            startActivity(intent);
            Preferences.this
                    .overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
				Preferences.this);
		if (syncIntervalPrefs.isSyncing()) {
			msg = getString(R.string.synchronizing);
		} else {
			long syncTime = syncIntervalPrefs.getLastSuccessfulSync();
			if (syncTime != 0) {
				msg = getString(R.string.last_sync) + " "
						+ AnyRSSHelper.toRelativeDateString(new Date(syncTime));
			} else {
				msg = getString(R.string.never_synced);
			}
		}
		return msg;
	}
    
}
