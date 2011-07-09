package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

import com.androidsx.microrss.R;
import com.androidsx.microrss.UpdateService;
import com.androidsx.microrss.WimmTemporaryConstants;
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

         // FIXME: this can't be hard-coded anymore. In AnyRSS, it used to come from the extras
        String rssUrl = getResources().getString(R.string.feed_url);
        String rssName = getResources().getString(R.string.feed_name);

        // FIXME (WIMM): do in an a-sync task? or is this really necessary to build the first view when there are no items?
        writeConfigToBackend(WimmTemporaryConstants.widgetId, this, rssName, rssUrl);
        dispatchToViewActivities();
    }

    private static void writeConfigToBackend(int appWidgetId, Context context, String title,
            String feedUrl) {
        Log.i(TAG, "Save initial config to the DB");

        Log.e(TAG, "FIXME: This is gonna fail unless this is the first time the app is executed");
        ContentValues values = new ContentValues();
        values.put(BaseColumns._ID, appWidgetId);
        values.put(FeedColumns.LAST_UPDATE, -1);
        values.put(FeedColumns.TITLE, title);
        values.put(FeedColumns.FEED_URL, feedUrl);

        // TODO: update instead of insert if editing an existing widget
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
