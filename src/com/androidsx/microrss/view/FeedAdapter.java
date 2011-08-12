package com.androidsx.microrss.view;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;

public class FeedAdapter extends BaseAdapter {
    private final Activity contextActivity;
    private final Feed[] feeds;
    private MicroRssDao dao;
    
    private static final class FeedViewHolder {
        public ImageView feedImage;
        public TextView feedTitle;
        public TextView feedCount;
        public ListView storyList;
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
            holder.storyList = (ListView) rowView.findViewById(R.id.story_list);
            List<Item> stories = dao.findStories(feedId);
            ListAdapter storyTitleAdapter = new StoryTitleAdapter(contextActivity, stories);
            holder.storyList.setAdapter(storyTitleAdapter);
            holder.storyList.setOnItemClickListener(new OnItemClickListener() {
                
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                    int storyId = (int) adapterView.getItemIdAtPosition(position);
                    Intent intent = IntentHelper.createIntent(contextActivity, null, StoryActivity.class);
                    intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
                    intent.putExtra(new StoryNavigationExtras().getCurrentIdKey(), storyId);
                    contextActivity.startActivity(intent);
                }
                
            });
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
}
