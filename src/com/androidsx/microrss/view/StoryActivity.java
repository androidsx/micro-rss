package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.view.extra.IntentDecoder;
import com.androidsx.microrss.view.extra.IntentEncoder;

public class StoryActivity extends Activity {
    private static final String TAG = "StoryActivity";
    private IntentDecoder intentDecoder;
    private IntentEncoder intentEncoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_wrapper);

        intentDecoder = new IntentDecoder(getIntent(), new StoryNavigationExtras());
        intentEncoder = new IntentEncoder(this, getIntent());
        
        if (intentDecoder.isValidIndex()) {
            MicroRssDao dao = new MicroRssDao(getContentResolver());
            Item story = dao.findStory(intentDecoder.getCurrentId());
            int feedId = (new IntentDecoder(getIntent(), new FeedNavigationExtras())).getCurrentId();
            Feed feed = dao.findFeed(feedId);
            
            ((TextView) findViewById(R.id.feed_title)).setText(feed.getTitle());
            ((TextView) findViewById(R.id.story_count)).setText(getString(R.string.story_count,
                    (intentDecoder.getCurrentIndex() + 1), intentDecoder.getCount()));
            
            ((TextView) findViewById(R.id.story_title)).setText(story.getTitle());
            ((TextView) findViewById(R.id.story_description)).setText(AnyRSSHelper.cleanHTML(story.getContent()));
            Bitmap storyBitmap = AnyRSSHelper.getBitmapFromCache(this, story.getThumbnail());
            if (storyBitmap != null) {
                Log.i(TAG, "Switching layout to story with image: " + story.getThumbnail());
                switchToImageLayout(storyBitmap);
            }
        } else {
            Log.e(TAG, "Wrong index: " + intentDecoder.getCurrentIndex() + " (total: " + intentDecoder.getCount() + ")");
            finish();
        }
    }

    /** Before going up to the feed level, we clean up the extras that won't make sense any more up there. */
    public void onClickNavigationUp(View target) {
        Intent intent = intentEncoder.buildGoUpIntent(FeedActivity.class);
        intent.putExtra(new StoryNavigationExtras().getAllIdsKey(), (String[]) null);
        intent.putExtra(new StoryNavigationExtras().getCurrentIndexKey(), (String) null);
        startActivity(intent);

    }

    public void onClickNavigationLeft(View target) {
        if (intentDecoder.canGoLeft()) {
            startActivity(intentEncoder.buildGoLeftIntent(StoryActivity.class, new StoryNavigationExtras()));
        } else {
            Toast.makeText(this,
                    "Can't go left anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go left anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationRight(View target) {
        if (intentDecoder.canGoRight()) {
            startActivity(intentEncoder.buildGoRightIntent(StoryActivity.class, new StoryNavigationExtras()));
        } else {
            Toast.makeText(this, "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationDown(View target) {
        // Can't go further down from here
    }
    
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
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.story_image);
        
        title.setLayoutParams(params);
    }
}