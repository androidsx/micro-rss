package com.androidsx.microrss.view;

/**
 * Keys of the extras that are passed around between the views to handle the navigation.
 */
public interface NavigationExtras {

    /**
     * Key of the extra that contains a sorted integer array with all the IDs for the items in this
     * level.
     * 
     * @return key of the extra with all the IDs
     */
    String getAllIdsKey();

    /**
     * Key of the extra that contains the index of the item (that is, the position in the array
     * given by {@link #getAllIdsKey()}) that the calling view is expected to display.
     * 
     * @return key of the extra with the index (position) of the current item
     */
    String getCurrentIndexKey();
}

class FeedNavigationExtras implements NavigationExtras {

    @Override
    public String getAllIdsKey() {
        return "feed-ids";
    }

    @Override
    public String getCurrentIndexKey() {
        return "feed-index";
    }
}

class StoryNavigationExtras implements NavigationExtras {

    @Override
    public String getAllIdsKey() {
        return "story-ids";
    }

    @Override
    public String getCurrentIndexKey() {
        return "story-index";
    }
}
