package com.androidsx.microrss.db;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.androidsx.microrss.domain.DefaultItem;
import com.androidsx.microrss.domain.DefaultItemList;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;

/**
 * DAO implementation based on SQLite database engine.
 * <p>
 * See {@link #insertItems} to learn how the items are sorted.
 */
public class SqLiteRssItemsDao implements RssItemsDao {

    private static final String TAG = SqLiteRssItemsDao.class.getSimpleName();

    private static final String[] PROJECTION_FEEDS = new String[] {
            ItemColumns.CONTENT,
            ItemColumns.ITEM_URL,
            ItemColumns.DATE,
            ItemColumns.POSITION,
            ItemColumns.TITLE,
            BaseColumns._ID};
    private static final int COL_CONTENT = 0;
    private static final int COL_ITEM_URL = 1;
    private static final int COL_DATE = 2;
    private static final int COL_POSITION = 3;
    private static final int COL_ITEM_TITLE = 4;
    private static final int COL_ID = 5;

    @Override
    public ItemList getItemList(ContentResolver resolver, int appWidgetId) {
        Uri feedWithIdUri = ContentUris.withAppendedId(
                MicroRssContentProvider.FEEDS_CONTENT_URI, appWidgetId);
        // FIXME: there is an extra DB call here, to the feeds table. ItemList shouldn't be aware of the feed
        final String feedTitle = extractFeedTitle(resolver, feedWithIdUri);
        final ItemList itemList = readSortedItemsFromDb(feedWithIdUri, resolver, feedTitle);
        Log.d(TAG, itemList.getNumberOfItems() + " items were loaded from the DB for the widget " + appWidgetId);
        return itemList;
    }
    
    /**
     * Here is an explanation of the way we sort the items. When we receive a
     * list of items from the RSS to be inserted in the DB, we associate every
     * item with an <i>index</i> in this way: the <i>lowest index</i> is given
     * to the last element in the list, and we consecutively assign higher
     * indexes to the others by going backwards in the list. That <i>lowest
     * index</i> is bigger than the largest index that we currently have in the
     * DB. This way, we can safely sort the items in ascending date order by
     * sorting them by descending index order. All clear, right?
     * <p>
     * FIXME: we are inserting one by one, wtf?
     */
    @Override
    public void insertItems(ContentResolver resolver, int appWidgetId,
            ItemList itemsToInsert) {
        Log.d(TAG, "Insert " + itemsToInsert.getNumberOfItems() + " elements into the DB");
        final Uri appWidgetUri = ContentUris.withAppendedId(
                MicroRssContentProvider.FEEDS_CONTENT_URI, appWidgetId);
        final Uri appWidgetForecasts = Uri.withAppendedPath(appWidgetUri,
                MicroRssContentProvider.TABLE_ITEMS);
        
        final int maxIndex = getMaxIndex(appWidgetUri, resolver);
        
        final ContentValues values = new ContentValues();
        for (int i = 0; i < itemsToInsert.getNumberOfItems(); i++) {
            final int index = maxIndex + itemsToInsert.getNumberOfItems() - i;
            Item feedItem = itemsToInsert.getItemAt(i);
            values.put(ItemColumns.CONTENT, feedItem.getContent());
            values.put(ItemColumns.ITEM_URL, feedItem.getURL());
            values.put(ItemColumns.TITLE, feedItem.getTitle());
            values.put(ItemColumns.DATE, feedItem.getPubDate()
                    .getTime());
            values.put(ItemColumns.POSITION, index);
            Log.v(TAG, "Insert item #" + index + ": " + feedItem);
            resolver.insert(appWidgetForecasts, values);
        }
    }
    
    // TODO: this may help: there is a column in BaseColumn named _COUNT :)
    @Override
    public int deleteOldestItems(ContentResolver resolver, int appWidgetId, int numItemsToDelete) {
        if (numItemsToDelete == 0) {
            Log.v(TAG, "No items are to be deleted");
            return 0;
        } else {
            Log.d(TAG, "Attempting to delete the " + numItemsToDelete + " oldest items from the DB");
            final Uri appWidgetUri = ContentUris.withAppendedId(
                    MicroRssContentProvider.FEEDS_CONTENT_URI, appWidgetId);
            final List<Integer> sortedListOfIds = readSortedItemIdsFromDb(appWidgetUri, resolver);
            final List<Integer> idsToDelete = sortedListOfIds.subList(
                    sortedListOfIds.size() - numItemsToDelete,
                    sortedListOfIds.size());
            return deleteItemsById(resolver, appWidgetUri, idsToDelete);
        }
    }
    
    private String extractFeedTitle(ContentResolver resolver, Uri feedWithIdUri) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(feedWithIdUri, new String[] { FeedColumns.TITLE }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex(FeedColumns.TITLE);
                return cursor.getString(titleColumn);
            } else {
                return "";
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    private ItemList readSortedItemsFromDb(Uri appWidgetUri, ContentResolver resolver, String feedTitle) {
        DefaultItemList itemList = new DefaultItemList();
        itemList.setTitle(feedTitle);
        
        Cursor cursor = null;
        try {
            cursor = queryForSortedItems(resolver, appWidgetUri);

            while (cursor != null && cursor.moveToNext()) {
                int index = cursor.getInt(COL_POSITION);
                String content = cursor.getString(COL_CONTENT);
                String title = cursor.getString(COL_ITEM_TITLE);
                String url = cursor.getString(COL_ITEM_URL);
                long date = cursor.getLong(COL_DATE);
                DefaultItem item = new DefaultItem(title, content, url,
                        new Date(date));
                itemList.addItem(item);
                Log.v(TAG, "Read from cursor item #" + index + ": " + item);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return itemList;
    }
    
    /** TODO: merge with #readSortedItemsFromDb */
    private List<Integer> readSortedItemIdsFromDb(Uri appWidgetUri, ContentResolver resolver) {
        final List<Integer> sortedListOfIds = new LinkedList<Integer>();

        Cursor cursor = null;
        try {
            cursor = queryForSortedItems(resolver, appWidgetUri);

            while (cursor != null && cursor.moveToNext()) {
                int id = cursor.getInt(COL_ID);
                sortedListOfIds.add(Integer.valueOf(id));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return sortedListOfIds;
    }
    
    private Cursor queryForSortedItems(ContentResolver resolver, Uri appWidgetUri) {
        Uri allForecastsUri = Uri.withAppendedPath(appWidgetUri,
                MicroRssContentProvider.TABLE_ITEMS);

        return resolver.query(allForecastsUri, PROJECTION_FEEDS,
                null,
                null,
                ItemColumns.POSITION + " DESC," // See #insertItems to understand why
                + ItemColumns.DATE + " DESC,"
                + BaseColumns._ID + " DESC");
    }
    
    private int getMaxIndex(Uri appWidgetUri, ContentResolver resolver) {
        Cursor cursor = null;
        int maxId;
        try {
            Uri allForecastsUri = Uri.withAppendedPath(appWidgetUri,
                    MicroRssContentProvider.TABLE_ITEMS);
            cursor = resolver.query(allForecastsUri, PROJECTION_FEEDS, null,
                    null, null);
            maxId = 0;
            while (cursor != null && cursor.moveToNext()) {
                int appWidgetId= cursor.getInt(COL_POSITION);
                maxId = appWidgetId > maxId ? appWidgetId : maxId;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.v(TAG, "The largest index in the DB is " + maxId);
        return maxId;
    }
    
    private int deleteItemsById(ContentResolver resolver, Uri appWidgetUri, List<Integer> listOfIds) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(BaseColumns._ID + " IN (");
        for (Integer id : listOfIds) {
            whereClause.append(id + ", ");
        }
        whereClause.replace(whereClause.length() - 2, whereClause.length(), ")");
        Log.v(TAG, "WHERE clase to delete items: " + whereClause);
        
        final Uri allForecastsUri = Uri.withAppendedPath(appWidgetUri,
                MicroRssContentProvider.TABLE_ITEMS);
        return resolver.delete(allForecastsUri, whereClause.toString(), null);
    }

}
