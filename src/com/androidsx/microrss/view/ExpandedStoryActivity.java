package com.androidsx.microrss.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.view.extra.IntentDecoder;
import com.androidsx.microrss.view.extra.IntentEncoder;

public class ExpandedStoryActivity extends Activity {
    private static final String TAG = "ExpandedStoryActivity";
    private IntentDecoder intentDecoder;
    private IntentEncoder intentEncoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.expanded_story_wrapper); 
        
        intentDecoder = new IntentDecoder(getIntent(), new StoryNavigationExtras());
        intentEncoder = new IntentEncoder(this, getIntent());
        
        if (intentDecoder.isValidIndex()) {
            Item story = new MicroRssDao(getContentResolver()).findStory(intentDecoder.getCurrentId());
            ((TextView) findViewById(R.id.expanded_story_title)).setText(story.getTitle());
            ((TextView) findViewById(R.id.expanded_story_description)).setText(AnyRSSHelper.cleanHTML(story.getContent()));
        } else {
            Log.e(TAG, "Wrong index: " + intentDecoder.getCurrentIndex() + " (total: " + intentDecoder.getCount() + ")");
            finish();
        }
    }
    
    public void onClickNavigationUp(View target) {
        startActivity(intentEncoder.buildGoUpIntent(StoryActivity.class));
    }

    public void onClickNavigationLeft(View target) {
        if (intentDecoder.canGoLeft()) {
            startActivity(intentEncoder.buildGoLeftIntent(ExpandedStoryActivity.class, new StoryNavigationExtras()));
        } else {
            Toast.makeText(this,
                    "Can't go left anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go left anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationRight(View target) {
        if (intentDecoder.canGoRight()) {
            startActivity(intentEncoder.buildGoRightIntent(ExpandedStoryActivity.class, new StoryNavigationExtras()));
        } else {
            Toast.makeText(this,
                    "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG,
                    "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationDown(View target) {
        // Can't go further down from here
    }
}