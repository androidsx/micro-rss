package com.androidsx.microrss.configure;

import android.content.Context;

/**
 * Holds the information about the number of items that should be stored in the DB. 
 */
public interface MaxNumItemsSaved {
  
  int getMaxNumItemsSaved(Context context, int feedId);
  
  void setMaxNumItemsSaved(Context context, int feedId, int maxNumItemsSaved);
  
  int getDefaultMaxNumItemsSaved(Context context);
}
