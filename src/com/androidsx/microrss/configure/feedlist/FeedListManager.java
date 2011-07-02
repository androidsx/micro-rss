package com.androidsx.microrss.configure.feedlist;

import java.util.Locale;

import android.content.Context;

/**
 * Manager for retrieving and maintaining the list of available feeds.
 */
public interface FeedListManager {

  SpinnerFeedItemInfo getFeedListTitle(Context context);
  
  /**
   * Retrieves the full list of feeds: all items and the categories, localized
   * for the selected locale.
   * 
   * @param context Android context, to have access to localized strings
   * @param locale locale that filters the items
   */
  SpinnerFeedItemInfo[] retrieveItems(Context context, Locale locale);

  /**
   * Retrieves the list of categories (News, Sports, ...).
   * 
   * @param context Android context, to have access to localized strings
   */
  SpinnerFeedItemInfo[] retrieveCategories(Context context);

  /**
   * Retrieves the localized feeds that belong to the provided category.
   * 
   * @param context Android context, to have access to localized strings
   * @param locale locale that filters the items
   * @param category category to filter by
   */
  SpinnerFeedItemInfo[] retrieveItemsByCategory(Context context,
      Locale locale, String category);
  
  
  /**
   * Adds a custom URL to the list of feeds. If the URL is new, it will be added
   * as a custom URL. Otherwise, it will just be discarded.
   * <p>
   * This allows clients to pass all selected URLs in without checking whether
   * they are custom or predefined.
   * 
   * @param context Android context, to have access to localized strings
   * @param feedInfo URL selected by the user, that could be a custom one
   */
  void addPotentialCustomUrl(Context context, SpinnerFeedItemInfo feedInfo);
  
  /**
   * Is this feed name a separator?
   * <p>
   * The spinner view only has access to the string representation of the item,
   * given by the {@code toString} method, so it needs a helper like this to
   * distinguish a real feed element (with an associated URL) from a title that
   * just separates a bunch of feeds.
   * 
   * @param feedName the feed name, as {@code toString} formats it
   * @return true if and only if the item is actually a title
   */
  boolean isSeparator(String feedName);
  
  /**
   * Is this feed name the spinner title?
   * 
   * @param feedItemName the feed name, as {@code toString} formats it
   * @return true if and only if the item is the spinner title
   */
  boolean isSpinnerTitle(String feedName);

}
