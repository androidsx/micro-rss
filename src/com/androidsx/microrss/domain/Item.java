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
 * Item that represents a piece of information containing a title and some text, in html format.
 */
public interface Item extends Serializable {

  /**
   * Unique identifier of this story, that generally corresponds to its database ID
   * 
   * @return ID of this story
   */
  int getId();

  /**
   * Plain text representation of the title of the item
   * 
   * @return title of the item
   */
  String getTitle();

  /**
   * HTML representation of the main content of the item
   * 
   * @return main content of the item
   */
  String getContent();
  
  /**
   * URL to the source of the information (if any). 
   * 
   * @return URL the URL of the item
   */
  String getURL();
  
  /**
   * Pubdate of the item (if any) 
   * 
   * @return pubDate publication date of the item
   */
  Date getPubDate();
  
  /**
   * Thumbnail URL of the item (if any) 
   * 
   * @return thumbnail the URL of the image representing the item
   */
  String getThumbnail();

}
