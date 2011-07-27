package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.Preferences;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class FeedActivity extends Activity {
    private static final String TAG = "FeedActivity";
    private NavigationProcessor navigation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);
        
        int[] ids = getIntent().getIntArrayExtra(new FeedNavigationExtras().getAllIdsKey());
        int currentIndex = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIndexKey(), 0);
        navigation = new NavigationProcessor(ids, currentIndex);
        
        if (navigation.isValidIndex()) {
            Feed feed = new MicroRssDao(getContentResolver()).findFeed(navigation.getCurrentId());
            
            ((TextView) findViewById(R.id.feed_title)).setText(feed.getTitle());
            ((TextView) findViewById(R.id.feed_count)).setText(getString(R.string.feed_count,
                    (navigation.getCurrentIndex() + 1), navigation.getCount()));
            
            Bitmap favicon = AnyRSSHelper.getBitmapFromCache(this, AnyRSSHelper.retrieveFaviconUrl(feed.getURL()));
            if (favicon != null) {
                ((ImageView) findViewById(R.id.feed_image)).setImageBitmap(favicon);
            }
        } else {
            Log.e(TAG, "Wrong index: " + navigation.getCurrentIndex() + " (total: " + navigation.getCount() + ")");
            finish();
        }
    }
    
    public void onClickNavigationUp(View target) {
        Toast.makeText(this, "Terminate application!", Toast.LENGTH_LONG).show();
    }

    public void onClickNavigationLeft(View target) {
        if (navigation.canGoLeft()) {
            Intent intent = IntentHelper.createIntent(this, getIntent().getExtras(), FeedActivity.class);
            intent.putExtra(new FeedNavigationExtras().getCurrentIndexKey(), navigation.goLeft());
            startActivity(intent);
        } else {
            startActivity(new Intent(this, Preferences.class));
            Log.d(TAG, "Can't go left anymore. Go to Settings");
        }
    }

    public void onClickNavigationRight(View target) {
        if (navigation.canGoRight()) {
            Intent intent = IntentHelper.createIntent(this, getIntent().getExtras(), FeedActivity.class);
            intent.putExtra(new FeedNavigationExtras().getCurrentIndexKey(), navigation.goRight());
            startActivity(intent);
        } else {
            Toast.makeText(this,
                    "Can't go right anymore. Already at index " + navigation.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG,
                    "Can't go right anymore. Already at index " + navigation.getCurrentIndex());
        }
    }

    public void onClickNavigationDown(View target) {
        MicroRssDao dao = new MicroRssDao(getContentResolver());
        int[] storyIds = dao.findStoryIds(navigation.getCurrentId());
        Intent intent = IntentHelper.createIntent(this, getIntent().getExtras(), StoryActivity.class);
        intent.putExtra(new StoryNavigationExtras().getAllIdsKey(), storyIds);
        intent.putExtra(new StoryNavigationExtras().getCurrentIndexKey(), 0);
        startActivity(intent);
    }
}
