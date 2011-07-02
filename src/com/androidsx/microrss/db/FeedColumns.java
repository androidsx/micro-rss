package com.androidsx.microrss.db;

/**
 * Columns for the DB table that contains 
 */
public class FeedColumns {
    /**
     * URL of the selected RSS.
     */
    public static final String FEED_URL = "feedUrl";
    
    /**
     * The type of view for the webview that opens the feed item, usually
     * defaults to {@link #WEBVIEW_TYPE_DEFAULT}.
     */
    @Deprecated
    public static final String WEBVIEW_TYPE = "webviewType";
    public static final int WEBVIEW_TYPE_SINGLE = 1;
    public static final int WEBVIEW_TYPE_LIST = 2;
    public static final int WEBVIEW_TYPE_EXPANDABLE = 3;
    public static final int WEBVIEW_TYPE_DEFAULT = WEBVIEW_TYPE_LIST;

    /**
     * Last system time when feed items for this widget were updated in millis,
     * usually set by {@link System#currentTimeMillis()} when they are downloaded.
     */
    public static final String LAST_UPDATED = "lastUpdated";

  /**
   * Interval to wait between background widget updates. This does not
   * exactly mean that we will download and parse new feeds every this period.
   */
    @Deprecated
    public static final String UPDATE_INTERVAL = "updateInterval";
    
    // FIXME: add several extra columns, just in case. or what? this is not a widget
}
