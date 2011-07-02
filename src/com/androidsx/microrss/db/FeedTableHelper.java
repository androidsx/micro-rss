package com.androidsx.microrss.db;


public class FeedTableHelper {

    /**
     * Directory twig to request all items for a specific widget.
     */
    public static final String TWIG_FEED_ITEMS = "forecasts";

    /**
     * Directory twig to request the forecast nearest the requested time.
     * <p>
     * TODO: Check whether this actually works
     */
    public static final String TWIG_FORECAST_AT = "forecast_at";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/appwidget";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/appwidget";
}
