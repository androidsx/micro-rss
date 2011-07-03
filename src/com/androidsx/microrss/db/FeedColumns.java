package com.androidsx.microrss.db;

/**
 * Columns for the DB table that contains the feeds.
 */
public class FeedColumns {
    /**
     * URL of the selected RSS.
     */
    public static final String FEED_URL = "feedUrl";
    
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
}
