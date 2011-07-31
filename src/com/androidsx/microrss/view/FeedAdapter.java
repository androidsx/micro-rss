package com.androidsx.microrss.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidsx.microrss.R;
import com.androidsx.microrss.domain.Feed;

public class FeedAdapter extends BaseAdapter {
    private Feed[] feeds;
    private Activity contextActivity;
    
    static class ViewHolder {
        public ImageView feedImage;
        public TextView feedTitle;
        public TextView feedCount;
    }

    public FeedAdapter(Activity contextActivity, Feed[] feeds) {
        this.feeds = feeds;
        this.contextActivity = contextActivity;
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
        ViewHolder holder;

        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = contextActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.feed, null, true);
            holder = new ViewHolder();
            holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
            holder.feedCount = (TextView) rowView.findViewById(R.id.feed_count);
            holder.feedImage = (ImageView) rowView.findViewById(R.id.feed_image);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
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