package com.androidsx.microrss.configure;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class ChooseFeedsActivity extends ListActivity {

    private static final String TAG = "ChooseFeedsActivity";
    
    private List<Feed> feeds = new ArrayList<Feed>();
    private ListView listView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feeds = new MicroRssDao(getContentResolver()).findFeeds();
        
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, feedToStringArray(feeds)));

        listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // FIXME: update in DB a field 'active', if checked or not.
        
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < feeds.size(); i++) {
            Log.v(TAG, "- " + feeds.get(i).getTitle() + ": " + checked.get(i));
        }
    }

    private static String[] feedToStringArray(List<Feed> list) {
        final String[] ret = new String[list.size()];
        int i = 0;
        for (Feed feed : list) {
            ret[i++] = feed.getTitle();
        }
        return ret;
    }
}