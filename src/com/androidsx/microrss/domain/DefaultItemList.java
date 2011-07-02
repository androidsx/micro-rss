/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.domain;

import java.util.LinkedList;
import java.util.List;

public class DefaultItemList implements ItemList {
  
  private static final long serialVersionUID = 7526472295622776147L;
  
  private final List<Item> items;
  
  private String title;
  
  public DefaultItemList() {
    items = new LinkedList<Item>();
  }

  public void addItem(Item item) {
    items.add(item);
  }
  
  @Override
  public Item getItemAt(int position) {
    return items.get(position);
  }

  @Override
  public int getNumberOfItems() {
    return items.size();
  }
  
  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return items.toString();
  }
  
}
