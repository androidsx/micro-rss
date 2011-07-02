package com.androidsx.microrss.webservice;

import java.util.List;

import com.androidsx.microrss.domain.Item;

interface RssSource {

    /**
     * Queries the given URL and parses the returned data into a list of
     * {@link Item} objects.
     * <p>
     * This is a blocking call while waiting for the webservice to return.
     */
    List<Item> getRssItems(String rssUrl, int maxNumberOfItems)
            throws FeedProcessingException;

}
