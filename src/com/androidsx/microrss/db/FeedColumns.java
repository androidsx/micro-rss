package com.androidsx.microrss.db;

/**
 * Columns for the DB table that contains the feeds.
 */
public class FeedColumns {

    /**
     * URL of the feed.
     */
    public static final String FEED_URL = "url";

    /**
     * Last system time when stories for this feed. It is expressend in the local system time in
     * milliseconds, as returned by {@link System#currentTimeMillis()}.
     */
    public static final String LAST_UPDATE = "last_update";
}
