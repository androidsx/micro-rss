/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Feed represents the source of information for a given set of stories.
 */
public interface Feed extends Serializable {
  
  /**
   * A feed with this ID must be treated as the settings activity, not an ordinary feed.
   * <p>
   * FIXME: This is the ugliest thing we've done, get rid of this soon please  
   */
  public static final int SETTINGS_ID = -2;
  
  /**
   * Database ID for this feed.
   * 
   * @return database ID
   */
  int getId();
    
  /**
   * Plain text representation of the title of the feed
   * 
   * @return title of the item
   */
  String getTitle();

  /**
   * URL to the source of the information, in RSS or ATOM format.
   * 
   * @return URL the URL of the item
   */
  String getURL();
  
  /**
   * Is this feed active? Only active feeds are updated and shown in the views. 
   * 
   * @return true if and only the feed is active
   */
  boolean isActive();
  
  /**
   * Last modification date of the feed 
   * 
   * @return pubDate last updated of this feed
   */
  Date getLastModificationDate();

}
