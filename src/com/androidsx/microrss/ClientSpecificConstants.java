package com.androidsx.microrss;

/**
 * Popurri of constants for this application.
 * <p>
 * The references to this class should be minimized as much as possible. 
 */
public class ClientSpecificConstants {
    
    public static final String FLURRY_APP_KEY = "2UNTJQR1DKS88WR9UR2B";
    
    public static class ActionIds {
      public static class LargeWidget {
        static final String SHOW_NEXT_ITEM_ACTION = "com.androidsx.microrss.large.SHOW_NEXT_ITEM";
        static final String SHOW_PREV_ITEM_ACTION = "com.androidsx.microrss.large.SHOW_PREV_ITEM";
        static final String UPDATE_FEED_ACTION = "com.androidsx.microrss.large.UPDATE_FEED";
      }
      
      public static class MedWidget {
        static final String SHOW_NEXT_ITEM_ACTION = "com.androidsx.microrss.SHOW_NEXT_ITEM";
        static final String SHOW_PREV_ITEM_ACTION = "com.androidsx.microrss.SHOW_PREV_ITEM";
        static final String UPDATE_FEED_ACTION = "com.androidsx.microrss.UPDATE_FEED";
      }
      
      public static class TinyWidget {
        static final String UPDATE_FEED_ACTION = "com.androidsx.microrss.tiny.UPDATE_FEED";
        static final String NOT_SUPPORTED_ACTION = "com.androidsx.microrss.tiny.NOT_SUPPORTED";
      }
    }

}
