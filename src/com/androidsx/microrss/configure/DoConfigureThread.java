package com.androidsx.microrss.configure;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;

import com.androidsx.anyrss.configure.UpdateTaskStatus;
import com.androidsx.anyrss.db.AppWidgets;
import com.androidsx.anyrss.db.AppWidgetsColumns;
import com.androidsx.anyrss.webservice.FeedProcessingException;
import com.androidsx.anyrss.webservice.WebserviceHelper;
import com.androidsx.microrss.UpdateService;
import com.androidsx.microrss.db.ContentProviderAuthority;

/**
 * Thread that saves the configuration, downloads and parses the feed, and
 * returns an status code to the calling thread (most likely
 * {@link ConfigureActivity} about the outcome of the operation.
 */
public class DoConfigureThread extends Thread {

  private static final String TAG = DoConfigureThread.class.getSimpleName();

  private final Context context;
  private final Handler endOfOperationHandler;
  private final int appWidgetId;
  private final String title;
  private final String rssUrl;
  private final int updateIntervalHours;
  private final int autoScrollSeconds;
  private final int maxNumItemsSaved;

  public DoConfigureThread(Context context, Handler endOfOperationHandler,
          int appWidgetId, String title, String rssUrl, int updateIntervalHours, int autoScrollSeconds, int maxNumItemsSaved) {
    this.context = context;
    this.endOfOperationHandler = endOfOperationHandler;
    this.appWidgetId = appWidgetId;
    this.title = title;
    this.rssUrl = rssUrl;
    this.updateIntervalHours = updateIntervalHours;
    this.autoScrollSeconds = autoScrollSeconds;
    this.maxNumItemsSaved = maxNumItemsSaved;
  }

  @Override
  public void run() {
    Log.d(TAG, "Start DoConfigureThread thread (0/3)");
    Log.w("WIMM", "Start doConfigure (1/3)");
    writeConfigToBackend(appWidgetId, context, title, rssUrl, updateIntervalHours, autoScrollSeconds);
    Log.w("WIMM", "In progress doConfigure (2/3)");
    UpdateTaskStatus status = requestFirstUpdate(appWidgetId, context, maxNumItemsSaved);
    sendMessageToHandler(endOfOperationHandler, status);
    Log.w("WIMM", "End doConfigure (3/3)");
    Log.d(TAG, "End of DoConfigureThread thread with status " + status);
  }

  private static void writeConfigToBackend(int appWidgetId, Context context,
          String title, String rssUrl, int updateIntervalHours, int autoScrollSeconds) {
    Log.d(TAG, "Save to backend: widgetID " + appWidgetId + ", title " + title
            + ", url " + rssUrl + " (" + updateIntervalHours + "h)");
    ContentValues values = new ContentValues();
    values.put(BaseColumns._ID, appWidgetId);
    values.put(AppWidgetsColumns.TITLE, title);
    values.put(AppWidgetsColumns.WEBVIEW_TYPE,
            AppWidgetsColumns.WEBVIEW_TYPE_DEFAULT);
    values.put(AppWidgetsColumns.LAST_UPDATED, -1);
    values.put(AppWidgetsColumns.CURRENT_ITEM_POSITION, 0);
    values.put(AppWidgetsColumns.UPDATE_INTERVAL, updateIntervalHours * 60);
    values.put(AppWidgetsColumns.RSS_URL, rssUrl);

    // TODO: update instead of insert if editing an existing widget
    ContentResolver resolver = context.getContentResolver();
    resolver.insert(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY), values);
    
//    SharedPreferencesHelper.saveIntValue(context, String.format(
//        ConfigureActivity.PREFS_AUTO_SCROLL_FIELD_PATTERN, appWidgetId), autoScrollSeconds);
  }

  private static UpdateTaskStatus requestFirstUpdate(int appWidgetId,
          Context context, int maxNumItemsSaved) {
    Log.i(TAG, "Request the first update");
    try {
      Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY),
              appWidgetId);
      
    WebserviceHelper.updateForecastsAndFeeds(context,
              appWidgetUri,
              appWidgetId,
              Math.max(UpdateService.MAX_ITEMS_PER_FEED, maxNumItemsSaved),
              ContentProviderAuthority.AUTHORITY,
              maxNumItemsSaved);
    } catch (FeedProcessingException e) {
      Log.w(TAG, "ParseException caught in the save button", e);
      UpdateTaskStatus statusCode = e.getStatus();
      statusCode.setMsg(e.getMessage()); // is this necessary? probably not
      return statusCode;
    } catch (Exception e) {
      Log.w(TAG, "Unknown exception caught in the save button", e);
      return UpdateTaskStatus.UNKNOWN_ERROR;
    }
    Log.i(TAG, "First update status: OK");
    return UpdateTaskStatus.OK;
  }

  private static void sendMessageToHandler(Handler endOfOperationHandler,
          UpdateTaskStatus status) {
    Message statusMessage = Message.obtain();
    statusMessage.obj = status;
    endOfOperationHandler.sendMessage(statusMessage);
  }

}
