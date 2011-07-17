package com.androidsx.microrss.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.microrss.R;
import com.androidsx.microrss.cache.CacheImageManager;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Item;

public class StoryActivity extends Activity {
    private static final String TAG = "StoryActivity";
    private IntentDecoder intentDecoder;
    private IntentEncoder intentEncoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_wrapper);

        intentDecoder = new StoryIntentDecoder(getIntent());
        intentEncoder = new StoryIntentEncoder(getIntent());
        
        if (intentDecoder.isValidIndex()) {
            Item story = new MicroRssDao(getContentResolver()).findStory(intentDecoder.getCurrentId());
            ((TextView) findViewById(R.id.story_title)).setText(story.getTitle());
            Bitmap storyBitmap = getStoryBitmap(story.getThumbnail());
            if (storyBitmap != null) {
                Log.i(TAG, "Switching layout to story with image: " + story.getThumbnail());
                switchToImageLayout(storyBitmap);
            }
        } else {
            Log.e(TAG, "Wrong index: " + intentDecoder.getCurrentIndex() + " (total: " + intentDecoder.getCount() + ")");
            finish();
        }
    }

    public void onClickNavigationUp(View target) {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    public void onClickNavigationLeft(View target) {
        if (intentDecoder.canGoLeft()) {
            startActivity(intentEncoder.buildGoLeftIntent(this, StoryActivity.class));
        } else {
            Toast.makeText(this,
                    "Can't go left anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go left anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationRight(View target) {
        if (intentDecoder.canGoRight()) {
            startActivity(intentEncoder.buildGoRightIntent(this, StoryActivity.class));
        } else {
            Toast.makeText(this, "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex(),
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Can't go right anymore. Already at index " + intentDecoder.getCurrentIndex());
        }
    }

    public void onClickNavigationDown(View target) {
        Intent intent = new Intent(this, ExpandedStoryActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }
    
    private Bitmap getStoryBitmap(String url) {
        Bitmap localBitmap = null;
        if (!url.equals("")) {
            CacheImageManager cacheManager = new CacheImageManager(this);
            File imageFromCache = cacheManager.retrieveImage(cacheManager.getFilenameForUrl(url));
            if (imageFromCache != null && imageFromCache.exists()) {
                if (imageFromCache.length() > 1000L) {
                    FileInputStream localFileInputStream;
                    try {
                        localFileInputStream = new FileInputStream(imageFromCache);
                        localBitmap = BitmapFactory.decodeStream(localFileInputStream);
                        localFileInputStream.close();
                    } catch (FileNotFoundException e) {
                        Log.w(TAG, "We couldn't get the cache file for the url: " + url);
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.w(TAG, "Some problems decoding the cache file for url: " + url);
                        e.printStackTrace();
                    }
                } else {
                    localBitmap = BitmapFactory.decodeFile(imageFromCache.getAbsolutePath());
                }
            }
        }
        return localBitmap;
    }
    
    private void switchToImageLayout(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.story_image);
        imageView.setImageBitmap(bitmap);
        
        TextView title = ((TextView) findViewById(R.id.story_title));
        title.setMaxLines(3);
        title.setBackgroundColor(R.color.story_background_title);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.story_image);
        
        title.setLayoutParams(params);
        
        View feedTitleLine = findViewById(R.id.feed_title_bottom_line);
        feedTitleLine.setVisibility(View.GONE);
    }
}