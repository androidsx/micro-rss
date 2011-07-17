package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.Preferences;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class FeedActivity extends Activity {
    private static final String TAG = "FeedActivity";
    private IntentDecoder intentDecoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);
        
        intentDecoder = new FeedIntentDecoder(getIntent());
        if (intentDecoder.isValidIndex()) {
            Feed feed = new MicroRssDao(getContentResolver()).findFeed(intentDecoder.getCurrentId());
            ((TextView) findViewById(R.id.feed_title)).setText(feed.getTitle());
            ((TextView) findViewById(R.id.feed_count)).setText(getString(R.string.feed_count,
                    (intentDecoder.getCurrentIndex() + 1), intentDecoder.getCount()));
        } else {
            Log.e(TAG, "Wrong index: " + intentDecoder.getCurrentIndex() + " (total: " + intentDecoder.getCount() + ")");
            finish();
        }
    }
    
    public void onClickNavigationUp(View target) {
        Toast.makeText(this, "We are supposed to terminate app", Toast.LENGTH_LONG).show();
    }

    public void onClickNavigationLeft(View target) {
        int feedIndex = getIntent().getIntExtra(ExtrasConstants.FEED_INDEX, 0);
        if (intentDecoder.canGoLeft()) {
            Intent intent = new Intent(this, FeedActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(ExtrasConstants.FEED_INDEX, feedIndex - 1);
            // FIXME: here, we need to reload the array of story ids
            startActivity(intent);
        } else {
            startActivity(new Intent(this, Preferences.class));
            Log.d(TAG, "Can't go left anymore. Go to Settings");
        }
    }

    public void onClickNavigationRight(View target) {
        int feedIndex = getIntent().getIntExtra(ExtrasConstants.FEED_INDEX, 0);
        if (intentDecoder.canGoRight()) {
            Intent intent = new Intent(this, FeedActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(ExtrasConstants.FEED_INDEX, feedIndex + 1);
            // FIXME: here, we need to reload the array of story ids
            startActivity(intent);
        } else {
            Toast.makeText(this, "Can't go right anymore. Already at index " + feedIndex, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go right anymore. Already at index " + feedIndex);
        }
    }

    public void onClickNavigationDown(View target) {
        final Intent intent = new Intent(this, StoryActivity.class);
        intent.putExtras(getIntent().getExtras());
        final int[] feedIds = getIntent().getIntArrayExtra(ExtrasConstants.FEED_IDS);
        final int feedIndex = getIntent().getIntExtra(ExtrasConstants.FEED_INDEX, 0);
        if (feedIndex >= feedIds.length) {
            Log.e(TAG, "OUCH"); // FIXME
        } else {
            int feedId = feedIds[feedIndex];
            final MicroRssDao dao = new MicroRssDao(getContentResolver());
            intent.putExtra(ExtrasConstants.STORY_IDS, dao.findStoryIds(feedId));
            intent.putExtra(ExtrasConstants.STORY_INDEX, 0);
            startActivity(intent);
        }
    }
}
