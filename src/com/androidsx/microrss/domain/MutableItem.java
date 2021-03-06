package com.androidsx.microrss.domain;

import java.util.Date;


public class MutableItem implements Item {

    private static final long serialVersionUID = 1L;
    
    int id;
    String content;
    Date pubDate;
    String title;
    String url;
    String thumbnail;

    public MutableItem() {
    }
    
    public MutableItem(int id, String content, Date pubDate, String title, String url, String thumbnail) {
        super();
        this.id = id;
        this.content = content;
        this.pubDate = pubDate;
        this.title = title;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Date getPubDate() {
        return pubDate;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getURL() {
        return url;
    }
    
    @Override
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "[" + title.replace('\n', ' ') + ", " + content.substring(0, Math.min(20, content.length())).replace('\n', ' ') + "]";
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
          && (this.content == null ? otherItem.getContent() == null
              : this.content.equals(otherItem.getContent()))
          && (this.url == null ? otherItem.getURL() == null : this.url
              .equals(otherItem.getURL()));
    }
    
    public int hashCode() {
      return HashItemHelper.createHash(title, url, content);
    }
}
