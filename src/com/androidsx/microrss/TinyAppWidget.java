package com.androidsx.microrss;

import android.appwidget.AppWidgetProvider;
import android.widget.RemoteViews;

import com.androidsx.anyrss.AbstractAppWidget;
import com.androidsx.anyrss.AbstractUpdateService;
import com.androidsx.microrss.ClientSpecificConstants.ActionIds;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.ContentProviderAuthority;

public class TinyAppWidget extends AbstractAppWidget {

  @Override
  protected void updateViewWhileUpdating(RemoteViews views) {
      views.setTextViewText(getFeedTitleId(), "Updating feed...");
  }

@Override
  protected Class<? extends AppWidgetProvider> getAppWidgetClass() {
    return TinyAppWidget.class;
  }

  @Override
  protected String getAuthority() {
    return ContentProviderAuthority.AUTHORITY;
  }

  @Override
  protected int getWidgetId() {
    return R.id.widget;
  }
  
  @Override
  protected int getWidgetLayoutId() {
    return R.layout.widget_tiny;
  }

  @Override
  protected int getNextItemId() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected int getPrevItemId() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Class<? extends AbstractUpdateService> getUpdateServiceClass() {
    return UpdateService.class;
  }

  @Override
  protected int getLocationId() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected int getConditionsId() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected int getFeedTitleId() {
    return R.id.feedTitle;
  }

  @Override
  protected int getUpdateButtonId() {
    return R.id.update_button;
  }

  @Override
  protected String getShowNextItemActionId() {
    return ActionIds.TinyWidget.NOT_SUPPORTED_ACTION;
  }

  @Override
  protected String getShowPreviousItemActionId() {
    return ActionIds.TinyWidget.NOT_SUPPORTED_ACTION;
  }
  
  @Override
  protected String getUpdateFeedActionId() {
    return ActionIds.TinyWidget.UPDATE_FEED_ACTION;
  }
  
}
