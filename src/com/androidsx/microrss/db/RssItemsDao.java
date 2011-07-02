package com.androidsx.microrss.db;

import android.content.ContentResolver;

import com.androidsx.microrss.domain.ItemList;

/**
 * Data Access Object that provides operations to retrieve and store RSS items
 * from and to persistent storage.
 */
public interface RssItemsDao {

    /**
     * Builds and returns a <i>sorted</i> list of RSS items.
     * 
     * @param resolver content resolver, provided by the activity context
     * @param appWidgetId identifies the widget we are working for
     * 
     * @return sorted list of all RSS items that are stored for this widget
     */
    ItemList getItemList(ContentResolver resolver, int appWidgetId);

    /**
     * Inserts a list of items in the DB, <i>after</i> the ones that already
     * exist in the DB.
     * 
     * @param resolver content resolver, provided by the activity context
     * @param appWidgetId identifies the widget we are working for
     * @param itemsToInsert list of items to be stored in the DB
     */
    void insertItems(ContentResolver resolver, int appWidgetId,
            ItemList itemsToInsert);

    /**
     * Deletes the oldest <i>numItemsToDelete</i> items in the DB.
     * 
     * @param resolver content resolver, provided by the activity context
     * @param appWidgetId identifies the widget we are working for
     * @param numItemsToDelete number of items to be deleted
     * 
     * @return number of items that were actually deleted
     */
    int deleteOldestItems(ContentResolver resolver, int appWidgetId,
            int numItemsToDelete);
    
}
