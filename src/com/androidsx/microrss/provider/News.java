package com.androidsx.microrss.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class News {

    /** Content provider authority for this application. Defined in the manifest file too. */
    public static final String AUTHORITY = "com.androidsx.microrss.provider.NewsProvider";
    
    /** Name of the feed table, whose columns are {@link Feeds}. */
    public static final String TABLE_FEEDS = "feeds";
    
    /** Name of the item table, whose columns are {@link Items}. */
    public static final String TABLE_ITEMS = "items";

    /**
     * Columns for the DB table that contains the feeds.
     */
    public static final class Feeds implements BaseColumns {
        
        /**
         * Content provider for the feeds table.
         * <p>
         * Use {@code ContentUris.withAppendedId(MicroRssContentProvider.Feeds.FEEDS_CONTENT_URI, feedId)} in
         * order to access a single feed.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_FEEDS);

        /**
         * URL of the feed.
         */
        public static final String FEED_URL = "url";

        /**
         * Time of the latest update for the stories in this feed. It is expressed in the local system
         * time in milliseconds, as returned by {@link System#currentTimeMillis()}.
         */
        public static final String LAST_UPDATE = "last_update";

        /**
         * Title of the feed. Usually assigned by the user.
         */
        public static final String TITLE = "title";

        /**
         * Flag that indicates whether this feed is active. Only active feeds are updated and shown in
         * the views. The others are just in "selectable" state.
         */
        public static final String ACTIVE = "active";

        /**
         * Flag that indicates whether this feed comes from the Google Reader account of the user.
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
         */
        public static final String FEED_ID = "feed_id";

        /**
         * URL where this item's extended info can be found. It is only available for some feeds, and
         * allows to insert a web link to it in the detailed view.
         */
        public static final String ITEM_URL = "url";

        /**
         * Title of this story.
         */
        public static final String TITLE = "title";

        /**
         * Main text content of this item.
         */
        public static final String CONTENT = "content";

        /**
         * Integer that sorts the items. They are sorted by using this index in ascending order,
         * starting by 1.
         */
        public static final String POSITION = "position";

        /**
         * Date and time at which this item was generated. It is only available for some feeds, and
         * allows to write something like "3 hours ago" in the header.
         */
        public static final String DATE = "date";
        
        /**
         * Thumbnail URL of the item (if any) 
         */
        public static final String THUMBNAIL_URL = "thumbnail";
    }

}
