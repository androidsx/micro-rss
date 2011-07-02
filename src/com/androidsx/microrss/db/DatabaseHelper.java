package com.androidsx.microrss.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.androidsx.anyrss.FlurryConstants;
import com.androidsx.anyrss.db.FeedColumns;
import com.androidsx.anyrss.db.ItemColumns;
import com.flurry.android.FlurryAgent;

/**
 * Helper to manage upgrading between versions of the forecast database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "anyrss.db";

    private static final int DATABASE_VERSION = 3;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the DB tables. This is only done when the first widget is
     * added, usually after installing the application.
     * 
     * @param db reference to the SQLite database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        FlurryAgent.onEvent(FlurryConstants.EVENT_NEW_INSTALL);
        Log.w(TAG, "Creating a new database " + DATABASE_NAME
                        + ", version " + DATABASE_VERSION);
        db.execSQL("CREATE TABLE " + MicroRssContentProvider.TABLE_FEEDS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                + FeedColumns.FEED_URL + " TEXT,"
                + FeedColumns.WEBVIEW_TYPE + " INTEGER,"
                + FeedColumns.LAST_UPDATED + " BIGINT,"
                + FeedColumns.UPDATE_INTERVAL + " INTEGER);");

        db.execSQL("CREATE TABLE " + MicroRssContentProvider.TABLE_ITEMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemColumns.FEED_ID + " INTEGER,"
                + ItemColumns.ITEM_INDEX + " INTEGER,"
                + ItemColumns.FEED_TITLE + " TEXT,"
                + ItemColumns.ITEM_CONTENT + " TEXT,"
                + ItemColumns.FEED_URL + " TEXT,"
                + ItemColumns.ITEM_DATE + " INTEGER,"
                + ItemColumns.ITEM_URL + " TEXT);");
    }

    /**
     * Upgrade to a different DB version. Which means, the schema has
     * changed with this new version of the application, which the user just
     * installed.
     * <p>
     * Since the schema has changed, and we do not know how the old DB looks
     * like, we are going to have to remove all the old data, and let the
     * existing widgets crash miserably.
     * <p>
     * TODO(pablo): mechanism to let the update service, or the app widgets,
     * that this has happened, so proper error messages can be shown.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        
        if (version != DATABASE_VERSION) {
            Log.w(TAG, "Destroying old data during upgrade.");
            db.execSQL("DROP TABLE IF EXISTS " + MicroRssContentProvider.TABLE_FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + MicroRssContentProvider.TABLE_ITEMS);
            onCreate(db);
        }
    }
}