package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.androidsx.microrss.R;

public class FeedActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);
    }
    
    public void onClickNavigationUp(View target) {
        Toast.makeText(this, "We are supposed to terminate app", Toast.LENGTH_LONG).show();
    }

    public void onClickNavigationLeft(View target) {
        startActivity(new Intent(this, FeedActivity.class));
    }

    public void onClickNavigationRight(View target) {
        startActivity(new Intent(this, FeedActivity.class));
    }

    public void onClickNavigationDown(View target) {
        startActivity(new Intent(this, StoryActivity.class));
    }
}