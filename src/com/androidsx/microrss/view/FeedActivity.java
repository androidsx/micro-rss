package com.androidsx.microrss.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.Preferences;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.wimm.framework.app.LauncherActivity;
import com.wimm.framework.view.MotionInterpreter;
import com.wimm.framework.view.MotionInterpreter.ScrollAxis;

/**
 * List of feeds: left-right to change feeds. Scroll up-down to see the titles of the stories of the
 * current feed. 
 */
public class FeedActivity extends LauncherActivity {
    private static final String TAG = "FeedActivity";

    private CustomAdapterViewTray customViewTrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);
        configureViewTray((CustomAdapterViewTray) findViewById(R.id.custom_feed_wrapper));

        MicroRssDao dao = new MicroRssDao(getContentResolver());
        Feed[] activeFeeds = dao.findActiveFeeds().toArray(new Feed[0]);
        
        if (activeFeeds.length > 0) {
            FeedAdapter feedAdapter = new FeedAdapter(this, activeFeeds);
            int currentFeedId = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIdKey(), -1);
            int position = feedAdapter.getItemPosition(currentFeedId, 0);
            if (position >= 0) {
                customViewTrayAdapter.setAdapter(feedAdapter);
                customViewTrayAdapter.setIndex(position);
            } else {
                Log.e(TAG, "Wrong feed id: " + currentFeedId);
                // FIXME: Why don't we just log it and move to the first position?
                ErrorMessageAdapter errorAdapter = new ErrorMessageAdapter(this, R.string.error_message_feed_unexpected_id,
                        R.string.error_message_feed_unexpected_id_detailed,
                        R.drawable.warning, R.color.error_message_warning);
                customViewTrayAdapter.setAdapter(errorAdapter);
            }
        } else {
            Log.e(TAG, "There are no active feeds");

            ErrorMessageAdapter errorAdapter = new ErrorMessageAdapter(this,
                    R.string.error_message_feed_no_active,
                    R.string.error_message_feed_no_active_detailed, R.drawable.information,
                    R.color.error_message_info);
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
        customViewTrayAdapter.setOnDragEndListener(dragEndListener);
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

    /** 
     * Controls the swipe down to go to Story View, and the swipe left on the first feed to go to Settings.
     */
    private CustomAdapterViewTray.OnDragEndListener dragEndListener = new CustomAdapterViewTray.OnDragEndListener() {

        @Override
        public void onDragEnd(MotionEvent arg0, ScrollAxis arg1, float arg3) {
            Log.v(TAG, "Detected movement " + arg1.toString() + ": index " + customViewTrayAdapter.getIndex());
//            if (arg1 == ScrollAxis.LeftRight && arg2 == Direction.Left
//                    && customViewTrayAdapter.getIndex() == 0) {
//                startActivity(new Intent(FeedActivity.this, Preferences.class));
//                Log.d(TAG, "Can't go left anymore. Go to Settings");
//            } else if (arg1 == ScrollAxis.UpDown && arg2 == Direction.Down
//                    && customViewTrayAdapter.getAdapter() != null
//                    && customViewTrayAdapter.getAdapter().getCount() > 0
//                    && !(customViewTrayAdapter.getAdapter() instanceof ErrorMessageAdapter)) {
//                onFeedClick(null);
//            }
        }
    };

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
