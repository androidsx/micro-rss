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
import com.androidsx.microrss.view.extra.IntentDecoder;
import com.androidsx.microrss.view.extra.IntentEncoder;

public class FeedActivity extends Activity {
    private static final String TAG = "FeedActivity";
    private IntentDecoder intentDecoder;
    private IntentEncoder intentEncoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);
        
        intentDecoder = new IntentDecoder(getIntent(), new FeedNavigationExtras());
        intentEncoder = new IntentEncoder(this, getIntent());
        
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
        Toast.makeText(this, "Terminate application!", Toast.LENGTH_LONG).show();
    }

    public void onClickNavigationLeft(View target) {
        if (intentDecoder.canGoLeft()) {
            startActivity(intentEncoder.buildGoLeftIntent(FeedActivity.class, new FeedNavigationExtras()));
        } else {
            startActivity(new Intent(this, Preferences.class));
            Log.d(TAG, "Can't go left anymore. Go to Settings");
        }
    }

    public void onClickNavigationRight(View target) {
        if (intentDecoder.canGoRight()) {
            startActivity(intentEncoder.buildGoRightIntent(FeedActivity.class, new FeedNavigationExtras()));
        } else {
            Toast.makeText(this,
                    "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG,
                    "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationDown(View target) {
        MicroRssDao dao = new MicroRssDao(getContentResolver());
        int[] storyIds = dao.findStoryIds(intentDecoder.getCurrentId());
        startActivity(intentEncoder.buildGoDownIntent(StoryActivity.class, new StoryNavigationExtras(), storyIds));
    }
}
