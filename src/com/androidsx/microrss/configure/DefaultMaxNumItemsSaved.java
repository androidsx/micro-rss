package com.androidsx.microrss.configure;

import android.content.Context;

public class DefaultMaxNumItemsSaved implements MaxNumItemsSaved {
  
  /**
   * In the (unlikely) case that the resources for default num items saved fails, we just use
   * this value.
   */
  private static final int NUM_ITEMS_SAVED_FALLBACK = 5;

  private final int confDefaultNumItemsSavedId;
  private final int maxNumItemsSavedPrefsNameId;
  
  /**
   * TODO: Pass in the actual values rather than the ids to Context objects
   * 
   * @param confDefaultNumItemsSavedId Usually corresponds to {@code R.string.conf_default_num_items_saved}
   * @param maxNumItemsSavedPrefsNameId Usually corresponds to {@code R.string.max_num_items_saved_prefs_name}
   */
  public DefaultMaxNumItemsSaved(int confDefaultNumItemsSavedId, int maxNumItemsSavedPrefsName) {
    this.confDefaultNumItemsSavedId = confDefaultNumItemsSavedId;
    this.maxNumItemsSavedPrefsNameId = maxNumItemsSavedPrefsName;
}

@Override
  public int getDefaultMaxNumItemsSaved(Context context) {
    int maxNumItemsSaved;
    try {
      maxNumItemsSaved = Integer.parseInt(context.getResources().getString(confDefaultNumItemsSavedId));
    } catch (NumberFormatException ex) {
      maxNumItemsSaved = NUM_ITEMS_SAVED_FALLBACK;
    }
    
    return maxNumItemsSaved;
  }

  @Override
  public int getMaxNumItemsSaved(Context context, int appWidgetId) {
    int maxNumItemsSaved = SharedPreferencesHelper.getIntValue(context, String.format(
        context.getResources().getString(maxNumItemsSavedPrefsNameId), appWidgetId));
    if (maxNumItemsSaved == 0) {
     maxNumItemsSaved = getDefaultMaxNumItemsSaved(context);
    }

    return maxNumItemsSaved;
  }

  @Override
  public void setMaxNumItemsSaved(Context context, int appWidgetId, int maxNumItemsSaved) {
    SharedPreferencesHelper.saveIntValue(context, String.format(
        context.getResources().getString(maxNumItemsSavedPrefsNameId), appWidgetId), maxNumItemsSaved);
  }

}
