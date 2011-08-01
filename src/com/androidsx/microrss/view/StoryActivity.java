package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;
import com.wimm.framework.view.MotionInterpreter;
import com.wimm.framework.view.MotionInterpreter.Direction;
import com.wimm.framework.view.MotionInterpreter.ScrollAxis;

public class StoryActivity extends Activity {
    private static final String TAG = "StoryActivity";

    private CustomAdapterViewTray customViewTrayAdapter;
    private int feedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_wrapper);

        configureViewTray((CustomAdapterViewTray) findViewById(R.id.custom_story_wrapper));

        feedId = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIdKey(), -1);
        if (feedId != -1) {
            MicroRssDao dao = new MicroRssDao(getContentResolver());
            Feed feed = dao.findFeed(feedId);

            StoryAdapter storyAdapter = new StoryAdapter(this, (Item[]) dao.findStories(feedId)
                    .toArray(new Item[0]), feed);
            if (storyAdapter.getCount() > 0) {
                customViewTrayAdapter.setAdapter(storyAdapter);
            } else {
                Log.e(TAG, "There are no stories for the feed id " + feedId);
                Toast.makeText(this, "There are no stories for the feed id " + feedId, Toast.LENGTH_SHORT).show();
                finish(); // TODO: error message or new activity but with sliders to go to settings.
            }
        } else {
            Log.e(TAG, "Wrong feed id: " + feedId);
            Toast.makeText(this, "Wrong feed id: " + feedId, Toast.LENGTH_SHORT).show();
            finish(); // TODO: error message or new activity but with sliders to go to settings.
        }
    }

    private void configureViewTray(CustomAdapterViewTray adapterViewTray) {
        customViewTrayAdapter = adapterViewTray;
        MotionInterpreter.ScrollAxis scrollAxis = MotionInterpreter.ScrollAxis.LeftRight;
        customViewTrayAdapter.setMotionAxis(scrollAxis);
        customViewTrayAdapter.setCanScrollInternalView(true);
        customViewTrayAdapter.setOnDragEndListener(dragEndListener);
    }
    
    /** 
     * Controls the swipe up to go to Feed View. Before going up to the feed level, we clean up
     * the extras that won't make sense any more up there.
     */
    private CustomAdapterViewTray.OnDragEndListener dragEndListener = new CustomAdapterViewTray.OnDragEndListener() {

        @Override
        public void onDragEnd(MotionEvent arg0, ScrollAxis arg1, Direction arg2, float arg3) {
            Log.v(TAG, "Detected movement " + arg1.toString() + ": " + arg2.toString());
            
            if (arg1 == ScrollAxis.UpDown && arg2 == Direction.Up
                    && customViewTrayAdapter.getViewScrollY() == 0) {
                Intent intent = IntentHelper.createIntent(StoryActivity.this, null, FeedActivity.class);
                intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
                startActivity(intent);
            }
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

    @Deprecated
    private void switchToImageLayout(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.story_image);
        imageView.setImageBitmap(bitmap);

        TextView feed = ((TextView) findViewById(R.id.feed_title));
        feed.setTextColor(getResources().getColor(R.color.story_feed_title_with_background));
        feed.setBackgroundColor(R.color.story_background_feed_title);

        TextView storyCount = ((TextView) findViewById(R.id.story_count));
        storyCount.setTextColor(getResources().getColor(R.color.story_feed_title_with_background));
        storyCount.setBackgroundColor(R.color.story_background_feed_title);

        TextView title = ((TextView) findViewById(R.id.story_title));
        title.setMaxLines(5);
        title.setPadding(3, 0, 3, 3);
        title.setTextColor(getResources().getColor(R.color.story_title_with_background));
        title.setBackgroundColor(R.color.story_background_title);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW);
    }
}