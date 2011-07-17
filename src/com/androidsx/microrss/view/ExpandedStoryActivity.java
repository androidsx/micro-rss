package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Item;

public class ExpandedStoryActivity extends Activity {
    private static final String TAG = "ExpandedStoryActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.expanded_story_wrapper); 
        
        IntentDecoder intentDecoder = new StoryIntentDecoder(getIntent());
        if (intentDecoder.isValidIndex()) {
            Item story = new MicroRssDao(getContentResolver()).findStory(intentDecoder.getCurrentId());
            ((TextView) findViewById(R.id.expanded_story_title)).setText(story.getTitle());
            ((TextView) findViewById(R.id.expanded_story_description)).setText(story.getContent());
        } else {
            Log.e(TAG, "Wrong index: " + intentDecoder.getCurrentIndex() + " (total: " + intentDecoder.getCount() + ")");
            finish();
        }
    }
    
    public void onClickNavigationUp(View target) {
        Intent intent = new Intent(this, StoryActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    public void onClickNavigationLeft(View target) {
        int storyIndex = getIntent().getIntExtra(ExtrasConstants.STORY_INDEX, 0);
        if (storyIndex == 0) {
            Toast.makeText(this, "Can't go left anymore. Already at index " + storyIndex, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go left anymore. Already at index " + storyIndex);
        } else {
            Intent intent = new Intent(this, ExpandedStoryActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(ExtrasConstants.STORY_INDEX, storyIndex - 1);
            startActivity(intent);
        }
    }

    // TODO: copy-pasted from StoryActivity
    public void onClickNavigationRight(View target) {
        int[] storyIds = getIntent().getIntArrayExtra(ExtrasConstants.STORY_IDS);
        int storyIndex = getIntent().getIntExtra(ExtrasConstants.STORY_INDEX, 0);
        if (storyIndex == storyIds.length - 1) {
            Toast.makeText(this, "Can't go right anymore. Already at index " + storyIndex, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go right anymore. Already at index " + storyIndex);
        } else {
            Intent intent = new Intent(this, ExpandedStoryActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(ExtrasConstants.STORY_INDEX, storyIndex + 1);
            startActivity(intent);
        }
    }

    public void onClickNavigationDown(View target) {
        // Can't go further down from here
    }
}