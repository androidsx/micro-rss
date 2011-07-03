/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.view;

import com.androidsx.microrss.FlurryConstants;
import com.androidsx.microrss.R;

public class AnyRssAppListModeActivity extends AbstractAnyRssListModeActivity {

  /** Store the install version code in the preferences. */
  @Override
  public void onStart() {
      super.onStart();
  }

  @Override
  protected String getAnalyticsAppKey() {
    return FlurryConstants.FLURRY_APP_KEY;
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