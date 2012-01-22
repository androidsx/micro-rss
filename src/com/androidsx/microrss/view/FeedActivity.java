package com.androidsx.microrss.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.WIMMCompatibleHelper;
import com.androidsx.microrss.configure.Preferences;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.DefaultFeed;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.sync.SyncIntervalPrefs;
import com.androidsx.microrss.wimm.Constants;
import com.wimm.framework.app.LauncherActivity;
import com.wimm.framework.view.MotionInterpreter;
import com.wimm.framework.view.ViewTray.OnIndexChangeListener;

/**
 * List of feeds: left-right to change feeds. Scroll up-down to see the titles of the stories of the
 * current feed. 
 */
public class FeedActivity extends LauncherActivity {
	private static final String TAG = "FeedActivity";
	private static final String PREFS_NAME = "com.androidsx.microrss";
	
    private final OnSharedPreferenceChangeListener firstSyncListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        	// update only when there has been a sync OR
        	// the sync has started/finished ONLY on the initial sync, to not
        	// update while you are looking feeds.
            if (key.equals(SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC) ||
            		key.equals(SyncIntervalPrefs.LAST_SYNC_ATTEMPT) ||
            		(key.equals(SyncIntervalPrefs.SYNC_STATUS) &&
            				prefs.getLong(SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC, 0) == 0)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Update the views after a successful synchronization operation");
                        configureViewTray((CustomAdapterViewTray) findViewById(R.id.custom_feed_wrapper));
                        buildView();
                        findViewById(R.id.custom_feed_wrapper).invalidate();
                        
                        // We don't have to listen to this event any more
                        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
                            .unregisterOnSharedPreferenceChangeListener(firstSyncListener);
                    }
                });
            }
        }
    };

    private CustomAdapterViewTray customViewTrayAdapter;
    private static final String FIRST_TIME_APP_OPENED_PREFS = "FIRST_TIME_APP_OPENED";
    private static final int REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.RUN_IN_WIMM_DEVICE) {
            setContentView(R.layout.feed_wimm_wrapper);
        } else {
            setContentView(R.layout.feed_wrapper);
        }
        
        // check first execution, to ask WIMM syncronization
        int isFirstTimeUserOpenApp = getIntValue(this, FIRST_TIME_APP_OPENED_PREFS);
        if (isFirstTimeUserOpenApp == 0) {
        	WIMMCompatibleHelper.requestSync(this);
        	saveIntValue(this, FIRST_TIME_APP_OPENED_PREFS, 1);
        }
        
        configureViewTray((CustomAdapterViewTray) findViewById(R.id.custom_feed_wrapper));
        buildView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        buildView();
    }
    
    private void buildView() {
    	SyncIntervalPrefs syncPrefs = new SyncIntervalPrefs(this);
        if (syncPrefs.getLastSyncAttempt() == 0) {
            if (syncPrefs.isSyncing()) {
            	Log.d(TAG, "A successful sync was never performed and we are syncing in the background");
            	buildFirstExecIsSyncingView();
            } else {
            	Log.d(TAG, "A successful sync was never performed and we are not syncing (yet) in the background");
            	buildFirstExecNoSyncView();
            }
        } else {
            MicroRssDao dao = new MicroRssDao(getContentResolver());
            List<Feed> feedList = dao.findActiveFeeds();
            if (feedList.size() == 0) {
                Log.e(TAG, "There are no active feeds");
                buildNoFeedsView();
            } else {
                buildNormalView(feedList);
            }
        }
    }
    
    private void buildFirstExecIsSyncingView() {
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(firstSyncListener);
        ErrorScreenAdapter errorAdapter = new ErrorScreenAdapter(this, R.string.error_message_first_update,
                R.string.error_message_first_update_detailed,
                R.drawable.information,
                getResources().getColor(R.color.error_message_info),
                true);
        customViewTrayAdapter.setAdapter(errorAdapter);
        customViewTrayAdapter.setIndex(1); // The index 0 is the settings, and 1 is the actual error message
    }

    private void buildFirstExecNoSyncView() {
    	getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
    	.registerOnSharedPreferenceChangeListener(firstSyncListener);
    	ErrorScreenAdapter errorAdapter = new ErrorScreenAdapter(this, R.string.error_message_first_update_no_sync,
    			R.string.error_message_first_update_no_sync_detailed,
    			R.drawable.information,
    			getResources().getColor(R.color.error_message_info),
    			false);
    	customViewTrayAdapter.setAdapter(errorAdapter);
    	customViewTrayAdapter.setIndex(1); // The index 0 is the settings, and 1 is the actual error message
    }
    
    private void buildNoFeedsView() {
        ErrorScreenAdapter errorAdapter = new ErrorScreenAdapter(this,
                R.string.error_message_feed_no_active,
                R.string.error_message_feed_no_active_detailed, R.drawable.information,
                getResources().getColor(R.color.error_message_info),
                false);
        customViewTrayAdapter.setAdapter(errorAdapter);
        customViewTrayAdapter.setIndex(1); // The index 0 is the settings, and 1 is the actual error message
    }
    
    private void buildNormalView(List<Feed> feedList) {
        List<Feed> feedsWithSettings = insertEmptyFeedForSettings(feedList);
        FeedAdapter feedAdapter = new FeedAdapter(this, feedsWithSettings.toArray(new Feed[0]));
        int currentFeedId = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIdKey(), -1);
        int position = feedAdapter.getItemPosition(currentFeedId, 1); // Default position is 1, due to the settings
        if (position >= 0) {
            customViewTrayAdapter.setAdapter(feedAdapter);
            customViewTrayAdapter.setIndex(position);
        } else {
            Log.e(TAG, "Wrong feed id: " + currentFeedId);
            // FIXME: Why don't we just log it and move to the first position?
            ErrorMessageAdapter errorAdapter = new ErrorMessageAdapter(this, R.string.error_message_feed_unexpected_id,
                    R.string.error_message_feed_unexpected_id_detailed,
                    R.drawable.warning, getResources().getColor(R.color.error_message_warning));
            customViewTrayAdapter.setAdapter(errorAdapter);
        }
        
        // See ticket #309
        customViewTrayAdapter.setOnIndexChangeListener(new OnIndexChangeListener() {
            
            @Override
            public void onIndexWillChange(int from, int to) {
                if (to == 0) {
                    onGoSettingsClick(null);
                }
            }
            
            @Override
            public void onIndexDidChange(int to) {
            }
        });
    }
    
    @Override
    public boolean dragCanExit() {
        return customViewTrayAdapter.getActiveView().getScrollY() == 0 ? super.dragCanExit() : false;
    }

    private void configureViewTray(CustomAdapterViewTray adapterViewTray) {
        customViewTrayAdapter = adapterViewTray;
        MotionInterpreter.ScrollAxis scrollAxis = MotionInterpreter.ScrollAxis.LeftRight;
        customViewTrayAdapter.setMotionAxis(scrollAxis);
        customViewTrayAdapter.setCanScrollInternalView(true);
        customViewTrayAdapter.setCanLoop(false);
    }

    @Deprecated
    public void onFeedClick(View target) {
        Intent intent = IntentHelper.createIntent(this, null, StoryActivity.class);

        int feedId = (int) customViewTrayAdapter.getAdapter().getItemId(customViewTrayAdapter.getIndex());
        intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
        startActivity(intent);
    }
    
    public void onGoSettingsClick(View target) {
        Intent intent = IntentHelper.createIntent(this, null, Preferences.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // See ticket #309
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    private List<Feed> insertEmptyFeedForSettings(List<Feed> before) {
        List<Feed> after = new ArrayList<Feed>();
        after.add(new DefaultFeed(Feed.SETTINGS_ID, "", "", true, new java.util.Date(), ""));
        after.addAll(before);
        return after;
    }

    public void onClickNavigationUp(View target) {
        Toast.makeText(this, "Terminate application!", Toast.LENGTH_LONG).show();
    }

    public void onClickNavigationLeft(View target) {
        int currentIndex = customViewTrayAdapter.getIndex();
        if (currentIndex > 0) {
            customViewTrayAdapter.setIndex(currentIndex - 1);
        } else {
            onGoSettingsClick(target);
        }
    }

    public void onClickNavigationRight(View target) {
        int currentIndex = customViewTrayAdapter.getIndex();
        if (currentIndex < customViewTrayAdapter.getAdapter().getCount() - 1) {
            customViewTrayAdapter.setIndex(currentIndex + 1);
        } else {
            Log.w(TAG, "Can't go right anymore. Already at index " + currentIndex);
        }
    }

    public void onClickNavigationDown(View target) {
        if (customViewTrayAdapter.getAdapter() != null
            && customViewTrayAdapter.getAdapter().getCount() > 0
            && !(customViewTrayAdapter.getAdapter() instanceof ErrorMessageAdapter)) {
            onFeedClick(null);
        }
    }
    
    private void saveIntValue(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static int getIntValue(Context context, String key) {
        SharedPreferences config = context.getSharedPreferences(PREFS_NAME, 0);
        return config.getInt(key, 0);
    }
    
    /** Currently not used in a proper way from Preferences. See #306. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case REQUEST_CODE:
            if (resultCode == Preferences.ACTIVITY_RESULT_EXIT) {
                Log.e(TAG, "Coming from Preferences. The user performed a drag-to-exit move.");
                doFinish();
            } else if (resultCode == Activity.RESULT_OK) {
                Log.e(TAG, "Coming from Preferences. The user swiped right");
            }
            break;
        }
    }
}
