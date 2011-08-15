package com.androidsx.microrss.view;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;
import com.wimm.framework.view.ScrollView;

public class FeedAdapter extends BaseAdapter {
    private final Activity contextActivity;
    private final Feed[] feeds;
    private MicroRssDao dao;
    
    private static final class FeedViewHolder {
        public ImageView feedImage;
        public TextView feedTitle;
        public TextView feedCount;
        public ScrollView storyList;
        public LinearLayout storyListWrapper;
    }

    public FeedAdapter(Activity contextActivity, Feed[] feeds) {
        this.feeds = feeds;
        this.contextActivity = contextActivity;
        dao = new MicroRssDao(contextActivity.getContentResolver());
    }

    @Override
    public int getCount() {
        return this.feeds.length;
    }

    @Override
    public Object getItem(int position) {
        return this.feeds[position];
    }

    @Override
    public long getItemId(int position) {
        return this.feeds[position].getId();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedViewHolder holder;

        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = contextActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.feed, null, true);
            holder = new FeedViewHolder();
            holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
            holder.feedCount = (TextView) rowView.findViewById(R.id.feed_count);
            holder.feedImage = (ImageView) rowView.findViewById(R.id.feed_image);
            
            final int feedId = (int) getItemId(position);
            
            // This DAO call is only done when the view is invalidated, which is nice
            holder.storyList = (ScrollView) rowView.findViewById(R.id.story_list);
            holder.storyListWrapper = (LinearLayout) rowView.findViewById(R.id.story_list_wrapper);
            
            List<Item> stories = dao.findStories(feedId);
            for (Item item : stories) {
                ViewGroup feedRowView = (ViewGroup) inflater.inflate(R.layout.feed_list_row, null, true);
                
                final int storyId = item.getId();
                TextView textView = (TextView) feedRowView.findViewById(R.id.title);
                textView.setText(item.getTitle());
                feedRowView.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                          Intent intent = IntentHelper.createIntent(contextActivity, null, StoryActivity.class);
                          intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
                          intent.putExtra(new StoryNavigationExtras().getCurrentIdKey(), storyId);
                          contextActivity.startActivity(intent);
                    }
                });
                holder.storyListWrapper.addView(feedRowView);
            }
            rowView.setTag(holder);
        } else {
            holder = (FeedViewHolder) rowView.getTag();
        }
        
        Feed feed = (Feed) getItem(position);
        
        holder.feedTitle.setText(feed.getTitle());
        holder.feedCount.setText(contextActivity.getString(R.string.feed_count,
                (position + 1), getCount()));
        
        Bitmap favicon = AnyRSSHelper.getBitmapFromCache(contextActivity, AnyRSSHelper.retrieveFaviconUrl(feed.getURL()));
        if (favicon != null) {
            holder.feedImage.setImageBitmap(favicon);
        }
        
        updateArrows(rowView, position, 0, getCount());
        
        return rowView;
    }

    /**
     * Get the row position associated with the specified row id in the list.
     * 
     * @param id row id of the list
     * @param defaultValue the value if not found
     * @return the position in the adapter or {@link defaultValue} if not found
     */
    public int getItemPosition(int id, int defaultPosition) {
        int position = 0;
        for (Feed feed : feeds) {
            if (feed.getId() == id) {
                return position;
            }
            position++;
        }
        return defaultPosition;
    }

    /**
     * Updates the style of the left-right arrows that provide a visual clue to the user that
     * side-navigation is possible. Currently, we just hide the arrows that don't make sense, but
     * we could also use the "dark" arrows for that.
     */
    private void updateArrows(View view, int position, int minPosition, int maxPosition) {
        String leftEnabled = contextActivity.getString(R.string.left_arrow_enabled);
        String leftDisabled = contextActivity.getString(R.string.left_arrow_disabled);
        String rightEnabled = contextActivity.getString(R.string.right_arrow_enabled);
        String rightDisabled = contextActivity.getString(R.string.right_arrow_disabled);
        
        if (position == minPosition) {
            ((TextView) view.findViewById(R.id.arrow_left)).setText(leftDisabled);
            view.findViewById(R.id.arrow_left).setVisibility(View.INVISIBLE);
            ((TextView) view.findViewById(R.id.arrow_right)).setText(rightEnabled);
            view.findViewById(R.id.arrow_right).setVisibility(View.VISIBLE);
        } else if (position + 1 == maxPosition) {
            ((TextView) view.findViewById(R.id.arrow_left)).setText(leftEnabled);
            view.findViewById(R.id.arrow_left).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.arrow_right)).setText(rightDisabled);
            view.findViewById(R.id.arrow_right).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) view.findViewById(R.id.arrow_left)).setText(leftEnabled);
            view.findViewById(R.id.arrow_left).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.arrow_right)).setText(rightEnabled);
            view.findViewById(R.id.arrow_right).setVisibility(View.VISIBLE);
        }
    }
}
