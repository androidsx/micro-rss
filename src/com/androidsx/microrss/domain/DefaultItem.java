/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.domain;

import java.util.Date;



public class DefaultItem implements Item {

  private static final long serialVersionUID = 7526472295622776145L;
  
  private final int id;
  private final String title;
  private final String description;
  private final String URL;
  private final Date pubDate;
  private final String thumbnail;

  public DefaultItem(int id, String title, String description, String URL, Date pubDate, String thumbnail) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.URL = URL;
    this.pubDate = pubDate;
    this.thumbnail = thumbnail;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getContent() {
    return description;
  }
  
  @Override
  public String getURL() {
    return URL;
  }
  
  @Override
  public Date getPubDate() {
    return pubDate;
  }
  
  @Override
  public String getThumbnail() {
    return thumbnail;
  }

  @Override
  public int getId() {
      return id;
  }
  
  @Override
  public String toString() {
    return "[" + title.replace('\n', ' ') + ", " + description.substring(0, Math.min(20, description.length())).replace('\n', ' ') + " thumb: " + thumbnail + "]";
  }

  @Override
  public boolean equals(Object other) {
    // Not strictly necessary, but often a good optimization
    if (this == other)
      return true;
    if (!(other instanceof Item))
      return false;

    Item otherItem = (Item) other;

    return (this.title == null ? otherItem.getTitle() == null : this.title
        .equals(otherItem.getTitle()))
        && (this.description == null ? otherItem.getContent() == null
            : this.description.equals(otherItem.getContent()))
        && (this.URL == null ? otherItem.getURL() == null : this.URL
            .equals(otherItem.getURL()));
  }
  
  @Override
  public int hashCode() {
    return HashItemHelper.createHash(title, URL, description);
  }
}
