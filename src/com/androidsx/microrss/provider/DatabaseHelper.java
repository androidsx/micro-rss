package com.androidsx.microrss.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.androidsx.microrss.provider.News.Categories;
import com.androidsx.microrss.provider.News.Feeds;
import com.androidsx.microrss.provider.News.Items;

/**
 * Helper to manage the database version upgrades. Works closely with the content provider,
 * {@link NewsProvider}.
 */
class DatabaseHelper extends SQLiteOpenHelper {
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
        //FlurryAgent.onEvent(FlurryConstants.EVENT_NEW_INSTALL);
        Log.i(TAG, "Create a new database " + DATABASE_NAME
                        + ", version " + DATABASE_VERSION);
        
        db.execSQL("CREATE TABLE " + News.TABLE_CATEGORIES + " ("
                + Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Categories.NAME + " TEXT UNIQUE"
                + ");");
        
        db.execSQL("CREATE TABLE " + News.TABLE_FEEDS + " ("
                + Feeds._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Feeds.CATEGORY_ID + " INTEGER,"
                + Feeds.FEED_URL + " TEXT,"
                + Feeds.TITLE + " TEXT,"
                + Feeds.ACTIVE + " BOOLEAN,"
                + Feeds.LAST_UPDATE + " BIGINT, "
                + Feeds.G_READER + " BOOLEAN"
                + ");");

        db.execSQL("CREATE TABLE " + News.TABLE_ITEMS + " ("
                + Items._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Items.FEED_ID + " INTEGER,"
                + Items.POSITION + " INTEGER,"
                + Items.TITLE + " TEXT, "
                + Items.CONTENT + " TEXT,"
                + Items.ITEM_URL + " TEXT,"
                + Items.THUMBNAIL_URL + " TEXT,"
                + Items.DATE + " INTEGER);");
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
            db.execSQL("DROP TABLE IF EXISTS " + News.TABLE_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + News.TABLE_FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + News.TABLE_ITEMS);
            onCreate(db);
        }
    }
}
