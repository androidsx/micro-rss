package com.androidsx.microrss.configure;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.cache.CacheImageManager;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.view.FeedActivity;
import com.androidsx.microrss.view.SwipeAwareListener;

public abstract class ChooseFeedsAbstractActivity extends ListActivity {
    private static final String TAG = "ChooseFeedsAbstractActivity";
    
    private MicroRssDao dao;
    private List<Feed> feeds;
    private ListView listView;
    private CacheImageManager cacheImageManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrapper_choose_greader);
        
        getListView().setOnTouchListener(swipeListener);
        
        dao = new MicroRssDao(getContentResolver());
        feeds = getFeeds();
        cacheImageManager = new CacheImageManager(this);
        
        setListAdapter(new ArrayAdapter<String>(this,
                R.layout.custom_simple_list_item_multiple_choice, feedToStringArray(feeds)));

        listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        for (int i = 0; i < feeds.size(); i++) {
            listView.setItemChecked(i, feeds.get(i).isActive());
        }
    }
    
    public void onGoBackClick(View target) {
        Intent intent = IntentHelper.createIntent(ChooseFeedsAbstractActivity.this, null,
                Preferences.class);
        startActivity(intent);
    }
    
    /**
     * TODO: Refresh all views somehow. Not done because, once the WIMM Portal is in place, we
     * probably won't need to do such a thing
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        boolean newActiveStatus = listView.getCheckedItemPositions().get(position);
        Feed feed = feeds.get(position);
        Log.i(TAG, "Update the feed " + feed.getTitle() + " as active=" + newActiveStatus);
        dao.updateFeedActive(feed, newActiveStatus, cacheImageManager);
    }

    private static String[] feedToStringArray(List<Feed> list) {
        final String[] ret = new String[list.size()];
        int i = 0;
        for (Feed feed : list) {
            ret[i++] = feed.getTitle();
        }
        return ret;
    }
    
    protected abstract List<Feed> getFeeds();
    
    private View.OnTouchListener swipeListener = new SwipeAwareListener() {

        @Override
        public void onTopToBottomSwipe() {
        }

        @Override
        public void onRightToLeftSwipe() {
        }

        @Override
        public void onLeftToRightSwipe() {
            Intent intent = IntentHelper.createIntent(ChooseFeedsAbstractActivity.this, null,
                    Preferences.class);
            startActivity(intent);
            ChooseFeedsAbstractActivity.this
                    .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        @Override
        public void onBottomToTopSwipe() {
        }
    };
}