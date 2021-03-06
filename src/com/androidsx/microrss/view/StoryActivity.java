package com.androidsx.microrss.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.Preferences;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.wimm.Constants;
import com.wimm.framework.view.MotionInterpreter;
import com.wimm.framework.view.MotionInterpreter.ScrollAxis;

public class StoryActivity extends ScrollAwareLauncherActivity {
	
	private static final int HEADER_HEIGHT = 23;

    private static final String TAG = "StoryActivity";

    private CustomAdapterViewTray customViewTrayAdapter;
    private int feedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.RUN_IN_WIMM_DEVICE) {
            setContentView(R.layout.story_wimm_wrapper);
        } else {
            setContentView(R.layout.story_wrapper);
        }

        customViewTrayAdapter = (CustomAdapterViewTray) findViewById(R.id.custom_story_wrapper);
        configureViewTray(customViewTrayAdapter);
        
        feedId = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIdKey(), -1);
        if (feedId != -1) {
            MicroRssDao dao = new MicroRssDao(getContentResolver());
            Feed feed = dao.findFeed(feedId);

            StoryAdapter storyAdapter = new StoryAdapter(this, dao.findStories(feedId)
                    .toArray(new Item[0]), feed);
            setDragable(storyAdapter);
            if (storyAdapter.getCount() > 0) {
                customViewTrayAdapter.setAdapter(storyAdapter);
                int currentStoryId = getIntent().getIntExtra(new StoryNavigationExtras().getCurrentIdKey(), -1);
                int position = storyAdapter.getItemPosition(currentStoryId, 0);
                if (position >= 0) {
                    customViewTrayAdapter.setIndex(position);
                } else {
                    Log.e(TAG, "Wrong story id: " + currentStoryId);
                }
            } else {
                Log.e(TAG, "There are no stories for the feed id " + feedId);
                
                customViewTrayAdapter.setCanScrollInternalView(false);
                
                ErrorMessageAdapter errorAdapter = new ErrorMessageAdapter(this, R.string.error_message_story_no_items,
                        R.string.error_message_story_no_items_detailed,
                        R.drawable.information,
                        getResources().getColor(R.color.error_message_info));
                customViewTrayAdapter.setAdapter(errorAdapter);
                setDragable(errorAdapter);
            }
        } else {
            Log.e(TAG, "Wrong feed id: " + feedId);

            customViewTrayAdapter.setCanScrollInternalView(false);
            
            ErrorMessageAdapter errorAdapter = new ErrorMessageAdapter(this, R.string.error_message_feed_unexpected_id,
                    R.string.error_message_feed_unexpected_id_detailed,
                    R.drawable.warning,
                    getResources().getColor(R.color.error_message_warning));
            customViewTrayAdapter.setAdapter(errorAdapter);
            setDragable(errorAdapter);
        }
    }

    private void configureViewTray(CustomAdapterViewTray adapterViewTray) {
        MotionInterpreter.ScrollAxis scrollAxis = MotionInterpreter.ScrollAxis.LeftRight;
        adapterViewTray.setMotionAxis(scrollAxis);
        adapterViewTray.setCanScrollInternalView(true);
        adapterViewTray.setOnDragEndListener(dragEndListener);
    }
    
    @Deprecated
    public void onStoryClick(View target) {
        try {
            int scrollY = target.findViewById(R.id.story_title).getTop() + HEADER_HEIGHT;
            customViewTrayAdapter.getActiveView().scrollTo(0, scrollY);
        } catch (Exception e) {
            // no need to make any action
            Log.e(TAG, "Can't scroll to title " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void onGoFeedClick(View target) {
    	   Intent intent = IntentHelper.createIntent(StoryActivity.this, null, FeedActivity.class);
           intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
           startActivity(intent);
    }
    
    public void onGoSettingsClick(View target) {
        Intent intent = IntentHelper.createIntent(this, null, Preferences.class);
        startActivity(intent);
    }
    
    /** 
     * Controls the swipe up to go to Feed View. Before going up to the feed level, we clean up
     * the extras that won't make sense any more up there.
     */
    @Deprecated
    private CustomAdapterViewTray.OnDragEndListener dragEndListener = new CustomAdapterViewTray.OnDragEndListener() {

        @Override
        public void onDragEnd(MotionEvent arg0, ScrollAxis arg1, float arg3) {
            Log.v(TAG, "Detected movement " + arg1.toString() + ": index " + customViewTrayAdapter.getIndex());            
//            if (arg1 == ScrollAxis.UpDown && arg2 == Direction.Up
//                    && customViewTrayAdapter.getViewScrollY() == 0) {
//                Intent intent = IntentHelper.createIntent(StoryActivity.this, null, FeedActivity.class);
//                intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
//                startActivity(intent);
//            }
        }
    };

    public void onClickNavigationUp(View target) {
        Intent intent = IntentHelper.createIntent(this, null, FeedActivity.class);
        intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
        startActivity(intent);
    }

    public void onClickNavigationLeft(View target) {
        int currentIndex = customViewTrayAdapter.getIndex();
        if (currentIndex > 0) {
            customViewTrayAdapter.setIndex(currentIndex - 1);
        } else {
            Log.w(TAG, "Can't go left anymore. Already at index " + currentIndex);
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
        // Can't go further down from here
    }
}