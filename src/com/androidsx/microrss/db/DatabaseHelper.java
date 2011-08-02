package com.androidsx.microrss.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.androidsx.microrss.FlurryConstants;
import com.flurry.android.FlurryAgent;

/**
 * Helper to manage the database version upgrades. Works closely with the content provider,
 * {@link MicroRssContentProvider}.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "microrss.db";
    private static final int DATABASE_VERSION = 15;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the DB tables. This is only done when the first feed is
     * added, usually after installing the application.
     * 
     * @param db reference to the SQLite database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        FlurryAgent.onEvent(FlurryConstants.EVENT_NEW_INSTALL);
        Log.i(TAG, "Create a new database " + DATABASE_NAME
                        + ", version " + DATABASE_VERSION);
        db.execSQL("CREATE TABLE " + MicroRssContentProvider.TABLE_FEEDS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FeedColumns.FEED_URL + " TEXT,"
                + FeedColumns.TITLE + " TEXT,"
                + FeedColumns.ACTIVE + " BOOLEAN,"
                + FeedColumns.LAST_UPDATE + " BIGINT, "
                + FeedColumns.G_READER + " BOOLEAN"
                + ");");

        db.execSQL("CREATE TABLE " + MicroRssContentProvider.TABLE_ITEMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ItemColumns.FEED_ID + " INTEGER,"
                + ItemColumns.POSITION + " INTEGER,"
                + ItemColumns.TITLE + " TEXT, "
                + ItemColumns.CONTENT + " TEXT,"
                + ItemColumns.ITEM_URL + " TEXT,"
                + ItemColumns.THUMBNAIL_URL + " TEXT,"
                + ItemColumns.DATE + " INTEGER);");
        
        db.execSQL("CREATE UNIQUE INDEX " + "INDEX_URL" + " ON " + MicroRssContentProvider.TABLE_FEEDS + " (" + FeedColumns.FEED_URL + ");");
        
    }

    /**
     * Upgrade to a different DB version. Which means, the schema has
     * changed with this new version of the application, which the user just
     * installed.
     * <p>
     * Since the schema has changed, and we do not know how the old DB looks
     * like, we are going to have to remove all the old data, and let the
     * existing feeds crash miserably.
     * <p>
     * TODO(pablo): mechanism to let the update service, or the app feeds,
     * that this has happened, so proper error messages can be shown.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        
        if (version != DATABASE_VERSION) {
            Log.w(TAG, "The database version has changed from " + oldVersion + " to " + newVersion
                    + ". We will now destroy all existing tables and recreate them");
            db.execSQL("DROP TABLE IF EXISTS " + MicroRssContentProvider.TABLE_FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + MicroRssContentProvider.TABLE_ITEMS);
            onCreate(db);
        }
    }
}
