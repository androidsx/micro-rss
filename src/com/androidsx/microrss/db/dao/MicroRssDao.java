package com.androidsx.microrss.db.dao;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.androidsx.microrss.db.ItemColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.domain.DefaultItem;
import com.androidsx.microrss.domain.Item;

public class MicroRssDao {

    private final Activity activity;

    public MicroRssDao(Activity activity) {
        this.activity = activity;
    }

    protected void persistFeed() {
        throw new UnsupportedOperationException();
    }

    protected void updateFeed() {
        throw new UnsupportedOperationException();
    }

    protected void persistStories() {
        throw new UnsupportedOperationException();
    }

    protected void findFeed() {
        throw new UnsupportedOperationException();
    }

    public List<Item> findStories(int feedId) throws DataNotFoundException {
        final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId);
        final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, MicroRssContentProvider.TABLE_ITEMS);
        final String[] projection = new String[] { ItemColumns.TITLE, ItemColumns.CONTENT,
                ItemColumns.ITEM_URL, ItemColumns.DATE };
        final Cursor cursor = activity.managedQuery(allStoriesUriInFeedUri, projection, null, null,
                ItemColumns.POSITION + " DESC");

        List<Item> items = new LinkedList<Item>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(itemFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return items;
    }
    
    /** @return sorted IDs of all the stories in a feed, given its ID */
    public int[] findStoryIds(int feedId) throws DataNotFoundException {
        final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId);
        final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, MicroRssContentProvider.TABLE_ITEMS);
        final String[] projection = new String[] { BaseColumns._ID };
        final Cursor cursor = activity.managedQuery(allStoriesUriInFeedUri, projection, null, null,
                ItemColumns.POSITION + " DESC");

        List<Integer> ids = new LinkedList<Integer>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
            } while (cursor.moveToNext());
        }
        return toIntArray(ids);
    }

    /** @return a single story, given its ID */
    public Item findStory(int id) throws DataNotFoundException {
        final Uri anItemUri = ContentUris.withAppendedId(MicroRssContentProvider.ITEMS_CONTENT_URI, id);
        final String[] projection = new String[] { ItemColumns.TITLE, ItemColumns.CONTENT, ItemColumns.ITEM_URL, ItemColumns.DATE };
        final Cursor cursor = activity.managedQuery(anItemUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return itemFromCursor(cursor);
        } else {
            throw new DataNotFoundException("No story was found for the id " + id);
        }
    }

    private static Item itemFromCursor(Cursor cursor) {
        return new DefaultItem(cursor.getString(cursor.getColumnIndex(ItemColumns.TITLE)),
                cursor.getString(cursor.getColumnIndex(ItemColumns.CONTENT)),
                cursor.getString(cursor.getColumnIndex(ItemColumns.ITEM_URL)), new Date(
                        cursor.getLong(cursor.getColumnIndex(ItemColumns.DATE))));
    }

    private static int[] toIntArray(List<Integer> list) {
        final int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list) {
            ret[i++] = e.intValue();
        }
        return ret;
    }
}
