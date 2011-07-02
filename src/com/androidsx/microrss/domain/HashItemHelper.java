package com.androidsx.microrss.domain;

class HashItemHelper {

  public static int createHash(String title, String URL, String content) {
    int hash = 1;
    hash = hash * 31  + (title == null ? 0 : title.hashCode());
    hash = hash * 31  + (URL == null ? 0 : URL.hashCode());
    hash = hash * 31  + (content == null ? 0 : content.hashCode());
    return hash;
  }
}
