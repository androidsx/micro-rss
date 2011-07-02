/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file.
 * Otherwise see <http://www.opensource.org/licenses/mit-license.php>  
 */
package com.androidsx.microrss.view;

import java.util.zip.DataFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidsx.microrss.FlurryConstants;
import com.androidsx.microrss.db.FeedColumns;
import com.androidsx.microrss.db.RssItemsDao;
import com.androidsx.microrss.db.SqLiteRssItemsDao;
import com.androidsx.microrss.domain.Item;
import com.androidsx.microrss.domain.ItemList;
import com.flurry.android.FlurryAgent;

/**
 * Class for an activity that shows a list of {@link Item} on a {@link ListView}. 
 * <p>
 * Receives as extra data the appWidgetId identifying the widget, the current position in
 * the list and the list of items (@link ItemList)
 */
public abstract class AbstractAnyRssListModeActivity extends ListActivity {

  private static final String TAG = "AnyRssListModeActivity";
  
  private static final int CHANGE_VIEW_MENU_ID = Menu.FIRST;
  private static final int SETTINGS_MENU_ID = Menu.FIRST + 1;
  
  private ItemList itemList;
  private int appWidgetId;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      Log.i(TAG, "list mode onCreate");
    super.onCreate(savedInstanceState);
    
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(getListModeLayoutId());
    registerForContextMenu(getListView());
    
    AbstractListModeViewAdapter adapter = null;
    int currentItemPos = -1;
    
    try {
      Bundle extras = getIntent().getExtras();
      if (extras == null) {
        throw new DataFormatException();
      }

      appWidgetId = extras.getInt("appWidgetId");
      itemList = retrieveItemList(extras, new SqLiteRssItemsDao(), appWidgetId);
      if (itemList == null) {
        throw new DataFormatException();
      }
      currentItemPos = extras.getInt(getResources().getResourceEntryName(
          getItemListPositionStringId()), -1);
   
      // Set the title of the list item
      ((TextView) findViewById(getTitleListModeId())).setText(itemList.getTitle());
      
      Log.e("WIMM", "abstract any rss list mode activity: set item list with " + itemList.getNumberOfItems() + " items");
      
      adapter = getListModeViewAdapter();
      adapter.setItemList(itemList);
      
    } catch (Exception e) { // We can't let the activity crash, so we catch any exception
      Log.e(TAG, "Unknown error while data from main activity", e);
      FlurryAgent.onError(FlurryConstants.ERROR_ID_PASS_DATA, "Can't pass data to list mode", e.getClass().toString());
    } 
    
    // If an error occurs, a message with no items is shown in the layout of the listview
    setListAdapter(adapter);
   
    // Only if it comes from the medwidget and you are in position different from the initial
    if ( currentItemPos > 0) {
      setSelection(currentItemPos);
    }
    
    saveLastViewTypeOpened(FeedColumns.WEBVIEW_TYPE_LIST, appWidgetId);
  }
  
  /**
  * Retrieve the item list. First we look for the list on the extra data passed to the activity, if
  * so, <i>probably</i> we are in application mode. If extra data is empty we retrieve the list
  * from the dao, because probably the activity comes from widget view.
  */
  private ItemList retrieveItemList(Bundle extras, RssItemsDao dao, int appWidgetId) {
     ItemList itemListFromExtras = (ItemList) extras.getSerializable("itemList");
     return itemListFromExtras == null ? dao.getItemList(getContentResolver(),
        appWidgetId) : itemListFromExtras;
  }
  
  @Override
  protected void onStart() {
      super.onStart();
      FlurryAgent.onStartSession(this, getAnalyticsAppKey());
  }

  
  @Override
  protected void onStop() {
      super.onStop();
      FlurryAgent.onEndSession(this);
  }
  
  /**
   * This method will be called when an item in the list is selected
   */
  @Override
  protected void onListItemClick(ListView l, View v, final int position, long id) {
    super.onListItemClick(l, v, position, id);
    
    new AlertDialog.Builder(this).setTitle(
        getResources().getString(getMenuOptionsTitleStringId())).setItems(
        new String[] { getResources().getString(getMenuOpenItemModeStringId()),
            getResources().getString(getMenuShareStringId()),
            getResources().getString(getMenuShareContentStringId()),
            getResources().getString(getMenuCopyStringId()),
            getResources().getString(getMenuGoToUrlStringId()) },
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
              createIntentAndStartActivity(getListModeActivityClass(), position, false);
            } else if(which == 1) {
              Item currentItem = (Item) getListAdapter().getItem(position);
              Intent sendIntent = new Intent();
              sendIntent.setAction(Intent.ACTION_SEND);
              sendIntent.setType("text/plain");
              
              // Just for the email
              sendIntent.putExtra(Intent.EXTRA_SUBJECT, currentItem.getTitle());
              sendIntent.putExtra(Intent.EXTRA_TEXT, 
                  currentItem.getTitle() + " " + currentItem.getURL()); 
              sendIntent.putExtra("com.twidroid.extra.MESSAGE",
                  currentItem.getTitle() + " " + currentItem.getURL());
              try {
                  startActivity(Intent.createChooser(sendIntent, "Share via"));
              } catch (android.content.ActivityNotFoundException ex) {
                Log.e(TAG, "Activity not found for text/plain");
              }   
            } else if(which == 2) {
              Item currentItem = (Item) getListAdapter().getItem(position);
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
            } else if(which == 3) {
              Item currentItem = (Item) getListAdapter().getItem(position);
              ClipboardManager clipboard =  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);   
            
              clipboard.setText(AnyRSSHelper.cleanHTML(currentItem.getContent()));  
              Toast.makeText(AbstractAnyRssListModeActivity.this, "Item content copied", Toast.LENGTH_SHORT).show();
            } else {
              Intent intent = new Intent(Intent.ACTION_VIEW);
              String url = ((Item) getListAdapter().getItem(position)).getURL();
              intent.setData(Uri.parse(url));
              startActivity(intent);
            }
          }
        }).show();
  }
  
  /** Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, CHANGE_VIEW_MENU_ID, CHANGE_VIEW_MENU_ID,  getResources().getString(getMenuChangeViewStringId()))
      .setIcon(android.R.drawable.ic_menu_view); 
    menu.add(0, SETTINGS_MENU_ID, SETTINGS_MENU_ID, getResources().getString(getMenuSettingsStringId()))
    .setIcon(android.R.drawable.ic_menu_preferences);
    
    return true;
  }

  /** Handles item selections */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case CHANGE_VIEW_MENU_ID:
      new AlertDialog.Builder(this)
      .setTitle(getResources().getString(getMenuChangeViewStringId()))
      .setSingleChoiceItems(
        new String[] { 
            getResources().getString(getMenuSingleViewStringId()), 
            getResources().getString(getMenuListViewStringId()),
            getResources().getString(getMenuExpandableViewStringId()) 
        },
        1,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final Class<? extends Activity> activityClass = getListModeActivityClass();
            if (activityClass != getListModeActivityClass()) {
              createIntentAndStartActivity(activityClass, 0, true);
            }
            dialog.cancel();
          }
        })
        .show();
      
      return true;
      
    case SETTINGS_MENU_ID:   
      Intent settingsIntent = new Intent(this, getSettingsActivityClass());
      settingsIntent.putExtra("appWidgetId", appWidgetId);
      startActivity(settingsIntent); 
      
      return true;
    }

    return false;
  }
  
  /**
   * Starts an activity adding as extra data the list of items. It also finish the 
   * current activity in order to have only one widget activity running independent of the view mode.
   * 
   * @param activityClass the class of the activity
   * @param finishCurrentActivity
   */
  protected void createIntentAndStartActivity(final Class<? extends Activity> activityClass, int position, boolean finishCurrentActivity) {
    Intent intent = new Intent(this, activityClass);
    intent.putExtra(getResources().getResourceEntryName(getItemListPositionStringId()), position);
    intent.putExtra("appWidgetId", appWidgetId);
    intent.putExtra("itemList", itemList);
    startActivity(intent);
    
    if(finishCurrentActivity == true) {
      finish();
    }
  }

  protected abstract String getAnalyticsAppKey();
  
  protected abstract int getListModeLayoutId();
  protected abstract int getItemListPositionStringId();
  protected abstract int getTitleListModeId();
  
  protected abstract int getMenuChangeViewStringId();
  protected abstract int getMenuSingleViewStringId();
  protected abstract int getMenuExpandableViewStringId();
  protected abstract int getMenuListViewStringId();
  protected abstract int getMenuOptionsTitleStringId();
  protected abstract int getMenuShareStringId();
  protected abstract int getMenuGoToUrlStringId();
  protected abstract int getMenuOpenItemModeStringId();
  protected abstract int getMenuSettingsStringId();
  protected abstract int getMenuShareContentStringId();
  protected abstract int getMenuCopyStringId();
  
  protected abstract Class<? extends AbstractAnyRssListModeActivity> getListModeActivityClass();
  protected abstract Class<? extends Activity> getSettingsActivityClass();
  
  
  protected abstract AbstractListModeViewAdapter getListModeViewAdapter();
  
  protected abstract void saveLastViewTypeOpened(int viewType, int appWidgetId);

}