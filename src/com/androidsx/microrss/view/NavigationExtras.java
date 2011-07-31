package com.androidsx.microrss.view;

/**
 * Keys of the extras that are passed around between the views to handle the navigation.
 */
public interface NavigationExtras {
  
    /**
     * Key of the extra that contains the id of the item that the calling view is expected to display.
     * 
     * @return key of the extra with the unique identifier of the current item
     */
    String getCurrentIdKey();
}

class FeedNavigationExtras implements NavigationExtras {

    @Override
    public String getCurrentIdKey() {
        return "feed-id";
    }
}

class StoryNavigationExtras implements NavigationExtras {

    @Override
    public String getCurrentIdKey() {
        return "story-id";
    }
}
