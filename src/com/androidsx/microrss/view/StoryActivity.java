package com.androidsx.microrss.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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
import com.wimm.framework.app.LauncherActivity;
import com.wimm.framework.view.AdapterViewTray;
import com.wimm.framework.view.MotionInterpreter;

public class StoryActivity extends LauncherActivity {
    private static final String TAG = "StoryActivity";

    private AdapterViewTray viewTray;
    private int feedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_wrapper);

        configureViewTray((AdapterViewTray) findViewById(R.id.viewTray));

        feedId = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIdKey(), -1);
        if (feedId != -1) {
            MicroRssDao dao = new MicroRssDao(getContentResolver());
            Feed feed = dao.findFeed(feedId);

            StoryAdapter storyAdapter = new StoryAdapter(this, (Item[]) dao.findStories(feedId)
                    .toArray(new Item[0]), feed);
            if (storyAdapter.getCount() >= 0) {
                viewTray.setAdapter(storyAdapter);
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

    private void configureViewTray(AdapterViewTray adapterViewTray) {
        viewTray = adapterViewTray;
        MotionInterpreter.ScrollAxis scrollAxis = MotionInterpreter.ScrollAxis.LeftRight;
        viewTray.setMotionAxis(scrollAxis);
        viewTray.setCanScrollInternalView(true);
    }

    /**
     * Shortcut to develop the logic for scroll up. Before going up to the feed level, we clean up
     * the extras that won't make sense any more up there. TODO: do it in the right way with a swipe
     * up/down component
     */
    @Override
    public boolean dragCanExit() {
        if (viewTray.getViewScrollY() == 0) {
            Intent intent = IntentHelper.createIntent(this, null, FeedActivity.class);
            intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
            startActivity(intent);
        }
        return false;
    }

    public void onClickNavigationUp(View target) {
        Intent intent = IntentHelper.createIntent(this, null, FeedActivity.class);
        intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
        startActivity(intent);
    }

    public void onClickNavigationLeft(View target) {
        int currentIndex = viewTray.getIndex();
        if (currentIndex > 0) {
            viewTray.setIndex(currentIndex - 1);
        } else {
            Log.w(TAG, "Can't go left anymore. Already at index " + currentIndex);
        }
    }

    public void onClickNavigationRight(View target) {
        int currentIndex = viewTray.getIndex();
        if (currentIndex < viewTray.getAdapter().getCount() - 1) {
            viewTray.setIndex(currentIndex + 1);
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