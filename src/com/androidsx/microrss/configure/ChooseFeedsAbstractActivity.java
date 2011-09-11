package com.androidsx.microrss.configure;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.cache.CacheImageManager;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.view.SwipeAwareListener;

public abstract class ChooseFeedsAbstractActivity extends ListActivity {
    private static final String TAG = "ChooseFeedsAbstractActivity";
    
    private MicroRssDao dao;
    private ListView listView;
    private CacheImageManager cacheImageManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrapper_choose_greader);
        
        getListView().setOnTouchListener(swipeListener);
        
        cacheImageManager = new CacheImageManager(this);
        dao = new MicroRssDao(getContentResolver());
        
        List<Feed> feeds = getFeeds();
        setListAdapter(configureAdapter(feeds));

        listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        for (int i = 0; i < feeds.size(); i++) {
        	if (feeds.get(i).isActive()) {
        		listView.setItemChecked(getAdapterPos(feeds.get(i)), true);
        	}
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
        int adapterPosition = position;
        if (!isHeader(adapterPosition)) {
	        boolean newActiveStatus = listView.getCheckedItemPositions().get(adapterPosition);
	        Feed feed = getFeed(adapterPosition);
	        Log.i(TAG, "Update the feed " + feed.getTitle() + " as active=" + newActiveStatus);
	        dao.updateFeedActive(feed, newActiveStatus, cacheImageManager);
        }
    }

    protected static String[] feedToStringArray(List<Feed> list) {
        final String[] ret = new String[list.size()];
        int i = 0;
        for (Feed feed : list) {
            ret[i++] = feed.getTitle();
        }
        return ret;
    }
    
    protected abstract List<Feed> getFeeds();
    protected abstract ListAdapter configureAdapter(List<Feed> feeds);
    protected abstract boolean isHeader(int position);
    
    /** It might have headers, having different position from the Items you pass to the adapters */
    protected abstract int getAdapterPos(Feed feed);
    protected abstract Feed getFeed(int adapterPposition);
    
    private View.OnTouchListener swipeListener = new SwipeAwareListener() {

        @Override
        public void onTopToBottomSwipe() {
        }

        @Override
        public void onRightToLeftSwipe() {
            Intent intent = IntentHelper.createIntent(ChooseFeedsAbstractActivity.this, null,
                    Preferences.class);
            startActivity(intent);
            ChooseFeedsAbstractActivity.this
                    .overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        @Override
        public void onLeftToRightSwipe() {
        }

        @Override
        public void onBottomToTopSwipe() {
        }
    };
    
	class FeedAdapter extends ArrayAdapter<Feed> {

        private List<Feed> items;

        public FeedAdapter(Context context, List<Feed> items) {
                super(context, 1, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.custom_simple_list_item_multiple_choice, null);
                }
                Feed feed = items.get(position);
                if (feed != null) {
                        ((TextView) v.findViewById(android.R.id.text1)).setText(feed.getTitle());
                }
                return v;
        }
}
}