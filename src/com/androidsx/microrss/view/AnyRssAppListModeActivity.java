/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.view;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.androidsx.commons.androidutil.ApplicationVersionHelper;
import com.androidsx.commons.appinfo.AboutUsActivity;
import com.androidsx.commons.appinfo.ChangelogActivity;
import com.androidsx.microrss.ClientSpecificConstants;
import com.androidsx.microrss.R;
import com.androidsx.microrss.configure.SettingsActivity;

public class AnyRssAppListModeActivity extends AbstractAnyRssListModeActivity {
  private static final int SUBMIT_MENU_ID = Menu.FIRST + 1;
  private static final int CHANGELOG_MENU_ID = Menu.FIRST + 2;
  private static final int ABOUT_MENU_ID = Menu.FIRST + 3;

  /** Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, CHANGELOG_MENU_ID, CHANGELOG_MENU_ID, getResources().getString(R.string.menu_changelog))
      .setIcon(android.R.drawable.ic_menu_help);
    menu.add(0, SUBMIT_MENU_ID, SUBMIT_MENU_ID, getResources().getString(R.string.menu_submit))
      .setIcon(android.R.drawable.ic_menu_send);
    menu.add(0, ABOUT_MENU_ID, ABOUT_MENU_ID, getResources().getString(R.string.menu_about))
    .setIcon(android.R.drawable.ic_menu_info_details);
  
    
    return true;
  }

  /** Show some information for new users or after updates. */
  @Override
  public void onStart() {
      super.onStart();
      if (ApplicationVersionHelper.isUserOpeningAppForFirstTime(this) || 
              ApplicationVersionHelper.isUserOpeningAppAfterUpdate(this)) {
          ApplicationVersionHelper.savesCurrentVersionCode(this);
      } 
  }

  /** Handles item selections */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
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
      
    case ABOUT_MENU_ID:
        Intent aboutIntent = new Intent(this, AboutUsActivity.class);
        aboutIntent.putExtra("withtabs", true);
        startActivity(aboutIntent);
        return true;   
      
    case CHANGELOG_MENU_ID:   
      Intent settingsIntent = new Intent(this, ChangelogActivity.class);
      settingsIntent.putExtra("withtabs", true);
      startActivity(settingsIntent); 
      
      return true;
    }

    return false;
  }
  
  @Override
  protected String getAnalyticsAppKey() {
    return ClientSpecificConstants.FLURRY_APP_KEY;
  }

  @Override
  protected int getItemListPositionStringId() {
    return R.string.item_list_position;
  }

  @Override
  protected AbstractListModeViewAdapter getListModeViewAdapter() {
    return new AbstractListModeViewAdapter(this) {

      @Override
      protected int getDescriptionListModeRowId() {
        return R.id.description_list_mode_row;
      }

      @Override
      protected int getListModeRowLayoutId() {
        return R.layout.list_mode_row;
      }

      @Override
      protected int getPubdateListModeRowId() {
        return R.id.pubdate_list_mode_row;
      }

      @Override
      protected int getTitleListModeRowId() {
        return R.id.title_list_mode_row;
      }

      @Override
      protected int getMaxCharactersContentItem() {
        return 800;
      }
      
    };
  }

  @Override
  protected int getListModeLayoutId() {
    return R.layout.list_mode;
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
  protected int getTitleListModeId() {
    return R.id.title_list_mode;
  }

  @Override
  protected Class<? extends AbstractAnyRssListModeActivity> getListModeActivityClass() {
    return AnyRssAppListModeActivity.class;
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
  protected int getMenuCopyStringId() {
    return R.string.menu_copy;
  }

  @Override
  protected int getMenuShareContentStringId() {
    return R.string.menu_share_content;
  }
}