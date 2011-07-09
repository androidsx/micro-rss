package com.androidsx.microrss.webservice;

import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;

/**
 * Provides operations to detect duplicated feed items.
 * <p>
 * TODO: merge with an upper-level helper interface once WebserviceHelper is refactored 
 */
interface DuplicateDetector {
    
    /**
     * Finds out whether this item is already loaded.
     * 
     * @param item the item which might be duplicated
     * @param itemList the list of items to compare with
     * @return true if and only this item is already loaded
     */
    boolean isDuplicated(Item item, ItemList itemList);
}
