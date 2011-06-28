package com.androidsx.microrss;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;

import com.androidsx.anyrss.AppWidgetUpdater;
import com.androidsx.anyrss.ItemList;
import com.androidsx.microrss.ClientSpecificConstants.ActionIds;
import com.androidsx.microrss.R;
import com.androidsx.anyrss.db.AppWidgets;
import com.androidsx.anyrss.view.AnyRSSHelper;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.androidsx.microrss.view.DetailsViewChooser;

/**
 * Updater for the medium-sized widget. 
 */
public class MedAppWidgetUpdater extends AppWidgetUpdater {
  private static final String TAG = MedAppWidgetUpdater.class.getSimpleName();
  
  /** Suffix to be added to the title, to fix a minor rendering issue. */
  private static final String TITLE_SUFFIX = " ";
  
  /** Index of the item we show in the widget screen. TODO: ticket #90. */
  private static final int ITEM_INDEX = 0;
  
  @Override
  protected RemoteViews fillUpView(Context context, int appWidgetId,
      ItemList itemList) {
    RemoteViews views = new RemoteViews(context.getPackageName(),
        R.layout.widget_med);

    // Use the first item that we retrieved for the title and content
    views.setTextViewText(R.id.feedTitle, itemList.getTitle() + TITLE_SUFFIX);
    views.setTextViewText(R.id.location, AnyRSSHelper.cleanHTML(itemList
        .getItemAt(ITEM_INDEX).getTitle()));
    views.setTextViewText(R.id.conditions, AnyRSSHelper.cleanHTML(itemList
        .getItemAt(ITEM_INDEX).getContent()));

    // Set the next button invisible
    views.setViewVisibility(R.id.prev_item, View.VISIBLE);
    views.setViewVisibility(R.id.next_item, View.INVISIBLE);

    // Connect the buttons to the intents
    linkButtonsToIntents(context, appWidgetId, views);

    return views;
  }
      
  @Override
  public Class<? extends AppWidgetProvider> getAppWidgetClass() {
    return MedAppWidget.class;
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
   * @param itemList the list of items to pass as extra to the details activity
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

    // Connect click intent to next item
    Intent nextItemIntent = new Intent(context, getAppWidgetClass());
    nextItemIntent.setAction(getShowNextItemActionId());
    nextItemIntent.setData(appWidgetUri);
    nextItemIntent.putExtra("appWidgetId", appWidgetId);
    PendingIntent nextItemPendingIntent = PendingIntent.getBroadcast(context, 0, nextItemIntent, 0);
    views.setOnClickPendingIntent(R.id.next_item, nextItemPendingIntent);

    // Connect click intent to previous item
    Intent prevItemIntent = new Intent(context, getAppWidgetClass());
    prevItemIntent.setAction(getShowPreviousItemActionId());
    prevItemIntent.setData(appWidgetUri);
    prevItemIntent.putExtra("appWidgetId", appWidgetId);
    PendingIntent prevItemPendingIntent = PendingIntent.getBroadcast(context, 0, prevItemIntent, 0);
    views.setOnClickPendingIntent(R.id.prev_item, prevItemPendingIntent);

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
    return ActionIds.MedWidget.SHOW_NEXT_ITEM_ACTION;
  }

  @Override
  protected String getShowPreviousItemActionId() {
    return ActionIds.MedWidget.SHOW_PREV_ITEM_ACTION;
  }
  
  @Override
  protected String getUpdateFeedActionId() {
    return ActionIds.MedWidget.UPDATE_FEED_ACTION;
  }

}
