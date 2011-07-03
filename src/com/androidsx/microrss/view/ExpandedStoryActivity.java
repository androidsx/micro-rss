package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.androidsx.microrss.R;

public class ExpandedStoryActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.expanded_story_wrapper); 
    }
    
    public void onClickNavigationUp(View target) {
        startActivity(new Intent(this, StoryActivity.class));
    }

    public void onClickNavigationLeft(View target) {
        startActivity(new Intent(this, ExpandedStoryActivity.class));
    }

    public void onClickNavigationRight(View target) {
        startActivity(new Intent(this, ExpandedStoryActivity.class));
    }

    public void onClickNavigationDown(View target) {
        Toast.makeText(this, "Do nothing", Toast.LENGTH_LONG).show();
    }
}