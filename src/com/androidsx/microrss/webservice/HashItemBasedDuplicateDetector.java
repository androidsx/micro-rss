package com.androidsx.microrss.webservice;

import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;

class HashItemBasedDuplicateDetector implements DuplicateDetector {
    
	@Override
	public boolean isDuplicated(Item item, ItemList itemList) {
	  int numItems = itemList.getNumberOfItems();

    for (int i = 0; i < numItems; i++) {
      if (itemList.getItemAt(i).equals(item)) {
        return true;
      }
    }
    return false;
	}

}
