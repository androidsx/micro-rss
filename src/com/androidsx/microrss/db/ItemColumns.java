package com.androidsx.microrss.db;

import android.appwidget.AppWidgetManager;

/**
 * Columns for the DB table that contains the stories.
 */
public class ItemColumns {

    /**
     * Foreign key to the feed that this story belongs to.
     */
    public static final String FEED_ID = "feed_id";

    /**
     * Integer that sorts the items. They are sorted by using this index in ascending order,
     * starting by 1.
     */
    public static final String POSITION = "position";

    /**
     * Title of this feed item.
     * 
     * @deprecated why the fuck is this here? move to the feeds table
     */
    @Deprecated
    public static final String FEED_TITLE = "feedTitle";

    /**
     * Main text content of this item.
     */
    public static final String CONTENT = "content";

    /**
     * URL where this item's extended info can be found. It is only available for some feeds, and
     * allows to insrt a web link to it in the detailed view.
     * 
     * @deprecated why the fuck is this here?
     */
    @Deprecated
    public static final String FEED_URL = "feedUrl";

    /**
     * Date and time at which this item was generated. It is only available for some feeds, and
     * allows to write something like "3 hours ago" in the header.
     */
    public static final String DATE = "date";

    /**
     * Web link where more details can be found about this feed item.
     */
    public static final String URL = "url";
}
