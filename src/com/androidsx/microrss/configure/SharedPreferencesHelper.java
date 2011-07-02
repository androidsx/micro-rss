package com.androidsx.microrss.configure;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

  private static final String PREFS_NAME = "AnyRssAppWidget";
  private static final char ARRAY_SEPARATOR = '|';
  private static final String ARRAY_SEPARATOR_REGEX = "\\|";
  
  private SharedPreferencesHelper() {
    
  }
  
  public static void removeValue(Context context, String key) {
    SharedPreferences.Editor editor =  context.getSharedPreferences(SharedPreferencesHelper.PREFS_NAME, 0).edit();
    editor.remove(key);
    editor.commit();
  }
  
  public static void saveIntValue(Context context, String key, int value) {
    SharedPreferences.Editor editor =  context.getSharedPreferences(SharedPreferencesHelper.PREFS_NAME, 0).edit();
    editor.putInt(key, value);
    editor.commit();
  }
  
  public static int getIntValue(Context context, String key) {
    SharedPreferences config =  context.getSharedPreferences(SharedPreferencesHelper.PREFS_NAME, 0);
    return config.getInt(key, 0);
  }

  /**
   * Adds a string to an array of strings in the shared preferences. A new array
   * will be created if it does not exist already.
   * <p>
   * Since arrays are not supported, the array is encoded as a
   * {@link #ARRAY_SEPARATOR}-separated list.
   */
  public static void addStringToArray(Context context, String key,
      String feedUrl) {
    final String[] stringArray = getStringArray(context, key);
    final List<String> asList = new LinkedList<String>();
    for (String str : stringArray) {
      asList.add(str);
    }
    asList.add(feedUrl);
    final String[] values = asList.toArray(new String[0]);
    saveStringArray(context, key, values);
  }
  
  /**
   * Retrieves an array of strings for the shared preferences.
   * <p>
   * Since arrays are not supported, the array is encoded as a
   * {@link #ARRAY_SEPARATOR}-separated list.
   */
  public static String[] getStringArray(Context context, String key) {
    SharedPreferences config =  context.getSharedPreferences(SharedPreferencesHelper.PREFS_NAME, 0);
    String encodedArray = config.getString(key, ""); // TODO: resize to avoid empty elements
    String[] split = encodedArray.split(ARRAY_SEPARATOR_REGEX);
    return removeEmptyElements(split);
  }
  
  
  private static void saveStringArray(Context context, String key, String[] values) {
    StringBuilder builder = new StringBuilder();
    for (String value : values) {
      builder.append(value).append(ARRAY_SEPARATOR);
    }
    SharedPreferences.Editor editor =  context.getSharedPreferences(SharedPreferencesHelper.PREFS_NAME, 0).edit();
    editor.putString(key, builder.toString());
    editor.commit();
  }
  
  private static String[] removeEmptyElements(String[] inputArray) {
    List<String> outputList = new LinkedList<String>();
    for (String str : inputArray) {
      if (str != null && !str.trim().equals("")) {
        outputList.add(str);
      }
    }
    return outputList.toArray(new String[0]);
  }
  
}
