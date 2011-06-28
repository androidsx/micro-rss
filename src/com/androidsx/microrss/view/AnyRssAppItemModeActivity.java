/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidsx.anyrss.view.AbstractAnyRssExpandableModeActivity;
import com.androidsx.anyrss.view.AbstractAnyRssItemModeActivity;
import com.androidsx.anyrss.view.AbstractAnyRssListModeActivity;
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

public class AnyRssAppItemModeActivity extends AbstractAnyRssItemModeActivity
    implements MobclixAdViewListener {

  private static final String TAG = "AnyRssItemModeActivity";
  
  private static final int SUBMIT_MENU_ID = Menu.FIRST;
  private static final int SHARE_MENU_ID = Menu.FIRST + 1;
  private static final int SHARE_CONTENT_MENU_ID = Menu.FIRST + 2;
  private static final int COPY_MENU_ID = Menu.FIRST + 3;
  private static final int ABOUT_MENU_ID = Menu.FIRST + 4;

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
    menu.add(0, SHARE_MENU_ID, SHARE_MENU_ID, getResources().getString(getMenuShareStringId()))
    .setIcon(android.R.drawable.ic_menu_share);
    menu.add(0, SHARE_CONTENT_MENU_ID, SHARE_CONTENT_MENU_ID, getResources().getString(getMenuShareContentStringId()))
    .setIcon(android.R.drawable.ic_menu_share);
    menu.add(0, COPY_MENU_ID, COPY_MENU_ID, getResources().getString(getMenuCopyStringId()))
    .setIcon(android.R.drawable.ic_menu_upload);
    menu.add(0, SUBMIT_MENU_ID, SUBMIT_MENU_ID, getResources().getString(R.string.menu_submit))
      .setIcon(android.R.drawable.ic_menu_send);
    menu.add(0, ABOUT_MENU_ID, ABOUT_MENU_ID, getResources().getString(R.string.menu_about))
    .setIcon(android.R.drawable.ic_menu_info_details);
    
    return true;
  }

  /** Handles item selections */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case ABOUT_MENU_ID:
        Intent aboutIntent = new Intent(this, AboutUsActivity.class);
        aboutIntent.putExtra("withtabs", true);
        startActivity(aboutIntent);
        return true;   
        
    case SHARE_MENU_ID:   
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_SEND);
      intent.setType("text/plain");
      
      // Just for the email
      intent.putExtra(Intent.EXTRA_SUBJECT, currentItem.getTitle());
      intent.putExtra(Intent.EXTRA_TEXT, 
          currentItem.getTitle() + " " + currentItem.getURL()); 
      intent.putExtra("com.twidroid.extra.MESSAGE",
          currentItem.getTitle() + " " + currentItem.getURL());
      try {
          startActivity(Intent.createChooser(intent, "Share via"));
      } catch (android.content.ActivityNotFoundException ex) {
        Log.e(TAG, "Activity not found for text/plain");
      }     
      
      return true;
    
    case SHARE_CONTENT_MENU_ID:   
      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.setType("text/plain");
      
      // Just for the email
      sendIntent.putExtra(Intent.EXTRA_SUBJECT, currentItem.getTitle());
      sendIntent.putExtra(Intent.EXTRA_TEXT, 
          AnyRSSHelper.cleanHTML(currentItem.getContent())); 
      sendIntent.putExtra("com.twidroid.extra.MESSAGE",
          AnyRSSHelper.cleanHTML(currentItem.getContent()));
      try {
          startActivity(Intent.createChooser(sendIntent, "Share content via"));
      } catch (android.content.ActivityNotFoundException ex) {
        Log.e(TAG, "Activity not found for text/plain");
      }   
      
      return true;  
      
    case COPY_MENU_ID:   
      ClipboardManager clipboard =  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);   
      
      clipboard.setText(AnyRSSHelper.cleanHTML(currentItem.getContent()));  
      Toast.makeText(this, "Item content copied", Toast.LENGTH_SHORT).show();
      
      return true;   
      
    case SUBMIT_MENU_ID:
      /* Create the Intent and fill it with our mails */
      final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

      emailIntent.setType("plain/text");
      emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
          "text@textsfromlastnight.com" });

      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "TFLN text");
      emailIntent.putExtra(Intent.EXTRA_TEXT, "(   ):\n"); 
   
      /* Send it off to the Activity-Chooser */
      startActivity(Intent.createChooser(emailIntent, "Send mail"));
      
      return true;
    }

    return false;
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
  protected int getErrorMessageStringId() {
    return R.string.details_no_data_error_html;
  }





  @Override
  protected int getItemListPositionStringId() {
    return R.string.item_list_position;
  }



  @Override
  protected int getItemModeLayoutId() {
    return R.layout.item_mode;
  }



  @Override
  protected int getNextButtonId() {
    return R.id.button_next;
  }



  @Override
  protected int getPreviousButtonId() {
    return R.id.button_previous;
  }



  @Override
  protected int getPubdateItemModeId() {
    return R.id.pubdate_item_mode;
  }



  @Override
  protected int getTitleItemModeId() {
    return R.id.title_item_mode;
  }



  @Override
  protected int getWebviewId() {
    return R.id.webview;
  }



  @Override
  protected void requestAd() {
    ((MobclixMMABannerXLAdView) findViewById(R.id.banner_adview)).getAd();
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
  protected int getMenuGoToUrlId() {
    return R.id.button_go_to_url;
  }
  
  @Override
  protected Class<? extends AbstractAnyRssExpandableModeActivity> getExpandableModeActivityClass() {
    return AnyRssExpandableModeActivity.class;
  }

  @Override
  protected Class<? extends AbstractAnyRssItemModeActivity> getItemModeActivityClass() {
    return AnyRssAppItemModeActivity.class;
  }

  @Override
  protected Class<? extends AbstractAnyRssListModeActivity> getListModeActivityClass() {
    return AnyRssListModeActivity.class;
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
  protected int getMenuCopyStringId() {
    return R.string.menu_copy;
  }

  @Override
  protected int getMenuShareContentStringId() {
    return R.string.menu_share_content;
  }

  @Override
  protected void saveLastViewTypeOpened(int viewType, int appWidgetId) {
    AnyRSSHelper.setWebviewType(this, viewType, appWidgetId, getAuthority());
  }
}
