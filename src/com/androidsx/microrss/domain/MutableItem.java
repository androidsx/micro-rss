package com.androidsx.microrss.domain;

import java.util.Date;


public class MutableItem implements Item {

    private static final long serialVersionUID = 1L;
    
    String content;
    Date pubDate;
    String title;
    String url;

    public MutableItem() {
    }
    
    public MutableItem(String content, Date pubDate, String title, String url) {
        super();
        this.content = content;
        this.pubDate = pubDate;
        this.title = title;
        this.url = url;
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

    public String getUrl() {
        return url;
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
