package com.androidsx.microrss.db.dao;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.ItemColumns;
import com.androidsx.microrss.db.MicroRssContentProvider;
import com.androidsx.microrss.domain.DefaultFeed;
import com.androidsx.microrss.domain.DefaultItem;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;

public class MicroRssDao {
    private final ContentResolver contentResolver;

    public MicroRssDao(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void updateFeed(Feed feed) {
        ContentValues values = new ContentValues();
        values.put(BaseColumns._ID, feed.getId());
        values.put(FeedColumns.LAST_UPDATE, feed.getLastModificationDate().getTime()); // FIXME: sure?
        values.put(FeedColumns.TITLE, feed.getTitle());
        values.put(FeedColumns.FEED_URL, feed.getURL());
        values.put(FeedColumns.ACTIVE, feed.isActive());

        final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feed.getId());
        
        contentResolver.update(aFeedUri, values, null, null);
    }

    public void updateFeedActive(Feed feed, boolean active) {
        Feed updatedFeed = new DefaultFeed(feed.getId(), feed.getTitle(), feed.getURL(), active, feed.getLastModificationDate());
        updateFeed(updatedFeed);
    }
    
    public int[] findAllFeedIds() {
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
    
    public int[] findActiveFeedIds() {
        Cursor cursor = null;
        try {
            final Uri allFeedsUri = MicroRssContentProvider.FEEDS_CONTENT_URI;
            final String[] projection = new String[] { BaseColumns._ID };
            cursor = contentResolver.query(allFeedsUri, projection, FeedColumns.ACTIVE + " = 1", null,
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
    
    public List<Feed> findFeeds() {
        return findFeeds(null);
    }
    
    public List<Feed> findActiveFeeds() {
        return findFeeds(FeedColumns.ACTIVE + " = 1");
    }
    
    public List<Feed> findGoogleReaderFeeds() {
        return findFeeds(FeedColumns.G_READER + " = 1");
    }
    
    public List<Feed> findSampleFeeds() {
        return findFeeds(FeedColumns.G_READER + " <> 1");
    }
    
    
    private List<Feed> findFeeds(String selection) {
        Cursor cursor = null;
        try {
            final Uri allFeedsUri = MicroRssContentProvider.FEEDS_CONTENT_URI;
            final String[] projection = new String[] { BaseColumns._ID, FeedColumns.TITLE,
                    FeedColumns.FEED_URL, FeedColumns.ACTIVE, FeedColumns.LAST_UPDATE };
            cursor = contentResolver.query(allFeedsUri, projection, selection, null,
                    BaseColumns._ID + " ASC"); // FIXME: sort by feed position instead
            
            List<Feed> feeds = new LinkedList<Feed>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    feeds.add(feedFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            return feeds;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    public Feed findFeed(int id) {
        Cursor cursor = null;
        try {
            final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, id);
            final String[] projection = new String[] { BaseColumns._ID, FeedColumns.FEED_URL, FeedColumns.TITLE,
                    FeedColumns.LAST_UPDATE, FeedColumns.ACTIVE };
            cursor = contentResolver.query(aFeedUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return feedFromCursor(cursor);
            } else {
                throw new IllegalArgumentException("No feed was found for the id " + id);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    public List<Item> findStories(int feedId) {
        Cursor cursor = null;
        try {
            final Uri aFeedUri = ContentUris.withAppendedId(MicroRssContentProvider.FEEDS_CONTENT_URI, feedId);
            final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, MicroRssContentProvider.TABLE_ITEMS);
            final String[] projection = new String[] { BaseColumns._ID, ItemColumns.TITLE, ItemColumns.CONTENT,
                    ItemColumns.ITEM_URL, ItemColumns.THUMBNAIL_URL, ItemColumns.DATE };
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
    public int[] findStoryIds(int feedId) {
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
    public Item findStory(int id) {
        Cursor cursor = null;
        try {
            final Uri anItemUri = ContentUris.withAppendedId(MicroRssContentProvider.ITEMS_CONTENT_URI, id);
            final String[] projection = new String[] { ItemColumns.TITLE, ItemColumns.CONTENT,
                    ItemColumns.ITEM_URL, ItemColumns.THUMBNAIL_URL, ItemColumns.DATE };
            cursor = contentResolver.query(anItemUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return itemFromCursor(cursor);
            } else {
                throw new IllegalArgumentException("No story was found for the id " + id);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static Item itemFromCursor(Cursor cursor) {
        return new DefaultItem(
                cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                cursor.getString(cursor.getColumnIndex(ItemColumns.TITLE)),
                cursor.getString(cursor.getColumnIndex(ItemColumns.CONTENT)),
                cursor.getString(cursor.getColumnIndex(ItemColumns.ITEM_URL)),
                new Date(cursor.getLong(cursor.getColumnIndex(ItemColumns.DATE))),
                cursor.getString(cursor.getColumnIndex(ItemColumns.THUMBNAIL_URL)));
    }
    
    private static Feed feedFromCursor(Cursor cursor) {
        return new DefaultFeed(
                cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                cursor.getString(cursor.getColumnIndex(FeedColumns.TITLE)),
                cursor.getString(cursor.getColumnIndex(FeedColumns.FEED_URL)),
                cursor.getInt(cursor.getColumnIndex(FeedColumns.ACTIVE)) == 1,
                new Date(cursor.getLong(cursor.getColumnIndex(FeedColumns.LAST_UPDATE))));
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
