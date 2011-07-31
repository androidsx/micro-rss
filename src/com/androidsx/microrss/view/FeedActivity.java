package com.androidsx.microrss.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.Preferences;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.wimm.framework.app.LauncherActivity;
import com.wimm.framework.view.AdapterViewTray;
import com.wimm.framework.view.MotionInterpreter;

public class FeedActivity extends LauncherActivity {
    private static final String TAG = "FeedActivity";

    private AdapterViewTray viewTray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);

        configureViewTray((AdapterViewTray) findViewById(R.id.viewTray));

        FeedAdapter feedAdapter = new FeedAdapter(this, (Feed[]) new MicroRssDao(
                getContentResolver()).findActiveFeeds().toArray(new Feed[0]));
        if (feedAdapter.getCount() >= 0) {
            int currentId = getIntent().getIntExtra(new FeedNavigationExtras().getCurrentIdKey(), -1);
            int position = feedAdapter.getItemPosition(currentId, 0);
            if (position >= 0) {
                viewTray.setAdapter(feedAdapter);
                viewTray.setIndex(position);
            } else {
                Log.e(TAG, "Wrong feed id: " + currentId);
                Toast.makeText(this, "Wrong feed id: " + currentId, Toast.LENGTH_SHORT).show();
                finish(); // TODO: error message or new activity but with sliders to go to settings.
            }
            
        } else {
            Log.e(TAG, "There are no active feeds");
            Toast.makeText(this, "There are no active feeds", Toast.LENGTH_SHORT).show();
            finish(); // TODO: error message or new activity but with sliders to go to settings.
        }
    }

    private void configureViewTray(AdapterViewTray adapterViewTray) {
        viewTray = adapterViewTray;
        MotionInterpreter.ScrollAxis scrollAxis = MotionInterpreter.ScrollAxis.LeftRight;
        viewTray.setMotionAxis(scrollAxis);
        viewTray.setCanScrollInternalView(false);
        viewTray.setCanLoop(false);
    }

    public void onFeedClick(View target) {
        Intent intent = IntentHelper.createIntent(this, null, StoryActivity.class);

        int feedId = (int) viewTray.getAdapter().getItemId(viewTray.getIndex());
        intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
        startActivity(intent);
    }

    public void onClickNavigationUp(View target) {
        Toast.makeText(this, "Terminate application!", Toast.LENGTH_LONG).show();
    }

    public void onClickNavigationLeft(View target) {
        int currentIndex = viewTray.getIndex();
        if (currentIndex > 0) {
            viewTray.setIndex(currentIndex - 1);
        } else {
            startActivity(new Intent(this, Preferences.class));
            Log.d(TAG, "Can't go left anymore. Go to Settings");
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
        onFeedClick(null);
    }
}
