/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.androidsx.anyrss.Item;
import com.androidsx.anyrss.view.AbstractAnyRssExpandableModeActivity;
import com.androidsx.anyrss.view.AbstractAnyRssItemModeActivity;
import com.androidsx.anyrss.view.AbstractAnyRssListModeActivity;
import com.androidsx.anyrss.view.AbstractExpandableModeViewAdapter;
import com.androidsx.anyrss.view.AnyRSSHelper;
import com.androidsx.commons.androidutil.ApplicationVersionHelper;
import com.androidsx.commons.appinfo.AboutUsActivity;
import com.androidsx.microrss.ClientSpecificConstants;
import com.androidsx.microrss.InfoActivity;
import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.SettingsActivity;
import com.androidsx.microrss.db.ContentProviderAuthority;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

/**
 * Class for an activity that shows a list of {@link Item} on a {@link ListView}. 
 * <p>
 * Receives as extra data the list of items and generally it will be instantiated when user
 * clicks on a widget with the state {@link AnyRssUpdateService.WidgetState#SUCCESS}.
 */
public class AnyRssExpandableModeActivity extends AbstractAnyRssExpandableModeActivity
    implements MobclixAdViewListener {

  private static final String TAG = "AnyRssExpandableModeActivity";
  
  private static final int SUBMIT_MENU_ID = Menu.FIRST + 2;
  private static final int ABOUT_MENU_ID = Menu.FIRST + 3;

  /** Show some information for new users or after updates. */
  @Override
  public void onStart() {
      super.onStart();
      if (ApplicationVersionHelper.isUserOpeningAppForFirstTime(this) || 
              ApplicationVersionHelper.isUserOpeningAppAfterUpdate(this)) {
          ApplicationVersionHelper.savesCurrentVersionCode(this);
          startActivity(new Intent(this, InfoActivity.class)); 
      } 
  }

  /** Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0, SUBMIT_MENU_ID, 0, getResources().getString(R.string.menu_submit))
      .setIcon(android.R.drawable.ic_menu_send);
    menu.add(0, ABOUT_MENU_ID, ABOUT_MENU_ID, getResources().getString(R.string.menu_about))
    .setIcon(android.R.drawable.ic_menu_info_details);
  
    
    return true;
  }

  /** Handles item selections */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean result = super.onOptionsItemSelected(item);
    
    if (item.getItemId() == SUBMIT_MENU_ID) {
      /* Create the Intent and fill it with our mails */
      final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

      emailIntent.setType("plain/text");
      emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
          "text@textsfromlastnight.com" });

      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "TFLN text");
      emailIntent.putExtra(Intent.EXTRA_TEXT, "(   ):\n"); 
   
      /* Send it off to the Activity-Chooser */
      startActivity(Intent.createChooser(emailIntent, "Send mail"));
      
      result = true;      
    } else if (item.getItemId() == ABOUT_MENU_ID) {
        Intent aboutIntent = new Intent(this, AboutUsActivity.class);
        aboutIntent.putExtra("withtabs", true);
        startActivity(aboutIntent);
        return true;   
    }

    return result;
  }
  
  @Override
  public void onSuccessfulLoad(MobclixAdView view) {
      Log.v(TAG, "The ad request was successful!");
  }

  @Override
  public void onFailedLoad(MobclixAdView view, int errorCode) {
      Log.w(TAG, "The ad request failed with error code: " + errorCode);
  }


  public boolean shouldTouchThrough(MobclixAdView adView) {
      return true;
  }

  @Override
  public void onCustomAdTouchThrough(MobclixAdView adView, String string) {
      Log.v("MobclixAdvertisingView", "The custom ad responded with '"
              + string + "' when touched!");
  }

  /**
   * Called when the MobclixAdView is retrieving an ad. Return a comma
   * separated list of keyword terms describing the application if available.
   */
  @Override
  public String keywords() {
      return "android,application,app,sex,porn,night,drunk,party";
  }

  /**
   * Called when the MobclixAdView is retrieving an ad. Return a comma
   * separated list of search terms if available.
   */
  @Override
  public String query() {
      return keywords();
  }
  
  @Override
  protected String getAnalyticsAppKey() {
    return ClientSpecificConstants.FLURRY_APP_KEY;
  }

  @Override
  protected String getAuthority() {
    return ContentProviderAuthority.AUTHORITY;
  }

  @Override
  protected int getItemListPositionStringId() {
    return R.string.item_list_position;
  }

  @Override
  protected AbstractExpandableModeViewAdapter getExpandableModeViewAdapter() {
    return new AbstractExpandableModeViewAdapter(this) {

      @Override
      protected int getExpandableModeChildLayoutId() {
        return R.layout.expandable_mode_child;
      }

      @Override
      protected int getExpandableModeGroupLayoutId() {
        return R.layout.expandable_mode_group;
      }

      @Override
      protected int getPubdateExpandableModeRowId() {
        return R.id.pubdate_list_mode_row;
      }

      @Override
      protected int getTitleExpandableModeRowId() {
        return R.id.title_list_mode_row;
      }

      @Override
      protected int getWebviewId() {
        return R.id.webview;
      }
      
    };
  }

  @Override
  protected int getExpandableModeLayoutId() {
    return R.layout.expandable_mode;
  }
  
  @Override
  protected int getMenuChangeViewStringId() {
    return R.string.menu_change_view;
  }

  @Override
  protected int getMenuExpandableViewStringId() {
    return R.string.menu_expandable_mode;
  }

  @Override
  protected int getMenuListViewStringId() {
    return R.string.menu_list_mode;
  }

  @Override
  protected int getMenuSingleViewStringId() {
    return R.string.menu_single_mode;
  }

  @Override
  protected int getTitleExpandableModeId() {
    return R.id.title_list_mode;
  }

  @Override
  protected void requestAd() {
    ((MobclixMMABannerXLAdView) findViewById(R.id.banner_adview)).getAd();
  }

  @Override
  protected Class<? extends AbstractAnyRssExpandableModeActivity> getExpandableModeActivityClass() {
    return AnyRssExpandableModeActivity.class;
  }

  @Override
  protected Class<? extends AbstractAnyRssItemModeActivity> getItemModeActivityClass() {
    return AnyRssItemModeActivity.class;
  }

  @Override
  protected Class<? extends AbstractAnyRssListModeActivity> getListModeActivityClass() {
    return AnyRssListModeActivity.class;
  }

  @Override
  protected int getMenuGoToUrlStringId() {
    return R.string.menu_go_to_url;
  }

  @Override
  protected int getMenuOpenItemModeStringId() {
    return R.string.list_mode_options_open_item_mode;
  }

  @Override
  protected int getMenuOptionsTitleStringId() {
    return R.string.list_mode_options_title;
  }

  @Override
  protected int getMenuSettingsStringId() {
    return R.string.menu_settings;
  }

  @Override
  protected int getMenuShareStringId() {
    return R.string.menu_share;
  }

  @Override
  protected Class<? extends Activity> getSettingsActivityClass() {
    return SettingsActivity.class;
  }

  @Override
  protected void saveLastViewTypeOpened(int viewType, int appWidgetId) {
    AnyRSSHelper.setWebviewType(this, viewType, appWidgetId, getAuthority());
  }

}
