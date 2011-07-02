/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.configure.feedlist;

import java.util.Locale;

import android.util.Log;

/**
 * Element in the spinner for the configuration screen, that contains information about a feed: name
 * and URL.
 * <p>
 * Additionally, an item can also behave as a separator in the spinner: the URL will not be read,
 * and the name will appear in such a way that it looks like a section title that separates the
 * items above from the ones below, which belong to this title. For instance, a separator could have
 * <i>Sports</i> as a name.
 * <p>
 * Override the method {@link #toString()} to customize what the item text will be in the spinner.
 */
public class SpinnerFeedItemInfo {
  private static final String TAG = SpinnerFeedItemInfo.class.getSimpleName();

  /** Separator for the serialized form of objects of this type. */
  private static final String SEPARATOR = "^";
  
  private final String name;
  private final String url;
  private final Locale locale;
  private final boolean isSeparator;

  /**
   * Main constructor.
   * 
   * @param name name for the feed, such as <i>Marca</i>
   * @param url url of the feed, such as <i>http://rss.marca.com/rss</i>
   * @param locale locale for which this feed is available. Use {@code null} for <i>all locales</i>.
   * @param isSeparator if true, this item will behave as a separator, as explained in the class
   *          comment
   */
  public SpinnerFeedItemInfo(String name, String url, Locale locale, boolean isSeparator) {
    this.name = name;
    this.url = url;
    this.locale = locale;
    this.isSeparator = isSeparator;
  }

  /**
   * Convenience constructor that sets the {@code isSeparator} to false.
   * 
   * @param name name for the feed, such as <i>Marca</i>
   * @param url url of the feed, such as <i>http://rss.marca.com/rss</i>
   */
  public SpinnerFeedItemInfo(String name, String url, Locale locale) {
    this(name, url, locale, false);
  }
  
  /**
   * Convenience constructor that sets the locale to null.
   * 
   * @param name name for the feed, such as <i>Marca</i>
   * @param url url of the feed, such as <i>http://rss.marca.com/rss</i>
   */
  public SpinnerFeedItemInfo(String name, String url, boolean isSeparator) {
    this(name, url, null, isSeparator);
  }

  /**
   * Convenience constructor that sets the {@code isSeparator} to false and the
   * locale to null.
   * 
   * @param name name for the feed, such as <i>Marca</i>
   * @param url url of the feed, such as <i>http://rss.marca.com/rss</i>
   */
  public SpinnerFeedItemInfo(String name, String url) {
    this(name, url, null, false);
  }

  /** See {@link #serialize} */
  public static SpinnerFeedItemInfo deserialize(String serialized) {
    final String[] split = serialized.split("\\" + SEPARATOR);
    if (split.length != 2) {
      Log.e(TAG, "Can't deserialize " + serialized);
      return new SpinnerFeedItemInfo(serialized, serialized);
    } else {
      return new SpinnerFeedItemInfo(split[0], split[1]);
    }
  }
  
  /** See {@link #deserialize} */
  public String serialize() {
    return name + SEPARATOR + url;
  }
  
  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public Locale getLocale() {
    return locale;
  }

  public boolean isSeparator() {
    return isSeparator;
  }

  /**
   * The spinner adapter uses it to render items in the main spinner view: where only the selected
   * item appears (not when the full list is shown).
   */
  @Override
  public String toString() {
    return name;
  }

}
