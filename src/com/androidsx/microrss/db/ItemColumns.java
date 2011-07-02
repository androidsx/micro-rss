package com.androidsx.microrss.db;

import android.appwidget.AppWidgetManager;

/**
 * Columns for the DB table that contains the items.
 */
public class ItemColumns {
    
    /**
     * The parent {@link AppWidgetManager#EXTRA_APPWIDGET_ID} of this
     * feed item.
     */
    public static final String FEED_ID = "feedId";
    
    /**
     * Integer that sorts the items. They are sorted by using this index in
     * ascending order, starting by 1.
     */
    public static final String ITEM_INDEX = "itemIndex";
    
    /**
     * Title of this feed item.
     * @deprecated why the fuck is this here?
     */
    @Deprecated
    public static final String FEED_TITLE = "feedTitle";
    
    /**
     * Main text content of this item. 
     */
    public static final String ITEM_CONTENT = "itemContent";
    
    /**
     * URL where this item's extended info can be found. It is only
     * available for some feeds, and allows to insrt a web link to it in the
     * detailed view.
     * @deprecated why the fuck is this here?
     */
    @Deprecated
    public static final String FEED_URL = "feedUrl";
    
    /**
     * Date and time at which this item was generated. It is only available
     * for some feeds, and allows to write something like "3 hours ago" in
     * the header.
     */
    public static final String ITEM_DATE = "itemDate";

    /**
     * Web link where more details can be found about this feed item.
     */
    public static final String ITEM_URL = "url";

}