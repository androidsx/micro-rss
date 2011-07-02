package com.androidsx.microrss.view;

import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;

import com.androidsx.anyrss.db.AppWidgets;
import com.androidsx.anyrss.db.AppWidgetsColumns;
import com.androidsx.anyrss.db.FeedItemColumns;
import com.androidsx.anyrss.domain.DefaultItem;
import com.androidsx.anyrss.domain.DefaultItemList;
import com.androidsx.anyrss.domain.ItemList;
import com.androidsx.microrss.db.ContentProviderAuthority;

public class ExtrasEmulator {

  private static final String TAG = "ExtrasEmulator";

  private static final String[] PROJECTION_APPWIDGETS = new String[] {
      AppWidgetsColumns.TITLE,
      AppWidgetsColumns.LAST_UPDATED,
      AppWidgetsColumns.CURRENT_ITEM_POSITION };

  private static final int COL_TITLE = 0;
  private static final int COL_LAST_UPDATED = 1;
  private static final int COL_CURRENT_ITEM_POSITION = 2;

  private static final String[] PROJECTION_FEEDS = new String[] { FeedItemColumns.FEED_TITLE,
      FeedItemColumns.FEED_CONTENT, FeedItemColumns.FEED_URL, FeedItemColumns.FEED_DATE, };

  private static final int COL_FEED_TITLE = 0;
  private static final int COL_FEED_CONTENT = 1;
  private static final int COL_FEED_URL = 2;
  private static final int COL_FEED_DATE = 3;

  private ExtrasEmulator() {
  }

  public static ItemList extractItemList(Context context, int appWidgetId) {

    Log.d(TAG, "--------- extras emulator begin ----------");
    Log.d(TAG, "Context: " + context + ", " + context.getContentResolver());
    Log.d(TAG, "widget ID: " + appWidgetId);

    Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY), appWidgetId);

    ContentResolver resolver = context.getContentResolver();
    Log.d(TAG, "resolver: " + resolver);
    Resources res = context.getResources();
    Log.d(TAG, "resources: " + res);

    Cursor cursor = null;
    String feedTitle = "";
    long lastUpdate = 0;

    // Pull out widget title and desired temperature units
    try {
      cursor = resolver.query(appWidgetUri, PROJECTION_APPWIDGETS, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        feedTitle = cursor.getString(COL_TITLE);
        lastUpdate = cursor.getInt(COL_LAST_UPDATED);
        Log.d(TAG, "Reading widget title from cursor: " + feedTitle);

        long deltaMinutes = (System.currentTimeMillis() - lastUpdate) / DateUtils.MINUTE_IN_MILLIS;
        Log.d(TAG, "Delta since last forecast update is " + deltaMinutes + " min");
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    // Find the feed, fill the view and create the itemlist
    Log.d(TAG, "Create the ItemList for the onClick event");
    DefaultItemList itemList = new DefaultItemList();
    itemList.setTitle(feedTitle);
    try {
      Uri allForecastsUri = Uri.withAppendedPath(appWidgetUri, AppWidgets.TWIG_FEED_ITEMS);
      cursor = resolver.query(allForecastsUri, PROJECTION_FEEDS, null, null, 
          FeedItemColumns.FEED_DATE + " DESC," + BaseColumns._ID + " DESC");
      while (cursor != null && cursor.moveToNext()) {
        String title = cursor.getString(COL_FEED_TITLE);
        String content = cursor.getString(COL_FEED_CONTENT);
        String url = cursor.getString(COL_FEED_URL);
        long date = cursor.getLong(COL_FEED_DATE);
        DefaultItem item = new DefaultItem(title, content, url, new Date(date));
        itemList.addItem(item);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    Log.i(TAG, itemList.getNumberOfItems() + " were just loaded from the DB");
    Log.d(TAG, "--------- extras emulator end: " + itemList.getNumberOfItems() + " ----------");

    return itemList;
  }

  public static int getCurrentItemPosition(Context context, int appWidgetId) {
    Log.d(TAG, "--------- get item position begin ----------");
    Log.d(TAG, "Context: " + context + ", " + context.getContentResolver());
    Log.d(TAG, "widget ID: " + appWidgetId);

    Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY), appWidgetId);

    ContentResolver resolver = context.getContentResolver();
    Log.d(TAG, "resolver: " + resolver);

    Cursor cursor = null;
    String feedTitle = null;
    int currentItemPosition = 0;

    // Get the current item position
    try {
      cursor = resolver.query(appWidgetUri, PROJECTION_APPWIDGETS, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        feedTitle = cursor.getString(COL_FEED_TITLE);
        currentItemPosition = cursor.getInt(COL_CURRENT_ITEM_POSITION);
        Log.d(TAG, "Reading widget title from cursor: " + feedTitle);
        Log.d(TAG, "Current item position" + currentItemPosition);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return currentItemPosition;
  }

  public static void updateCurrentItemPosition(Context context, int appWidgetId, int position) {
    Log.d(TAG, "--------- update item position begin ----------");
    Log.d(TAG, "Context: " + context + ", " + context.getContentResolver());
    Log.d(TAG, "widget ID: " + appWidgetId);

    Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY), appWidgetId);

    ContentResolver resolver = context.getContentResolver();
    Log.d(TAG, "resolver: " + resolver);

    ContentValues values = new ContentValues();
    values.put(AppWidgetsColumns.CURRENT_ITEM_POSITION, position);
    int updateRows = resolver.update(appWidgetUri, values, null, null);
    Log.d(TAG, "Updated " + updateRows + " rows for the current item position");
    Log.d(TAG, "--------- update item position end ----------");
  }
}