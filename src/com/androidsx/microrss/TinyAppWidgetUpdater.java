package com.androidsx.microrss;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;

import com.androidsx.anyrss.AppWidgetUpdater;
import com.androidsx.anyrss.ItemList;
import com.androidsx.microrss.ClientSpecificConstants.ActionIds;
import com.androidsx.microrss.R;
import com.androidsx.anyrss.db.AppWidgets;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.androidsx.microrss.view.DetailsViewChooser;

/**
 * Updater for the small-sized widget. 
 */
public class TinyAppWidgetUpdater extends AppWidgetUpdater {
  private static final String TAG = TinyAppWidgetUpdater.class.getSimpleName();
  
  /** Suffix to be added to the title, to fix a minor rendering issue. */
  private static final String TITLE_SUFFIX = " ";
  
  @Override
  protected RemoteViews fillUpView(Context context, int appWidgetId,
      ItemList itemList) {
    RemoteViews views = new RemoteViews(context.getPackageName(),
        R.layout.widget_tiny);

    // Use the first item that we retrieved for the title and content
    views.setTextViewText(R.id.feedTitle, itemList.getTitle() + TITLE_SUFFIX);

    // Connect the buttons to the intents
    linkButtonsToIntents(context, appWidgetId, views); 

    return views;
  }
      
  @Override
  public Class<? extends AppWidgetProvider> getAppWidgetClass() {
    return TinyAppWidget.class;
  }

  @Override
  protected String getAuthority() {
    return ContentProviderAuthority.AUTHORITY;
  }
    
  /**
   * Set onclick intents to the current view of a specific widget on the home screen. This intents
   * are usually set when the widget loaded successfully.
   * 
   * @param views The {@link RemoteView} where we connect the intents
   * @param context the context of the activity
   * @param appWidgetId the widget id to update
   */
  private void linkButtonsToIntents(Context context, int appWidgetId, RemoteViews views) {
    Log.d(TAG, "Link the buttons in the widget view");
    
    Uri appWidgetUri = ContentUris.withAppendedId(AppWidgets.getContentUri(ContentProviderAuthority.AUTHORITY), appWidgetId);
    
    // Connect click intent to update feed
    Intent updateIntent = new Intent(context, getAppWidgetClass());
    updateIntent.setAction(getUpdateFeedActionId());
    updateIntent.setData(appWidgetUri);
    updateIntent.putExtra("appWidgetId", appWidgetId);
    PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
    views.setOnClickPendingIntent(R.id.update_button, updatePendingIntent);

    // Connect click intent to launch details dialog
    Intent detailIntent = new Intent(context, DetailsViewChooser.class); // TODO: shouldn't this be read from Shared Preferences?
    detailIntent.putExtra("appWidgetId", appWidgetId);
    detailIntent.setData(appWidgetUri);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
        detailIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    views.setOnClickPendingIntent(R.id.widget, pendingIntent);
  }

  @Override
  protected String getShowNextItemActionId() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getShowPreviousItemActionId() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  protected String getUpdateFeedActionId() {
    return ActionIds.TinyWidget.UPDATE_FEED_ACTION;
  }

}
