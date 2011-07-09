package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.androidsx.microrss.R;
import com.androidsx.microrss.UpdateService;
import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.db.dao.MicroRssDao;

/**
 * Main activity: starts the service, waits for the configuration thread to do the first update, and
 * then gets the items from the DB, and passes them to the view activity.
 */
public class InitActivity extends Activity {
    public static final String TAG = "RetrieveRssItemsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate init activity");

        Log.i(TAG, "Start the update service");
        startService(new Intent(this, UpdateService.class)); // if already started, does nothing

        MicroRssDao dao = new MicroRssDao(getContentResolver());
        int[] currentIds = dao.findFeedIds();
        if (currentIds.length == 0) {
            Log.i("WIMM", "This is temporary: put some feeds into the DB");
            // FIXME (WIMM): do in an a-sync task? or is this really necessary to build the first view when there are no items?
            writeConfigToBackend(this, "Tech Crunch", "http://feeds.feedburner.com/Techcrunch");
            writeConfigToBackend(this, "BBC Top Stories", "http://feeds.bbci.co.uk/news/rss.xml");
            writeConfigToBackend(this, "and.roid.es", "http://feeds.feedburner.com/AndroidEnEspanol");
            writeConfigToBackend(this, "Geek And Poke", "http://geekandpoke.typepad.com/geekandpoke/rss.xml");
        }
        
        dispatchToViewActivities();
    }

    private static void writeConfigToBackend(Context context, String title,
            String feedUrl) {
        Log.i(TAG, "Save initial config to the DB");

        Log.e(TAG, "FIXME: This is terrible: we create more and more feeds");
        ContentValues values = new ContentValues();
        values.put(FeedColumns.LAST_UPDATE, -1);
        values.put(FeedColumns.TITLE, title);
        values.put(FeedColumns.FEED_URL, feedUrl);

        // TODO: update instead of insert if editing an existing feed
        ContentResolver resolver = context.getContentResolver();
        resolver.insert(MicroRssContentProvider.FEEDS_CONTENT_URI, values);
    }
    
    private void dispatchToViewActivities() {
        Log.i(TAG, "Dispatch to the view activities");

        Intent intent = new Intent(this, FeedActivity.class);

        final MicroRssDao dao = new MicroRssDao(this.getContentResolver());
        int[] feedIds = dao.findFeedIds();
        final int firstFeedIndex = 0;
        
        // TODO: If there are no feeds, dispatch to a different view
        if (feedIds.length > 0) {
            intent.putExtra(ExtrasConstants.FEED_IDS, feedIds);
            intent.putExtra(ExtrasConstants.FEED_INDEX, firstFeedIndex);
            startActivity(intent);
            Log.i(TAG, "End of the initialization activity");
        } else {
            // FIXME: deal with this properly
            Log.e(TAG, "There are no feeds");
        }
        finish();
    }
}
