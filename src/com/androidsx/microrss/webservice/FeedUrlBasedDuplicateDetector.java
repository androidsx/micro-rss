package com.androidsx.microrss.webservice;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;
import com.androidsx.microrss.provider.News.Items;

class FeedUrlBasedDuplicateDetector implements DuplicateDetector {

    private enum Duplicated { TRUE, FALSE, NOT_SURE }
    
    private static final String TAG = FeedUrlBasedDuplicateDetector.class.getSimpleName();
    protected static final String FULLY_QUALIFIED_URL_REGEX = "^(https?://)" 
        + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //user@ 
        + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP- 199.194.52.184 
        + "|" // allows either IP or domain 
        + "([0-9a-z_!~*'()-]+\\.)*" // tertiary domain(s)- www., optional
        + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // second level domain, compulsory
        + "[a-z]{2,6})" // first level domain- .com or .museum 
        + "(:[0-9]{1,4})?" // port number- :80, optional
        + "/" // a slash *is* required
        + ".+" // now we require a relative URL to the feed *item*
        + "$";

    private final ContentResolver resolver;
    private final Uri feedForecasts;

    /**
     * Constructs a new duplicate detector instance.
     * 
     * @param contentResolver to access the DB. TODO: should be removed in favor of a DAO interface
     * @param feedForecasts. TODO: should be removed in favor of a DAO interface
     */
    public FeedUrlBasedDuplicateDetector(ContentResolver resolver, Uri feedForecasts) {
        this.resolver = resolver;
        this.feedForecasts = feedForecasts;
    }

    @Override
    public boolean isDuplicated(Item item, ItemList itemList) {
        if (duplicatedByFeedUrl(item.getURL()) == Duplicated.TRUE) {
            return true;
        } else if (duplicatedByFeedUrl(item.getURL()) == Duplicated.FALSE) {
            return false;
        } else {
            return false; // TODO: should keep on searching
        }
    }
    
    protected Duplicated duplicatedByFeedUrl(String url) {
        Duplicated thereAreItemsWithThisUrl;
        Cursor cursor = null;
        try {
            cursor = resolver.query(feedForecasts, null, 
                    Items.ITEM_URL + " = \"" + url + "\"", null, null);
            if (cursor == null || cursor.moveToFirst() == false) {
                thereAreItemsWithThisUrl = Duplicated.FALSE;
            } else {
                thereAreItemsWithThisUrl = Duplicated.TRUE;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while comparing an item " + e.getMessage());
            thereAreItemsWithThisUrl = Duplicated.NOT_SURE;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return thereAreItemsWithThisUrl;
    }
    
    protected boolean isFullyQualifiedItemUrl(String url) {
        return url == null ? false : url.matches(FULLY_QUALIFIED_URL_REGEX);
    } 
    
}
