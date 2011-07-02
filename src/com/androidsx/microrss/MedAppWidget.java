package com.androidsx.microrss;

import android.appwidget.AppWidgetProvider;

import com.androidsx.anyrss.AbstractAppWidget;
import com.androidsx.anyrss.AbstractUpdateService;
import com.androidsx.microrss.ClientSpecificConstants.ActionIds;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.ContentProviderAuthority;

public class MedAppWidget extends AbstractAppWidget {

  @Override
  protected Class<? extends AppWidgetProvider> getAppWidgetClass() {
    return MedAppWidget.class;
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
    return R.layout.widget_med;
  }

  @Override
  protected int getNextItemId() {
    return R.id.next_item;
  }

  @Override
  protected int getPrevItemId() {
    return R.id.prev_item;
  }

  @Override
  protected Class<? extends AbstractUpdateService> getUpdateServiceClass() {
    return UpdateService.class;
  }

  @Override
  protected int getLocationId() {
    return R.id.location;
  }

  @Override
  protected int getConditionsId() {
    return R.id.conditions;
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
