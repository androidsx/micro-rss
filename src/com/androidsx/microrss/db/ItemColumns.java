package com.androidsx.microrss.db;

/**
 * Columns for the DB table that contains the stories.
 */
public class ItemColumns {

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
