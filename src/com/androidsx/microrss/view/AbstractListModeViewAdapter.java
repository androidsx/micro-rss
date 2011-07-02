package com.androidsx.microrss.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;

/**
 * Adapter for the list mode view: maintains the association between rows in the list and RSS
 * items
 */
public abstract class AbstractListModeViewAdapter extends BaseAdapter {
  
  private ItemList listItems;
  private final Context context;

  public AbstractListModeViewAdapter(Context context) {
    this.context = context;
  }
  
  public void setItemList(ItemList listItems) {
    this.listItems = listItems;
  }

  @Override
  public int getCount() {
    return listItems.getNumberOfItems();
  }

  @Override
  public Object getItem(int position) {
    return listItems.getItemAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }
  
  /**
   * Creates a custom view for a row in the list, which corresponds to an item of the RSS feed
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
        view = View.inflate(context, getListModeRowLayoutId(), null);
    }

    Item item = (Item) getItem(position);
    
    String content = AnyRSSHelper.removeHtmlTags(AnyRSSHelper.unescapeHTML(item.getContent())).trim();
    int sizeContent = content.length();
    int maxSizeContent = getMaxCharactersContentItem();
    if (sizeContent > maxSizeContent) {
      content = content.substring(0, maxSizeContent - 3) + "...";
    }
    
    // Parse the publication date into format '5 hours ago' (if any)
    String pubDateStr = AnyRSSHelper.toRelativeDateString(item.getPubDate());
    
    ((TextView) view.findViewById(getTitleListModeRowId()))
      .setText(AnyRSSHelper.unescapeHTML(item.getTitle()).trim());
    ((TextView) view.findViewById(getDescriptionListModeRowId()))
      .setText(content);
    ((TextView) view.findViewById(getPubdateListModeRowId()))
      .setText(pubDateStr);
    
    return view;
  }
  
  protected abstract int getListModeRowLayoutId();
  protected abstract int getTitleListModeRowId();
  protected abstract int getDescriptionListModeRowId();
  protected abstract int getPubdateListModeRowId();
  protected abstract int getMaxCharactersContentItem();
}
