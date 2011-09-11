package com.androidsx.microrss.db.dao;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.Log;

import com.androidsx.microrss.cache.CacheImageManager;
import com.androidsx.microrss.domain.DefaultFeed;
import com.androidsx.microrss.domain.DefaultItem;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.provider.News;
import com.androidsx.microrss.provider.News.Categories;
import com.androidsx.microrss.provider.News.Feeds;
import com.androidsx.microrss.provider.News.Items;

public class MicroRssDao {
    private final ContentResolver contentResolver;

    public MicroRssDao(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    String findCategoryById(int id) {
        Cursor cursor = null;
        try {
            final Uri aCategoryUri = ContentUris.withAppendedId(News.Categories.CONTENT_URI, id);
            final String[] projection = new String[] { Categories._ID, Categories.NAME };
            cursor = contentResolver.query(aCategoryUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(Categories.NAME));
            } else {
                throw new IllegalArgumentException("No category was found for the id " + id);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    Integer findCategoryByName(String categoryName) {
        Cursor cursor = null;
        try {
            final String[] projection = new String[] { Categories._ID, Categories.NAME };
            cursor = contentResolver.query(News.Categories.CONTENT_URI, projection, Categories.NAME
                    + " = ?", new String[] { categoryName }, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(Categories._ID));
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    int persistCategory(String categoryName) {
        ContentValues values = new ContentValues();
        values.put(Categories.NAME, categoryName);
        
        Uri uri = contentResolver.insert(News.Categories.CONTENT_URI, values);
        return Integer.valueOf(uri.getPathSegments().get(1));
    }
    
    public void persistFeed(Context context, String category, String title, String feedUrl,
            boolean active, boolean gReader) {
        persistFeedInternal(context, category, title, feedUrl, active, gReader);
    }
    
    /**
     * @deprecated Do this logic in the caller
     */
    @Deprecated
    public void persistFeedCheckingUniqueKey(Context context, String category, String title, String feedUrl, boolean active,
            boolean gReader) {
        if (findFeedsByUrl(feedUrl).size() == 0) {
            persistFeedInternal(context, category, title, feedUrl, active, gReader);
        } else {
            Log.w("MicroRssDao", "This feed: " + title + " already exists in the database. We skip it.");
        }
    }

    private void persistFeedInternal(Context context, String category, String title, String feedUrl, boolean active,
            boolean gReader) {
        Integer categoryId = findCategoryByName(category);
        if (categoryId == null) {
            categoryId = persistCategory(category);
        }
        
        ContentValues values = new ContentValues();
        values.put(Feeds.CATEGORY_ID, categoryId);
        values.put(Feeds.LAST_UPDATE, -1);
        values.put(Feeds.TITLE, title);
        values.put(Feeds.FEED_URL, feedUrl);
        values.put(Feeds.ACTIVE, active);
        values.put(Feeds.G_READER, gReader);
        
        ContentResolver resolver = context.getContentResolver();
        resolver.insert(News.Feeds.CONTENT_URI, values);
    }
    
    public void updateFeed(Feed feed) {
        ContentValues values = new ContentValues();
        values.put(Feeds._ID, feed.getId());
        values.put(Feeds.LAST_UPDATE, feed.getLastModificationDate().getTime()); // FIXME: sure?
        values.put(Feeds.TITLE, feed.getTitle());
        values.put(Feeds.FEED_URL, feed.getURL());
        values.put(Feeds.ACTIVE, feed.isActive());

        final Uri aFeedUri = ContentUris.withAppendedId(News.Feeds.CONTENT_URI, feed.getId());
        
        contentResolver.update(aFeedUri, values, null, null);
    }

    /**
     * Updates the feed activating or disabling it. 
     * 
     * @param active if false it will delete all the item feeds and thumbnails associated
     */
    public void updateFeedActive(Feed feed, boolean active, CacheImageManager cacheManager) {
        Feed updatedFeed = new DefaultFeed(feed.getId(), feed.getTitle(), feed.getURL(), active, feed.getLastModificationDate(), feed.getCategory());
        updateFeed(updatedFeed);

        if (active == false) {
            deleteAllItems(feed, cacheManager);
        }
    }

    public int[] findAllFeedIds() {
        Cursor cursor = null;
        try {
            final Uri allFeedsUri = News.Feeds.CONTENT_URI;
            final String[] projection = new String[] { Feeds._ID };
            cursor = contentResolver.query(allFeedsUri, projection, null, null,
                    Feeds._ID + " ASC"); // FIXME: sort by feed position instead
    
            List<Integer> ids = new LinkedList<Integer>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getInt(cursor.getColumnIndex(Feeds._ID)));
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
            final Uri allFeedsUri = News.Feeds.CONTENT_URI;
            final String[] projection = new String[] { Feeds._ID };
            cursor = contentResolver.query(allFeedsUri, projection, Feeds.ACTIVE + " = 1", null,
                    Feeds._ID + " ASC"); // FIXME: sort by feed position instead
    
            List<Integer> ids = new LinkedList<Integer>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getInt(cursor.getColumnIndex(Feeds._ID)));
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
        return findFeeds(Feeds.ACTIVE + " = 1");
    }
    
    public List<Feed> findGoogleReaderFeeds() {
        return findFeeds(Feeds.G_READER + " = 1");
    }
    
    public List<Feed> findSampleFeeds() {
        return findFeeds(Feeds.G_READER + " <> 1");
    }

    public List<Feed> findFeedsByUrl(String url) {
        return findFeeds(Feeds.FEED_URL + " = " + DatabaseUtils.sqlEscapeString(url));
    }
    
    
    private List<Feed> findFeeds(String selection) {
        Cursor cursor = null;
        try {
            final Uri allFeedsUri = News.Feeds.CONTENT_URI;
            final String[] projection = new String[] { Feeds._ID, Feeds.CATEGORY_ID, Feeds.TITLE,
                    Feeds.FEED_URL, Feeds.ACTIVE, Feeds.LAST_UPDATE };
            cursor = contentResolver.query(allFeedsUri, projection, selection, null,
                    Feeds._ID + " ASC"); // FIXME: sort by feed position instead
            
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
            final Uri aFeedUri = ContentUris.withAppendedId(News.Feeds.CONTENT_URI, id);
            final String[] projection = new String[] { Feeds._ID, Feeds.CATEGORY_ID, Feeds.FEED_URL, Feeds.TITLE,
                    Feeds.LAST_UPDATE, Feeds.ACTIVE };
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
            final Uri aFeedUri = ContentUris.withAppendedId(News.Feeds.CONTENT_URI, feedId);
            final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, News.TABLE_ITEMS);
            final String[] projection = new String[] { Items._ID, Items.TITLE, Items.CONTENT,
                    Items.ITEM_URL, Items.THUMBNAIL_URL, Items.DATE };
            cursor = contentResolver.query(allStoriesUriInFeedUri, projection, null, null,
                    Items.POSITION + " DESC");
    
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
            final Uri aFeedUri = ContentUris.withAppendedId(News.Feeds.CONTENT_URI, feedId);
            final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, News.TABLE_ITEMS);
            final String[] projection = new String[] { Items._ID };
            cursor = contentResolver.query(allStoriesUriInFeedUri, projection, null, null,
                    Items.POSITION + " DESC");
    
            List<Integer> ids = new LinkedList<Integer>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getInt(cursor.getColumnIndex(Items._ID)));
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
            final Uri anItemUri = ContentUris.withAppendedId(News.Items.CONTENT_URI, id);
            final String[] projection = new String[] { Items.TITLE, Items.CONTENT,
                    Items.ITEM_URL, Items.THUMBNAIL_URL, Items.DATE };
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
    
    /** 
     * Delete all the items for the given feed, together with the associated
     * thumbnails
     */
    private void deleteAllItems(Feed feed, CacheImageManager cacheManager) {
        List<Item> itemsToDelete = findStories(feed.getId());
        
        for (Item item : itemsToDelete) {
            if (!item.getThumbnail().equals("")) {
                cacheManager.deleteImage(cacheManager.getFilenameForUrl(item.getThumbnail()));
            }
        }
        final Uri feedUri = ContentUris.withAppendedId(
        News.Feeds.CONTENT_URI, feed.getId());
        final Uri allItemsFeedUri = Uri.withAppendedPath(feedUri,
              News.TABLE_ITEMS);
        contentResolver.delete(allItemsFeedUri, null, null);
    }

    private static Item itemFromCursor(Cursor cursor) {
        return new DefaultItem(
                cursor.getInt(cursor.getColumnIndex(Items._ID)),
                cursor.getString(cursor.getColumnIndex(Items.TITLE)),
                cursor.getString(cursor.getColumnIndex(Items.CONTENT)),
                cursor.getString(cursor.getColumnIndex(Items.ITEM_URL)),
                new Date(cursor.getLong(cursor.getColumnIndex(Items.DATE))),
                cursor.getString(cursor.getColumnIndex(Items.THUMBNAIL_URL)));
    }
    
    private Feed feedFromCursor(Cursor cursor) {
        String categoryName = findCategoryById(cursor.getInt(cursor.getColumnIndex(Feeds.CATEGORY_ID)));
        return new DefaultFeed(
                cursor.getInt(cursor.getColumnIndex(Feeds._ID)),
                cursor.getString(cursor.getColumnIndex(Feeds.TITLE)),
                cursor.getString(cursor.getColumnIndex(Feeds.FEED_URL)),
                cursor.getInt(cursor.getColumnIndex(Feeds.ACTIVE)) == 1,
                new Date(cursor.getLong(cursor.getColumnIndex(Feeds.LAST_UPDATE))),
                categoryName);
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
