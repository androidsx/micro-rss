/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.view;

import com.androidsx.anyrss.view.AbstractAnyRssListModeActivity;
import com.androidsx.anyrss.view.AbstractDetailsViewChooser;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.ContentProviderAuthority;


public class DetailsViewChooser extends AbstractDetailsViewChooser {

  @Override
  protected String getAuthority() {
    return ContentProviderAuthority.AUTHORITY;
  }

  @Override
  protected int getItemListPositionStringId() {
    return R.string.item_list_position;
  }
  
  @Override
  protected Class<? extends AbstractAnyRssListModeActivity> getListModeActivityClass() {
    return AnyRssListModeActivity.class;
  }

}
