/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.domain;

import java.io.Serializable;

/**
 * List of indexed items that preserves the order.
 * <p>
 * Note: This interface allows to pass a list of items as an extra between activities. It is a
 * workaround to the limitation that we can't pass a list of items (a {@code List<Item>), which is
 * not serializable.
 */
public interface ItemList extends Serializable {

  /**
   * Gets the item at a given position.
   * 
   * @param position index of the item to be retrieved
   * @return the item and the given position
   */
  Item getItemAt(int position);

  /**
   * Returns the number of items in the list
   * 
   * @return number of items
   */
  int getNumberOfItems();
  
  /**
   * Gets the title of the list. In case the list contains RSS items, the title will be the feed title.
   * 
   * @return list title
   */
  String getTitle();

}
