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

import com.androidsx.anyrss.db.FeedColumns;
import com.androidsx.anyrss.db.FeedTableHelper;
import com.androidsx.anyrss.db.ItemColumns;
import com.androidsx.anyrss.db.ItemTableHelper;

/**
 * Content provider for information about feeds and their items.
 */
public class MicroRssContentProvider extends ContentProvider {
    public static final String TAG = MicroRssContentProvider.class.getSimpleName();

    /** Name of the appwidgets table, whose columns are {@link FeedColumns}. */
    public static final String TABLE_APPWIDGETS = "appwidgets";
    
    /** Name of the items table, whose columns are {@link ItemColumns}. */
    public static final String TABLE_FEED_ITEMS = "items";

    private static final int APPWIDGETS = 101;
    private static final int APPWIDGETS_ID = 102;
    private static final int APPWIDGETS_FORECASTS = 103;

    private static final int FORECASTS = 201;
    private static final int FORECASTS_ID = 202;

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
            case APPWIDGETS: {
                count = db.delete(TABLE_APPWIDGETS, selection, selectionArgs);
                break;
            }
            case APPWIDGETS_ID: {
                // Delete a specific widget and all its forecasts
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                count = db.delete(TABLE_APPWIDGETS, BaseColumns._ID + "=" + appWidgetId, null);
                count += db.delete(TABLE_FEED_ITEMS, ItemColumns.FEED_ID + "="
                        + appWidgetId, null);
                break;
            }
            case APPWIDGETS_FORECASTS: {
                // Delete all the forecasts for a specific widget
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                if (selection == null) {
                    selection = "";
                } else {
                    selection = "(" + selection + ") AND ";
                }
                selection += ItemColumns.FEED_ID + "=" + appWidgetId;
                count = db.delete(TABLE_FEED_ITEMS, selection, selectionArgs);
                break;
            }
            case FORECASTS: {
                count = db.delete(TABLE_FEED_ITEMS, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (getUriMatcher().match(uri)) {
            case APPWIDGETS:
                return FeedTableHelper.CONTENT_TYPE;
            case APPWIDGETS_ID:
                return FeedTableHelper.CONTENT_ITEM_TYPE;
            case APPWIDGETS_FORECASTS:
                return ItemTableHelper.CONTENT_TYPE;
            case FORECASTS:
                return ItemTableHelper.CONTENT_TYPE;
            case FORECASTS_ID:
                return ItemTableHelper.CONTENT_ITEM_TYPE;
        }
        throw new IllegalStateException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Uri resultUri = null;

        switch (getUriMatcher().match(uri)) {
            case APPWIDGETS: {
                Log.w("WIMM", "Here, we used to insert the widget title into the table_appwidgets table");
                try {
                    long rowId = db.insert(TABLE_APPWIDGETS, FeedColumns.FEED_URL, values);
                    if (rowId != -1) {
                        resultUri = ContentUris.withAppendedId(FeedTableHelper.getContentUri(ContentProviderAuthority.AUTHORITY), rowId);
                    }
                } catch (SQLiteConstraintException e) {
                    Log.e("WIMM", "The widget was already created. This is expected, until we fix it in a cleaner way, since we use a constant number. This catch can NOT stay");
                }
                break;
            }
            case APPWIDGETS_FORECASTS: {
                // Insert a feed item into a specific widget
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                values.put(ItemColumns.FEED_ID, appWidgetId);
                long rowId = db.insert(TABLE_FEED_ITEMS, ItemColumns.FEED_URL, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(FeedTableHelper.getContentUri(ContentProviderAuthority.AUTHORITY), rowId);
                }
                break;
            }
            case FORECASTS: {
                long rowId = db.insert(TABLE_FEED_ITEMS, ItemColumns.FEED_URL, values);
                if (rowId != -1) {
                    resultUri = ContentUris.withAppendedId(constructForecastsContentUri(), rowId);
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
            case APPWIDGETS: {
                qb.setTables(TABLE_APPWIDGETS);
                break;
            }
            case APPWIDGETS_ID: {
                String appWidgetId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_APPWIDGETS);
                qb.appendWhere(BaseColumns._ID + "=" + appWidgetId);
                break;
            }
            case APPWIDGETS_FORECASTS: {
                // Pick all the forecasts for given widget, sorted by insertion time
                String appWidgetId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_FEED_ITEMS);
                qb.appendWhere(ItemColumns.FEED_ID + "=" + appWidgetId);
                sortOrder = (sortOrder == null) ? BaseColumns._ID + " ASC" : sortOrder;
                break;
            }
            case FORECASTS: {
                qb.setTables(TABLE_FEED_ITEMS);
                break;
            }
            case FORECASTS_ID: {
                String forecastId = uri.getPathSegments().get(1);
                qb.setTables(TABLE_FEED_ITEMS);
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
            case APPWIDGETS: {
                return db.update(TABLE_APPWIDGETS, values, selection, selectionArgs);
            }
            case APPWIDGETS_ID: {
                long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
                return db.update(TABLE_APPWIDGETS, values, BaseColumns._ID + "=" + appWidgetId,
                        null);
            }
            case FORECASTS: {
                return db.update(TABLE_FEED_ITEMS, values, selection, selectionArgs);
            }
        }

        throw new UnsupportedOperationException();
    }

    private UriMatcher getUriMatcher() {
        if (sUriMatcher == null) {
            sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            sUriMatcher.addURI(ContentProviderAuthority.AUTHORITY, "appwidgets", APPWIDGETS);
            sUriMatcher.addURI(ContentProviderAuthority.AUTHORITY, "appwidgets/#", APPWIDGETS_ID);
            sUriMatcher.addURI(ContentProviderAuthority.AUTHORITY, "appwidgets/#/forecasts", APPWIDGETS_FORECASTS);
            sUriMatcher.addURI(ContentProviderAuthority.AUTHORITY, FeedTableHelper.TWIG_FEED_ITEMS, FORECASTS);
            sUriMatcher.addURI(ContentProviderAuthority.AUTHORITY, FeedTableHelper.TWIG_FEED_ITEMS + "/#", FORECASTS_ID);
        }
        return sUriMatcher;
    }
    
    private Uri constructForecastsContentUri() {
        return Uri.parse("content://" + ContentProviderAuthority.AUTHORITY + "/anyrss");
    }
}
