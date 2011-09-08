package com.androidsx.microrss.view;

import java.util.ArrayList;
import java.util.List;

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
import com.androidsx.microrss.configure.SharedPreferencesHelper;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.DefaultFeed;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.sync.SyncIntervalPrefs;
import com.wimm.framework.app.LauncherActivity;
import com.wimm.framework.view.MotionInterpreter;

/**
 * List of feeds: left-right to change feeds. Scroll up-down to see the titles of the stories of the
 * current feed. 
 */
public class FeedActivity extends LauncherActivity {
    private static final String TAG = "FeedActivity";

    private final OnSharedPreferenceChangeListener firstSyncListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
            if (key.equals(SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC)) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wimm_wrapper);
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
        if (SharedPreferencesHelper.getLongValue(this, SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC) == 0) {
            Log.d(TAG, "A successful sync was never performed: start the service and wait for it to finish");
            buildFirstExecView();
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
    
    private void buildFirstExecView() {
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
        startActivity(intent);
    }
    
    private List<Feed> insertEmptyFeedForSettings(List<Feed> before) {
        List<Feed> after = new ArrayList<Feed>();
        after.add(new DefaultFeed(Feed.SETTINGS_ID, "", "", true, new java.util.Date()));
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
            startActivity(new Intent(this, Preferences.class));
            Log.d(TAG, "Can't go left anymore. Go to Settings");
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
}
