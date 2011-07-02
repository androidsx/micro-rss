package com.androidsx.microrss.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Content provider for information about feeds and their items.
 * TODO (WIMM): consider using http://developer.android.com/reference/android/content/ContentResolver.html#notifyChange(android.net.Uri, android.database.ContentObserver) to notify and get notified of data changes
 * TODO (WIMM): use Activity.managedQuery instead of ContentResolver.query
 */
public class MicroRssContentProvider extends ContentProvider {
    public static final String TAG = MicroRssContentProvider.class.getSimpleName();

    /** Content provider authority for this application. Defined in the manifest file too. */
    private static final String AUTHORITY = "com.androidsx.microrss";
    
    /** Name of the feed table, whose columns are {@link FeedColumns}. */
    public static final String TABLE_FEEDS = "feeds";
    private static final String SINGLE_FEED = "feed";
    
    /** Name of the item table, whose columns are {@link ItemColumns}. */
    public static final String TABLE_ITEMS = "items";
    private static final String SINGLE_ITEM = "item";

    /** Content provider for the feeds table. */
    public static final Uri FEEDS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FEEDS);
    
    /** Content provider for the items table. */
    // TODO: how come this is not used outside? hmm i think they are doing the parse themselves, jodeeeeeeer
    private static final Uri ITEMS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ITEMS);
    
    private static final int FEEDS = 101;
    private static final int FEEDS_ID = 102;
    private static final int FEEDS_ITEMS = 103;

    private static final int ITEMS = 201;
    private static final int ITEMS_ID = 202;

    private DatabaseHelper mOpenHelper;
    
    /**
     * Matcher used to filter an incoming {@link Uri}. Use
     * {@link #getUriMatcher} to access it.
     */
    private UriMatcher sUriMatcher;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count = 0;

        switch (getUriMatcher().match(uri)) {
            case FEEDS: {
                count = db.delete(TABLE_FEEDS, selection, selectionArgs);
                break;
            }
            case FEEDS_ID: {
                // Delete a specific widget and all its forecasts
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                count = db.delete(TABLE_FEEDS, BaseColumns._ID + "=" + appWidgetId, null);
                count += db.delete(TABLE_ITEMS, ItemColumns.FEED_ID + "="
                        + appWidgetId, null);
                break;
            }
            case FEEDS_ITEMS: {
                // Delete all the forecasts for a specific widget
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                if (selection == null) {
                    selection = "";
                } else {
                    selection = "(" + selection + ") AND ";
                }
                selection += ItemColumns.FEED_ID + "=" + appWidgetId;
                count = db.delete(TABLE_ITEMS, selection, selectionArgs);
                break;
            }
            case ITEMS: {
                count = db.delete(TABLE_ITEMS, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        return count;
    }

    @Override
    public String getType(Uri uri) {
        final String FEED_CONTENT_TYPE = "vnd.android.cursor.dir/" + SINGLE_FEED;
        final String FEED_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + SINGLE_FEED;
        
        final String ITEM_CONTENT_TYPE = "vnd.android.cursor.dir/" + SINGLE_ITEM;
        final String ITEM_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + SINGLE_ITEM;
        
        switch (getUriMatcher().match(uri)) {
            case FEEDS:
                return FEED_CONTENT_TYPE;
            case FEEDS_ID:
                return FEED_CONTENT_ITEM_TYPE;
            case FEEDS_ITEMS:
                return ITEM_CONTENT_TYPE;
            case ITEMS:
                return ITEM_CONTENT_TYPE;
            case ITEMS_ID:
                return ITEM_CONTENT_ITEM_TYPE;
        }
        throw new IllegalStateException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri resultUri = null;

        switch (getUriMatcher().match(uri)) {
            case FEEDS: {
                Log.w("WIMM", "Here, we used to insert the widget title into the table_appwidgets table");
                try {
                    long rowId = db.insert(TABLE_FEEDS, FeedColumns.FEED_URL, values);
                    if (rowId != -1) {
                        resultUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, rowId);
                    }
                } catch (SQLiteConstraintException e) {
                    Log.e("WIMM", "The widget was already created. This is expected, until we fix it in a cleaner way, since we use a constant number. This catch can NOT stay");
                }
                break;
            }
            case FEEDS_ITEMS: {
                // Insert a feed item into a specific widget
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                values.put(ItemColumns.FEED_ID, appWidgetId);
                long rowId = db.insert(TABLE_ITEMS, ItemColumns.FEED_URL, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, rowId);
                }
                break;
            }
            case ITEMS: {
                long rowId = db.insert(TABLE_ITEMS, ItemColumns.FEED_URL, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(ITEMS_CONTENT_URI, rowId);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        return resultUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "query() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String limit = null;

        switch (getUriMatcher().match(uri)) {
            case FEEDS: {
                qb.setTables(TABLE_FEEDS);
                break;
            }
            case FEEDS_ID: {
                String appWidgetId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_FEEDS);
                qb.appendWhere(BaseColumns._ID + "=" + appWidgetId);
                break;
            }
            case FEEDS_ITEMS: {
                // Pick all the forecasts for given widget, sorted by insertion time
                String appWidgetId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_ITEMS);
                qb.appendWhere(ItemColumns.FEED_ID + "=" + appWidgetId);
                sortOrder = (sortOrder == null) ? BaseColumns._ID + " ASC" : sortOrder;
                break;
            }
            case ITEMS: {
                qb.setTables(TABLE_ITEMS);
                break;
            }
            case ITEMS_ID: {
                String forecastId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_ITEMS);
                qb.appendWhere(BaseColumns._ID + "=" + forecastId);
                break;
            }
        }

        return qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(TAG, "update() with uri=" + uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (getUriMatcher().match(uri)) {
            case FEEDS: {
                return db.update(TABLE_FEEDS, values, selection, selectionArgs);
            }
            case FEEDS_ID: {
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                return db.update(TABLE_FEEDS, values, BaseColumns._ID + "=" + appWidgetId,
                        null);
            }
            case ITEMS: {
                return db.update(TABLE_ITEMS, values, selection, selectionArgs);
            }
        }

        throw new UnsupportedOperationException();
    }

    private UriMatcher getUriMatcher() {
        if (sUriMatcher == null) {
            sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            sUriMatcher.addURI(AUTHORITY, TABLE_FEEDS, FEEDS);
            sUriMatcher.addURI(AUTHORITY, TABLE_FEEDS + "/#", FEEDS_ID);
            sUriMatcher.addURI(AUTHORITY, TABLE_FEEDS + "/#/" + TABLE_ITEMS, FEEDS_ITEMS); // 
            sUriMatcher.addURI(AUTHORITY, TABLE_ITEMS, ITEMS);
            sUriMatcher.addURI(AUTHORITY, TABLE_ITEMS + "/#", ITEMS_ID);
        }
        return sUriMatcher;
    }
}
