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

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.androidsx.anyrss.FlurryConstants;
import com.androidsx.anyrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.anyrss.configure.UpdateTaskStatus;
import com.androidsx.anyrss.db.AppWidgets;
import com.androidsx.microrss.ClientSpecificConstants;
import com.androidsx.microrss.UpdateService;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.flurry.android.FlurryAgent;

/**
 * Activity to configure forecast widgets. Usually launched automatically by an
 * {@link AppWidgetHost} after the {@link AppWidgetManager#EXTRA_APPWIDGET_ID} has been bound to a
 * widget.
 */
public class ConfigureActivity extends Activity {
  public static final String TAG = "ConfigureActivity";
  
  public static final String PREFS_AUTO_SCROLL_FIELD_PATTERN = "AutoScrollSeconds-%d";
  
  private static final int PREFS_AUTO_SCROLL_RATE_SECONDS = 0;
  
  private static final int UPDATE_INTERVAL_HOURS = 2;

  private static final int DIALOG_ERROR_MESSAGE_KEY = 0;
  String dialogErrorMessage = "";
  private static final int DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY = 2;
  
  private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
  private boolean successfullyConfigured = false;

  /**
   * Dialog that shows the "loading" message. It is started by {@link #onClick} for the
   * {@link #mSave} button, and closed by {@link #endOfOperationHandler}
   */
  private ProgressDialog loadingDialog;

  /**
   * Handler that receives the status message from {@link DoConfigureThread}, so it knows whether
   * the feed was correctly loaded or not, and acts accordingly.
   */
  private Handler endOfOperationHandler = new Handler() {
    private static final String TAG = "EndOfOperationHandler";

    @Override
    public void handleMessage(Message msg) {
      loadingDialog.dismiss();
      UpdateTaskStatus result = (UpdateTaskStatus) msg.obj;
      Log.v(TAG, "Message is " + result);

      if (result == UpdateTaskStatus.OK) {
        successfullyConfigured = true;
        requestUpdateAndFinishConfig();
      } else if (result == UpdateTaskStatus.FEED_PROCESSING_EXCEPTION_NO_EMAIL
              || result == UpdateTaskStatus.UNKNOWN_ERROR) {
        successfullyConfigured = false;
        // TODO(pablo): should read this from strings.xml
        dialogErrorMessage = "Oops it failed: " + result.getMsg();
        Log.w(TAG, "Can't configure! Message for the user (with NO email): "
                + dialogErrorMessage);
        showDialog(DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY);
      } else {
        successfullyConfigured = false;
        // TODO(pablo): should read this from strings.xml
        dialogErrorMessage = "Oops it failed: " + result.getMsg();
        Log.w(TAG, "Can't configure! Message for the user (with email): "
                + dialogErrorMessage);
        showDialog(DIALOG_ERROR_MESSAGE_KEY);
      }
    }

  };

  @Override
  protected void onStart() {
    super.onStart();
    FlurryAgent.setContinueSessionMillis(FlurryConstants.SESSION_MILLIS);
    FlurryAgent.onStartSession(this, ClientSpecificConstants.FLURRY_APP_KEY);
  }

  @Override
  protected void onStop() {
    super.onStop();
    FlurryAgent.onEndSession(this);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate configure activity");

    requestWindowFeature(Window.FEATURE_NO_TITLE); 
    setContentView(R.layout.configure);
    

    // Read the appWidgetId to configure from the incoming intent
    mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setConfigureResult(Activity.RESULT_CANCELED);
    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
      return;
    }

    String rssUrl = getResources().getString(R.string.feed_url);
    String rssName = getResources().getString(R.string.feed_name);

    loadingDialog = ProgressDialog.show(this, 
        getResources().getString(R.string.dialog_loading_title), 
        getResources().getString(R.string.dialog_loading_description),
        true, // indeterminate
        false); // hmm it is not cancelable ...

    Log.v(TAG, "Start a thread to save the configuration");

    Map<String, String> eventParams = new HashMap<String, String>();
    eventParams.put(FlurryConstants.PARAM_CONFIGURE_FEED_URL, rssUrl);
    FlurryAgent.onEvent(FlurryConstants.EVENT_NEW_WIDGET, eventParams);
    
    int maxNumItemsSaved = new DefaultMaxNumItemsSaved(
            R.string.conf_default_num_items_saved,
            R.string.max_num_items_saved_prefs_name).getDefaultMaxNumItemsSaved(this);

    new DoConfigureThread(this, endOfOperationHandler, mAppWidgetId, rssName,
        rssUrl, UPDATE_INTERVAL_HOURS, ConfigureActivity.PREFS_AUTO_SCROLL_RATE_SECONDS, maxNumItemsSaved).start();
  }

  private void requestUpdateAndFinishConfig() {
    // Trigger pushing a widget update to surface
    Log.i(TAG, "Request update from the save button");
    UpdateService.requestUpdate(new int[] { mAppWidgetId // not sure
        });
    startService(new Intent(this, UpdateService.class));

    setConfigureResult(Activity.RESULT_OK);
    Log.i(TAG, "End of the configure activity");
    finish();
  }

  @Override
  protected Dialog onCreateDialog(int id) {

    switch (id) {
    case DIALOG_ERROR_MESSAGE_KEY: {
      Log.d(TAG, "Show error dialog with msg " + dialogErrorMessage);
      AlertDialog alertDialog = new AlertDialog.Builder(ConfigureActivity.this).setTitle(
          "Oops, error!").setCancelable(false).setMessage(
          dialogErrorMessage + "\n\nYou can send us an email, we'll find out and tell you "
              + "why it failed to load").setNegativeButton("No, thanks",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              dialog.cancel();
              finish();
            }
          }).setPositiveButton("Email us", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
          emailIntent.setType("text/html");
          emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
              new String[] { "android.sx@gmail.com" });
          emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "textsfromlastnight error: "
              + dialogErrorMessage);
          emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "RSS: "
              + getResources().getString(R.string.feed_url));

          dialog.cancel();
          finish();
          
          startActivity(Intent.createChooser(emailIntent, "Share via"));
        }
      }).create();
      return alertDialog;
    }
    case DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY: {
        Log.d(TAG, "Show no-email error dialog with msg " + dialogErrorMessage);
        AlertDialog alertDialog = new AlertDialog.Builder(ConfigureActivity.this)
        .setTitle("Oops, error!")
        .setMessage(dialogErrorMessage)
          .setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              // Just close it and let the user try again
            }
          })
          .create();
        return alertDialog;
      }
    }
    return null;
  }

  /**
   * Convenience method to always include {@link #mAppWidgetId} when setting the result
   * {@link Intent}.
   */
  private void setConfigureResult(int resultCode) {
    final Intent data = new Intent();
    data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(resultCode, data);
  }

  /**
   * If the configuration has failed (or simply didn't finish), make sure that there is no DB entry
   * for this widget.
   * <p>
   * Note: when we change the orientation, this method is also called. It is not a problem with the
   * current implementation, though.
   */
  @Override
  protected void onDestroy() {
    if (!successfullyConfigured && mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
      Log.d(TAG, "Deleting appWidgetId=" + mAppWidgetId);
      ContentResolver resolver = getContentResolver();
      Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY), mAppWidgetId);
      int rows = resolver.delete(appWidgetUri, null, null);
      Log.d(TAG, rows + " rows deleted (probably 0, since usually"
          + " this widget is not in the DB yet)");
    }
    super.onDestroy();
  }
}
