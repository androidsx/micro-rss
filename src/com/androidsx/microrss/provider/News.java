package com.androidsx.microrss.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/** 
 * Table and column definitions to access WIMM News Reader content provider 
 * */
public final class News {

    /** Content provider authority for this application. */
    public static final String AUTHORITY = "com.androidsx.microrss.provider.NewsProvider";
    
    /** Name of the category table, whose columns represent {@link Categories}. */
    public static final String TABLE_CATEGORIES = "categories";
    
    /** Name of the feed table, whose columns represent {@link Feeds}. */
    public static final String TABLE_FEEDS = "feeds";
    
    /** Name of the item table, whose columns represent {@link Items}. */
    public static final String TABLE_ITEMS = "items";

    public static final class Categories implements BaseColumns {
        
        /**
         * Content provider for the categories table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_CATEGORIES);
        
        /**
         * Name of this category, in English.
         * <p>Type: TEXT</p>
         */
        public static final String NAME = "name";
    }
    
    /**
     * Columns for the DB table that contains the feeds.
     */
    public static final class Feeds implements BaseColumns {
        
        /**
         * Content provider for the feeds table.
         * 
         * <p>Use {@code ContentUris.withAppendedId(News.Feeds.FEEDS_CONTENT_URI, feedId)} in
         * order to access a single feed.</p>
         * 
         * <p>Use {@code final Uri aFeedUri = ContentUris.withAppendedId(News.Feeds.CONTENT_URI, feedId);
         *   final Uri allStoriesUriInFeedUri = Uri.withAppendedPath(aFeedUri, News.TABLE_ITEMS);} 
         *  in order to access to all items of a single feed.</p>
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FEEDS);
        
        /**
         * Foreign key to the category that this feed belongs to.
         * <p>Type: INTEGER</p>
         */
        public static final String CATEGORY_ID = "category_id";

        /**
         * URL of the feed.
         * <p>Type: TEXT</p>
         */
        public static final String FEED_URL = "url";

        /**
         * Time of the latest update for the stories in this feed. It is expressed in the local system
         * time in milliseconds, as returned by {@link System#currentTimeMillis()}.
         * <p>Type: INTEGER (long; millis since epoch)</p>
         */
        public static final String LAST_UPDATE = "last_update";

        /**
         * Title of the feed. Usually assigned by the user.
         * <p>Type: TEXT</p>
         */
        public static final String TITLE = "title";

        /**
         * Flag that indicates whether this feed is active. Only active feeds are updated and shown in
         * the views. The others are just in "selectable" state.
         * <p>Type: INTEGER (boolean)</p>
         */
        public static final String ACTIVE = "active";

        /**
         * Flag that indicates whether this feed comes from the Google Reader account of the user.
         * <p>Type: INTEGER (boolean)</p>
         */
        public static final String G_READER = "g_reader";
    }

    /**
     * Columns for the DB table that contains the stories.
     */
    public static final class Items implements BaseColumns {

        /** Content provider for the items table. */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ITEMS);
        
        /**
         * Foreign key to the feed that this story belongs to.
         * <p>Type: INTEGER (boolean)</p>
         */
        public static final String FEED_ID = "feed_id";

        /**
         * URL where this item's extended info can be found. It is only available for some feeds, and
         * allows to insert a web link to it in the detailed view.
         * <p>Type: TEXT</p>
         */
        public static final String ITEM_URL = "url";

        /**
         * Title of this story.
         * <p>Type: TEXT</p>
         */
        public static final String TITLE = "title";

        /**
         * Main text content of this item.
         * <p>Type: TEXT</p>
         */
        public static final String CONTENT = "content";

        /**
         * Integer that sorts the items. They are sorted by using this index in ascending order,
         * starting by 1.
         * <p>Type: INTEGER</p>
         */
        public static final String POSITION = "position";

        /**
         * Date and time at which this item was generated. It is only available for some feeds, and
         * allows to write something like "3 hours ago" in the header.
         * <p>Type: INTEGER (long; millis since epoch)</p>
         */
        public static final String DATE = "date";
        
        /**
         * Thumbnail URL of the item (if any) 
         * <p>Type: TEXT</p>
         */
        public static final String THUMBNAIL_URL = "thumbnail";
    }

}
