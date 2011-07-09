package com.androidsx.microrss.db.dao;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.ItemColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.domain.DefaultItem;
import com.androidsx.microrss.domain.Item;

public class MicroRssDao {
    private final ContentResolver contentResolver;

    public MicroRssDao(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
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

    public int[] findFeedIds() {
        Cursor cursor = null;
        try {
            final Uri allFeedsUri = MicroRssContentProvider.FEEDS_CONTENT_URI;
            final String[] projection = new String[] { BaseColumns._ID };
            cursor = contentResolver.query(allFeedsUri, projection, null, null,
                    BaseColumns._ID + " ASC"); // FIXME: sort by feed position instead
    
            List<Integer> ids = new LinkedList<Integer>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
                } while (cursor.moveToNext());
            }
            return toIntArray(ids);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    protected void findFeed() {
        throw new UnsupportedOperationException();
    }

    public List<Item> findStories(int feedId) throws DataNotFoundException {
        Cursor cursor = null;
        try {
            final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId);
            final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, MicroRssContentProvider.TABLE_ITEMS);
            final String[] projection = new String[] { ItemColumns.TITLE, ItemColumns.CONTENT,
                    ItemColumns.ITEM_URL, ItemColumns.DATE };
            cursor = contentResolver.query(allStoriesUriInFeedUri, projection, null, null,
                    ItemColumns.POSITION + " DESC");
    
            List<Item> items = new LinkedList<Item>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    items.add(itemFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            return items;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    /** @return sorted IDs of all the stories in a feed, given its ID */
    public int[] findStoryIds(int feedId) throws DataNotFoundException {
        Cursor cursor = null;
        try {
            final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId);
            final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, MicroRssContentProvider.TABLE_ITEMS);
            final String[] projection = new String[] { BaseColumns._ID };
            cursor = contentResolver.query(allStoriesUriInFeedUri, projection, null, null,
                    ItemColumns.POSITION + " DESC");
    
            List<Integer> ids = new LinkedList<Integer>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));
                } while (cursor.moveToNext());
            }
            return toIntArray(ids);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /** @return a single story, given its ID */
    // TODO: maybe instead of throwing an exception, an empty item that says "we're sorry, no item here"
    // TODO: and consider making it unchecked
    public Item findStory(int id) throws DataNotFoundException {
        Cursor cursor = null;
        try {
            final Uri anItemUri = ContentUris.withAppendedId(MicroRssContentProvider.ITEMS_CONTENT_URI, id);
            final String[] projection = new String[] { ItemColumns.TITLE, ItemColumns.CONTENT, ItemColumns.ITEM_URL, ItemColumns.DATE };
            cursor = contentResolver.query(anItemUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return itemFromCursor(cursor);
            } else {
                throw new DataNotFoundException("No story was found for the id " + id);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
