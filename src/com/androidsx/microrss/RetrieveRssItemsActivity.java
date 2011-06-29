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

package com.androidsx.microrss;

import java.util.zip.DataFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.androidsx.anyrss.ItemList;
import com.androidsx.anyrss.WimmTemporaryConstants;
import com.androidsx.anyrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.anyrss.configure.UpdateTaskStatus;
import com.androidsx.anyrss.db.SqLiteRssItemsDao;
import com.androidsx.anyrss.webservice.FeedProcessingException;
import com.androidsx.anyrss.webservice.WebserviceHelper;
import com.androidsx.microrss.configure.DoConfigureThread;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.androidsx.microrss.view.AnyRssAppListModeActivity;

/**
 * Activity to configure forecast widgets. Usually launched automatically by an
 * {@link AppWidgetHost} after the {@link AppWidgetManager#EXTRA_APPWIDGET_ID}
 * has been bound to a widget.
 */
public class RetrieveRssItemsActivity extends Activity {
  public static final String TAG = "RetrieveRssItemsActivity";

  private static final int DIALOG_ERROR_MESSAGE_KEY = 0;
  String dialogErrorMessage = "";
  private static final int DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY = 2;

  private ItemList itemList;

  private static final int MAX_NUM_ITEMS_RETRIEVED = 100;
  /**
   * Dialog that shows the "loading" message. It is started by {@link #onClick}
   * for the {@link #mSave} button, and closed by {@link #endOfOperationHandler}
   */
  private ProgressDialog loadingDialog;

  /**
   * Handler that receives the status message from {@link DoConfigureThread}, so
   * it knows whether the feed was correctly loaded or not, and acts
   * accordingly.
   */
  private Handler endOfOperationHandler = new Handler() {
    private static final String TAG = "EndOfOperationHandler";

    @Override
    public void handleMessage(Message msg) {
      Log.d(TAG, "Received message");
      loadingDialog.dismiss();
      UpdateTaskStatus result = (UpdateTaskStatus) msg.obj;
      Log.d(TAG, "Message is " + result);

      if (result == UpdateTaskStatus.OK) {
        startIntentToDisplayItems();
      } else if (result == UpdateTaskStatus.FEED_PROCESSING_EXCEPTION_NO_EMAIL
          || result == UpdateTaskStatus.UNKNOWN_ERROR) {
        dialogErrorMessage = "Oops it failed: " + result.getMsg();
        Log.w(TAG, "Can't configure! Message for the user (with NO email): "
            + dialogErrorMessage);
        showDialog(DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY);
      } else {
        dialogErrorMessage = "Oops it failed: " + result.getMsg();
        Log.w(TAG, "Can't configure! Message for the user (with email): "
            + dialogErrorMessage);
        showDialog(DIALOG_ERROR_MESSAGE_KEY);
      }
    }

  };
  
  /**
   * Handler that receives the status message from {@link DoConfigureThread}, so it knows whether
   * the feed was correctly loaded or not, and acts accordingly.
   */
  private Handler endOfConfigureThreadHandler = new Handler() {
    private static final String TAG = "EndOfOperationHandler";

    @Override
    public void handleMessage(Message msg) {
        
        
      UpdateTaskStatus result = (UpdateTaskStatus) msg.obj;
      Log.v(TAG, "Message is " + result);

      if (result == UpdateTaskStatus.OK) {
        //successfullyConfigured = true;
          Log.w("WIMM", "Return from the configure thread, which finished OK");
        onConfigureThreadFinishesSuccessfully();
      } else if (result == UpdateTaskStatus.FEED_PROCESSING_EXCEPTION_NO_EMAIL
              || result == UpdateTaskStatus.UNKNOWN_ERROR) {
        //successfullyConfigured = false;
        // TODO(pablo): should read this from strings.xml
        dialogErrorMessage = "Oops it failed: " + result.getMsg();
        Log.w(TAG, "Can't configure! Message for the user (with NO email): "
                + dialogErrorMessage);
        showDialog(DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY);
      } else {
        //successfullyConfigured = false;
        // TODO(pablo): should read this from strings.xml
        dialogErrorMessage = "Oops it failed: " + result.getMsg();
        Log.w(TAG, "Can't configure! Message for the user (with email): "
                + dialogErrorMessage);
        showDialog(DIALOG_ERROR_MESSAGE_KEY);
      }
    }

  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate retrieve items activity");

    //Log.w("WIMM", "Main activity: force update the update of the fake widget. This starts up the update service grr");
    //new MedAppWidget().updateFeedAction(this, WimmTemporaryConstants.widgetId);
    
    Log.w("WIMM", "Start the update service, and request the first update");
    
    UpdateService.requestUpdate(new int[] { WimmTemporaryConstants.widgetId });
    UpdateService.forceUpdate();
    startService(new Intent(this, UpdateService.class)); // if already started, does nothing
    
    /*Log.w("WIMM", "Main activity: call the configure activity, by launching the activity. important to save the config");
    Intent intent = new Intent(this, ConfigureActivity.class);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, WimmTemporaryConstants.widgetId);
    startActivityForResult(intent, 0);*/
    

    
    Log.w("WIMM", "Start the doConfigure thread, we are still in the main activity");
    String rssUrl = getResources().getString(R.string.feed_url); // FIXME: this can't be hardcoded anymore. In AnyRSS, it used to come from the extras (from the configure activity, I guess)
    String rssName = getResources().getString(R.string.feed_name);
    int maxNumItemsSaved = new DefaultMaxNumItemsSaved(
            R.string.conf_default_num_items_saved,
            R.string.max_num_items_saved_prefs_name).getDefaultMaxNumItemsSaved(this);
    new DoConfigureThread(this, endOfConfigureThreadHandler, WimmTemporaryConstants.widgetId, rssName,
            rssUrl, UPDATE_INTERVAL_HOURS, PREFS_AUTO_SCROLL_RATE_SECONDS, maxNumItemsSaved).start();
  }
  
  private static final int PREFS_AUTO_SCROLL_RATE_SECONDS = 0;
  private static final int UPDATE_INTERVAL_HOURS = 2;
  
  // FIXME: Here, we don't even filter by resultCode (in a rush due to wimm)
  private void onConfigureThreadFinishesSuccessfully() {
    Log.i("WIMM", "Continue with the normal execution of the activity");
    
    //requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    // Retrieve the info extras, if not check resources
    String rssUrl = getResources().getString(R.string.feed_url); // FIXME: this can't be hardcoded anymore. In AnyRSS, it used to come from the extras (from the configure activity, I guess)
    String rssName = getResources().getString(R.string.feed_name);
    
    Log.d(TAG, "Show loading dialog for feed " + rssName + " : " + rssUrl);
    loadingDialog = ProgressDialog.show(this, getResources().getString(
            R.string.dialog_loading_title), getResources().getString(
                    R.string.dialog_loading_description), true, // indeterminate
                    false); // hmm it is not cancelable ...
    
    Log.d(TAG, "Start a thread to retrieve the list of items from " + rssUrl + ": WIMM-canceled");
    //new Thread(new RetrieveRssItems(rssName, rssUrl)).start();
    
    Log.w("WIMM", "Main activity: grab the items from the database (instead of the internet)");
    itemList = new SqLiteRssItemsDao(ContentProviderAuthority.AUTHORITY).getItemList(getContentResolver(), WimmTemporaryConstants.widgetId);
    
    // Let's just mock-notify that this is done
    Message statusMessage = Message.obtain();
    statusMessage.obj = UpdateTaskStatus.OK;
    endOfOperationHandler.sendMessage(statusMessage);
    
    Log.w("WIMM", itemList.getNumberOfItems() + " items were fetched. We just told the handler that we are done here");
  }

  private void startIntentToDisplayItems() {
    Intent detailIntent = new Intent(this, AnyRssAppListModeActivity.class);
    detailIntent.putExtra("appWidgetId", 0);
    detailIntent.putExtra("itemList", itemList);
    Log.w("WIMM", "Start AnyRssAppListModeActivity, passing the list of " + itemList.getNumberOfItems() + " items");
    startActivity(detailIntent);

    Log.i(TAG, "End of the anyrss activity");
    finish();
  }

  @Override
  protected Dialog onCreateDialog(int id) {

    switch (id) {
    case DIALOG_ERROR_MESSAGE_KEY: {
      Log.d(TAG, "Show error dialog with msg " + dialogErrorMessage);
      AlertDialog alertDialog = new AlertDialog.Builder(
          RetrieveRssItemsActivity.this)
          .setTitle("Oops, error!")
          .setCancelable(false)
          .setMessage(
              dialogErrorMessage
                  + "\n\nYou can send us an email, we'll find out and tell you "
                  + "why it failed to load").setNegativeButton("No, thanks",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  dialog.cancel();
                  finish();
                }
              }).setPositiveButton("Email us",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  final Intent emailIntent = new Intent(
                      android.content.Intent.ACTION_SEND);
                  emailIntent.setType("text/html");
                  emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                      new String[] { "android.sx@gmail.com" });
                  emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                      getResources().getString(R.string.app_name) + " error: " + dialogErrorMessage);
                  emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                      "RSS: " + getResources().getString(R.string.feed_url));

                  dialog.cancel();
                  finish();

                  startActivity(Intent.createChooser(emailIntent, "Share via"));
                }
              }).create();
      return alertDialog;
    }
    case DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY: {
      Log.d(TAG, "Show no-email error dialog with msg " + dialogErrorMessage);
      AlertDialog alertDialog = new AlertDialog.Builder(
          RetrieveRssItemsActivity.this).setTitle("Oops, error!").setMessage(
          dialogErrorMessage).setNegativeButton("OK",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              dialog.cancel();
              finish();
            }
          }).create();
      return alertDialog;
    }
    }
    return null;
  }

  
  class RetrieveRssItems implements Runnable {
    String rssName;
    String rssUrl;
    
    public RetrieveRssItems(String rssName, String rssUrl) {
      this.rssName = rssName;
      this.rssUrl = rssUrl;
    }

    public void run() {
      UpdateTaskStatus statusCode = UpdateTaskStatus.OK;
      try {
        itemList = WebserviceHelper.getRssItems(RetrieveRssItemsActivity.this,
            rssUrl, rssName, MAX_NUM_ITEMS_RETRIEVED);

        if (itemList == null || itemList.getNumberOfItems() == 0) {
          statusCode = UpdateTaskStatus.FEED_PROCESSING_EXCEPTION_NO_EMAIL;
        }
      } catch (FeedProcessingException e) {
        Log.w(TAG, "ParseException caught retrieving the rss items from "
            + rssUrl, e);
        statusCode = e.getStatus();
        statusCode.setMsg(e.getMessage());
      }

      Message statusMessage = Message.obtain();
      statusMessage.obj = statusCode;
      endOfOperationHandler.sendMessage(statusMessage);
    }
  }
}
