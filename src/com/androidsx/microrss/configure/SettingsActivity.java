/*
 * Copyright (C) 2009 Jeff Sharkey, http://jsharkey.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidsx.microrss.configure;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.androidsx.anyrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.anyrss.configure.MaxNumItemsSaved;
import com.androidsx.anyrss.db.FeedDbTable;
import com.androidsx.anyrss.db.FeedColumns;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.ContentProviderAuthority;

/**
 * Activity to configure a widget after being inserted (not in configure activity). Usually launched 
 * from a menu entry
 */
public class SettingsActivity extends Activity implements View.OnClickListener { 
  public static final String TAG = "SettingsActivity";

  /**
   * In the (unlikely) case that the update interval given by the user is not correct, we just use
   * this value.
   * <p>
   * This constant has been copied all over the place, in hours or minutes... so be careful!
   */
  private static final int UPDATE_INTERVAL_FALLBACK_HOURS = 3;

  private Button mSave;

  private EditText mUpdateInterval;
  private SeekBar mSeekUpdateInterval;
  private final OnSeekBarChangeListener seekUpdateIntervalChangeListener = new OnSeekBarChangeListener() {
      
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
          // Purposedly empty
      }
      
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
          // Purposedly empty
      }
      
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
              boolean fromUser) {
          mUpdateInterval.setText("" + (progress + 1));
      }
  };
  
  private EditText mNumItemsSaved;
  private SeekBar mSeekNumItemsSaved;
  private final OnSeekBarChangeListener seekNumItemsSavedChangeListener = new OnSeekBarChangeListener() {
      
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
          // Purposedly empty
      }
      
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
          // Purposedly empty
      }
      
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
              boolean fromUser) {
          mNumItemsSaved.setText("" + (progress + 1));
      }
  };

  private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

  private static final String[] PROJECTION_APPWIDGETS = new String[] { FeedColumns.UPDATE_INTERVAL };

  private static final int COL_UPDATE_INTERVAL = 0;

  private MaxNumItemsSaved maxNumItemsSaved;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate settings activity");

    setContentView(R.layout.settings);

    mUpdateInterval = (EditText) findViewById(R.id.conf_update_time);
    mSeekUpdateInterval = (SeekBar) findViewById(R.id.seek_conf_update_time);
    mSeekUpdateInterval.setOnSeekBarChangeListener(seekUpdateIntervalChangeListener);
    
    mNumItemsSaved = (EditText) findViewById(R.id.conf_num_items_saved);
    mSeekNumItemsSaved = (SeekBar) findViewById(R.id.seek_conf_num_items_saved);
    mSeekNumItemsSaved.setOnSeekBarChangeListener(seekNumItemsSavedChangeListener);
    
    mSave = (Button) findViewById(R.id.conf_save);
    mSave.setOnClickListener(this);

    // Read the appWidgetId to configure from the incoming intent
    mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
      return;
    }
    
    maxNumItemsSaved = new DefaultMaxNumItemsSaved(
            R.string.conf_default_num_items_saved,
            R.string.max_num_items_saved_prefs_name);

    int updateInterval = getCurrentUpdateInterval();
    mSeekUpdateInterval.setProgress(updateInterval - 1);
    mSeekUpdateInterval.setSecondaryProgress(updateInterval - 1);
    mUpdateInterval.setText("" + updateInterval);
    
    int currentMaxNumItemsSaved = maxNumItemsSaved.getMaxNumItemsSaved(this, mAppWidgetId);
    mSeekNumItemsSaved.setProgress(currentMaxNumItemsSaved - 1);
    mSeekNumItemsSaved.setSecondaryProgress(currentMaxNumItemsSaved - 1);
    mNumItemsSaved.setText("" + currentMaxNumItemsSaved);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.conf_save: {

      int updateInterval;
      try {
        updateInterval = Integer.parseInt(mUpdateInterval.getText().toString());
      } catch (NumberFormatException e) {
        Log.e(TAG, "Can't parse the update interval: " + mUpdateInterval.getText());
        updateInterval = UPDATE_INTERVAL_FALLBACK_HOURS;
      }

      setUpdateInterval(updateInterval);
      
      
      int numItemsSaved;
      try {
        numItemsSaved = Integer.parseInt(mNumItemsSaved.getText().toString());
      } catch (NumberFormatException e) {
        Log.e(TAG, "Can't parse the numItemsSaved: " + mNumItemsSaved.getText());
        
        numItemsSaved = maxNumItemsSaved.getDefaultMaxNumItemsSaved(this);
      }

      maxNumItemsSaved.setMaxNumItemsSaved(this, mAppWidgetId, numItemsSaved);
      
      finish();
      break;
    }
    }
  }

  private void setUpdateInterval(int updateInterval) {
    ContentResolver resolver = getContentResolver();
    ContentValues values = new ContentValues();

    // This Uri has the WIDGET_ID, so we only update ONE widget
    Uri appWidgetUriWithId = ContentUris.withAppendedId(FeedDbTable.getContentUri(ContentProviderAuthority.AUTHORITY), mAppWidgetId);
    values.put(FeedColumns.UPDATE_INTERVAL, updateInterval * 60);
    int updateRows = resolver.update(appWidgetUriWithId, values, null, null);
    Log.d(TAG, "Updated " + updateRows + " rows for UPDATE_INTERVAL with value " 
        + updateInterval + " (should be one and only one)");
  }

  /**
   * Gets the actual update interval for the actual widget that is being configured.
   * <p>
   * The update interval format is hours
   * 
   * @return
   */
  private int getCurrentUpdateInterval() {
    Uri appWidgetUriWithId = ContentUris.withAppendedId(FeedDbTable.getContentUri(ContentProviderAuthority.AUTHORITY), mAppWidgetId);
    Cursor cursor = null;
    int updateIntervalHours = UPDATE_INTERVAL_FALLBACK_HOURS;
    try {
      cursor = getContentResolver().query(appWidgetUriWithId, PROJECTION_APPWIDGETS, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        final int dBUpdateIntervalMinutes = cursor.getInt(COL_UPDATE_INTERVAL);
        final int updateIntervalMinutes = dBUpdateIntervalMinutes < 60
              ? UPDATE_INTERVAL_FALLBACK_HOURS * 60
              : dBUpdateIntervalMinutes;
        updateIntervalHours = updateIntervalMinutes / 60;
        Log.d(TAG, "Reading widget update interval from cursor: "
            + dBUpdateIntervalMinutes + " minutes (" + updateIntervalMinutes
            + ")");
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return updateIntervalHours;
  }
}