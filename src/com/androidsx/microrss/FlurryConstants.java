package com.androidsx.microrss;

public class FlurryConstants {
    public static final String EVENT_NEW_INSTALL = "New install";
    
    public static final String EVENT_NEW_WIDGET = "Add new widget";
    public static final String PARAM_CONFIGURE_FEED_URL = "Feed URL";

    /**
     * Amount of time, in miliseconds, that the application can stay inactive that will
     * still be considered to be alive in the <i>same session</i>. Since the user might
     * stay for a while reading an item, we'll make it bigger than the default, which is
     * 10 seconds.
     */
    public static final long SESSION_MILLIS = 60 * 1000;

    public static final String ERROR_ID_PASS_DATA = "pass_data";
    public static final String ERROR_ID_PACKAGE_MANAGER = "package_manager";
    public static final String ERROR_ID_FORCE_UPDATE = "force_update";
    public static final String ERROR_ID_NEXT_PREVIOUS = "next_previous_buttons";
    public static final String ERROR_ID_UPDATE_SERVICE = "update_service";
    public static final String ERROR_ID_INFO_PROVIDER = "info_provider";

    public static final String FLURRY_APP_KEY = "DBNMS9H7I3Z4PBMIJ7VW";
}
