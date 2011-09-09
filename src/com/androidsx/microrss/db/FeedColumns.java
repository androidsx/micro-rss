package com.androidsx.microrss.db;

import android.provider.BaseColumns;

/**
 * Columns for the DB table that contains the feeds.
 */
public class FeedColumns implements BaseColumns {

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
