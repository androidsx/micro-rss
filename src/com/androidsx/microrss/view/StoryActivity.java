package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.androidsx.microrss.R;

public class StoryActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_wrapper);
    }
    
    public void onClickNavigationUp(View target) {
        startActivity(new Intent(this, FeedActivity.class));
    }

    public void onClickNavigationLeft(View target) {
        startActivity(new Intent(this, StoryActivity.class));
    }

    public void onClickNavigationRight(View target) {
        startActivity(new Intent(this, StoryActivity.class));
    }

    public void onClickNavigationDown(View target) {
        startActivity(new Intent(this, ExpandedStoryActivity.class));
    }
}