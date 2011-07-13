package com.androidsx.microrss.db;

import java.util.Arrays;

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
 * Content provider for information about feeds and their items. It is strongly recommended to
 * perform the DB access operations in a separate thread.
 * <p>
 * TODO (WIMM): consider using http://developer.android.com/reference/android/content/ContentResolver.html#notifyChange(android.net.Uri, android.database.ContentObserver) to notify and get notified of data changes
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

    /**
     * Content provider for the feeds table.
     * <p>
     * Use {@code ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId)} in
     * order to access a single feed.
     */
    public static final Uri FEEDS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FEEDS);
    
    /** Content provider for the items table. */
    public static final Uri ITEMS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ITEMS);
    
    private static final int ALL_FEEDS = 101;
    private static final int A_FEED_BY_ID = 102;
    private static final int ALL_ITEMS_FOR_A_FEED_BY_ID = 103;
    private static final int ALL_ITEMS = 201;
    private static final int A_ITEMS_BY_ID = 202;

    private DatabaseHelper databaseHelper;
    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete() with uri=" + uri + ", selection=" + selection + ", args=" + arrayToString(selectionArgs));
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int count = 0;
        switch (getUriMatcher().match(uri)) {
            case ALL_FEEDS: {
                Log.d(TAG, "Delete all the feeds that match " + selection + " with " +  arrayToString(selectionArgs));
                count += db.delete(TABLE_FEEDS, selection, selectionArgs);
                break;
            }
            case A_FEED_BY_ID: {
                long feedId = Long.parseLong(uri.getPathSegments().get(1));
                Log.d(TAG, "Delete the feed " + feedId + " and all its items");
                count += db.delete(TABLE_FEEDS, BaseColumns._ID + "=" + feedId, null);
                count += db.delete(TABLE_ITEMS, ItemColumns.FEED_ID + "=" + feedId, null);
                break;
            }
            case ALL_ITEMS_FOR_A_FEED_BY_ID: {
                long feedId = Long.parseLong(uri.getPathSegments().get(1));
                selection = (selection == null ? "" : "(" + selection + ") AND ") + ItemColumns.FEED_ID + "=" + feedId;
                Log.d(TAG, "Delete the items " + arrayToString(selectionArgs) + " for the feed " + feedId);
                count += db.delete(TABLE_ITEMS, selection, selectionArgs);
                break;
            }
            case ALL_ITEMS: {
                Log.d(TAG, "Delete all items for all feeds that match " + selection + " with " +  arrayToString(selectionArgs));
                count += db.delete(TABLE_ITEMS, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        Log.v(TAG, "delete() is done. " + count + " elements were deleted");
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, "insert() with uri=" + uri + ", values=[...]");
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Uri resultUri = null;
        switch (getUriMatcher().match(uri)) {
            case ALL_FEEDS: {
                Log.d(TAG, "Insert a new feed");
                try {
                    long feedId = db.insert(TABLE_FEEDS, null, values);
                    if (feedId != -1) {
                        resultUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId);
                    }
                } catch (SQLiteConstraintException e) {
                    // FIXME: Soon...
                    Log.e("WIMM", "This feed already exists. This is expected, until we fix it in a cleaner way, since we use a constant number. This catch can NOT stay");
                }
                break;
            }
            case ALL_ITEMS_FOR_A_FEED_BY_ID: {
                long feedId = Long.parseLong(uri.getPathSegments().get(1));
                Log.d(TAG, "Insert a new item for the feed " + feedId);
                values.put(ItemColumns.FEED_ID, feedId);
                long rowId = db.insert(TABLE_ITEMS, null, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(MicroRssContentProvider.ITEMS_CONTENT_URI, rowId);
                }
                break;
            }
            case ALL_ITEMS: {
                Log.d(TAG, "Insert items, just like that. Not attached to a feed?");
                long rowId = db.insert(TABLE_ITEMS, null, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(ITEMS_CONTENT_URI, rowId);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        Log.v(TAG, "insert() is done. Results are in uri " + resultUri);
        return resultUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "query() with uri=" + uri + ", projection=" + arrayToString(projection)
                + ", selection=" + selection + ", selectionArgs=" + arrayToString(selectionArgs));
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (getUriMatcher().match(uri)) {
            case ALL_FEEDS: {
                Log.d(TAG, "Fetch all feeds");
                qb.setTables(TABLE_FEEDS);
                break;
            }
            case A_FEED_BY_ID: {
                String feedId = uri.getPathSegments().get(1);
                Log.d(TAG, "Fetch the feed with id " + feedId);
                qb.setTables(TABLE_FEEDS);
                qb.appendWhere(BaseColumns._ID + "=" + feedId);
                break;
            }
            case ALL_ITEMS_FOR_A_FEED_BY_ID: {
                String feedId = uri.getPathSegments().get(1);
                Log.d(TAG, "Fetch all items for the feed " + feedId);
                qb.setTables(TABLE_ITEMS);
                qb.appendWhere(ItemColumns.FEED_ID + "=" + feedId);
                sortOrder = (sortOrder == null) ? BaseColumns._ID + " ASC" : sortOrder;
                break;
            }
            case ALL_ITEMS: {
                Log.d(TAG, "Fetch all items for all feeds");
                qb.setTables(TABLE_ITEMS);
                break;
            }
            case A_ITEMS_BY_ID: {
                String itemId = uri.getPathSegments().get(1);
                Log.d(TAG, "Fetch the item with id " + itemId);
                qb.setTables(TABLE_ITEMS);
                qb.appendWhere(BaseColumns._ID + "=" + itemId);
                break;
            }
        }

        return qb.query(databaseHelper.getReadableDatabase(), projection, selection, selectionArgs,
                null, null, sortOrder, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(TAG, "update() with uri=" + uri + ", values=" + values
                + ", selection=" + selection + ", selectionArgs=" + arrayToString(selectionArgs));
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int count = 0;
        switch (getUriMatcher().match(uri)) {
            case ALL_FEEDS: {
                count = db.update(TABLE_FEEDS, values, selection, selectionArgs);
                break;
            }
            case A_FEED_BY_ID: {
                long feedId = Long.parseLong(uri.getPathSegments().get(1));
                count = db.update(TABLE_FEEDS, values, BaseColumns._ID + "=" + feedId,
                        null);
                break;
            }
            case ALL_ITEMS: {
                count = db.update(TABLE_ITEMS, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        Log.v(TAG, "update() is done. " + count + " elements were updated");
        return count;
    }
    
    @Override
    public String getType(Uri uri) {
        final String FEED_CONTENT_TYPE = "vnd.android.cursor.dir/" + SINGLE_FEED;
        final String FEED_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + SINGLE_FEED;
        
        final String ITEM_CONTENT_TYPE = "vnd.android.cursor.dir/" + SINGLE_ITEM;
        final String ITEM_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + SINGLE_ITEM;
        
        switch (getUriMatcher().match(uri)) {
            case ALL_FEEDS:
                return FEED_CONTENT_TYPE;
            case A_FEED_BY_ID:
                return FEED_CONTENT_ITEM_TYPE;
            case ALL_ITEMS_FOR_A_FEED_BY_ID:
                return ITEM_CONTENT_TYPE;
            case ALL_ITEMS:
                return ITEM_CONTENT_TYPE;
            case A_ITEMS_BY_ID:
                return ITEM_CONTENT_ITEM_TYPE;
        }
        throw new IllegalStateException();
    }

    private UriMatcher getUriMatcher() {
        if (uriMatcher == null) {
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            
            uriMatcher.addURI(AUTHORITY, TABLE_FEEDS,                       ALL_FEEDS);
            uriMatcher.addURI(AUTHORITY, TABLE_FEEDS + "/#",                A_FEED_BY_ID);
            uriMatcher.addURI(AUTHORITY, TABLE_FEEDS + "/#/" + TABLE_ITEMS, ALL_ITEMS_FOR_A_FEED_BY_ID);
            
            uriMatcher.addURI(AUTHORITY, TABLE_ITEMS,                       ALL_ITEMS);
            uriMatcher.addURI(AUTHORITY, TABLE_ITEMS + "/#",                A_ITEMS_BY_ID);
        }
        return uriMatcher;
    }
    
    private static String arrayToString(Object[] array) {
        return array == null ? "[]" : Arrays.asList(array).toString();
    }
}
