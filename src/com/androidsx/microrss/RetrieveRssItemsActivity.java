package com.androidsx.microrss;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.androidsx.anyrss.ItemList;
import com.androidsx.anyrss.WimmTemporaryConstants;
import com.androidsx.anyrss.configure.DefaultMaxNumItemsSaved;
import com.androidsx.anyrss.configure.UpdateTaskStatus;
import com.androidsx.anyrss.db.SqLiteRssItemsDao;
import com.androidsx.microrss.configure.DoConfigureThread;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.androidsx.microrss.view.AnyRssAppListModeActivity;

/**
 * Main activity: starts the service, waits for the configuration thread to do the first update, and
 * then gets the items from the DB, and passes them to the view activity.
 */
public class RetrieveRssItemsActivity extends Activity {
  public static final String TAG = "RetrieveRssItemsActivity";

  private static final int DIALOG_ERROR_MESSAGE_KEY = 0;
  String dialogErrorMessage = "";
  private static final int DIALOG_NO_EMAIL_ERROR_MESSAGE_KEY = 2;

  private ItemList itemList;
  
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

    Log.w("WIMM", "Start the update service, and request the first update");
    UpdateService.requestUpdate(new int[] { WimmTemporaryConstants.widgetId });
    UpdateService.forceUpdate();
    startService(new Intent(this, UpdateService.class)); // if already started, does nothing
    
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
    
    Log.w("WIMM", "Main activity: grab the items from the database (instead of the internet)");
    itemList = new SqLiteRssItemsDao(ContentProviderAuthority.AUTHORITY).getItemList(getContentResolver(), WimmTemporaryConstants.widgetId);
    
    Log.w("WIMM", "Start the item to display the " + itemList.getNumberOfItems() + " that were just fetched from the DB");
    startIntentToDisplayItems();
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
}
