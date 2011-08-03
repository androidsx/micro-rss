package com.androidsx.microrss.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidsx.microrss.R;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;

public class StoryAdapter extends BaseAdapter {
    private Activity contextActivity;
    private Item[] stories;
    private Feed feed;
    
    static class ViewHolder {
        public ImageView storyImage;
        public TextView storyTitle;
        public TextView storyDescription;
        public TextView storyTimestamp;
        public TextView storyCount;
        public TextView feedTitle;
    }

    public StoryAdapter(Activity contextActivity, Item[] stories, Feed feed) {
        this.stories = stories;
        this.contextActivity = contextActivity;
        this.feed = feed;
    }

    @Override
    public int getCount() {
        return this.stories.length;
    }

    @Override
    public Object getItem(int position) {
        return this.stories[position];
    }

    /** TODO: Return the Database ID */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = contextActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.story, null, true);
            holder = new ViewHolder();
            holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
            holder.storyCount = (TextView) rowView.findViewById(R.id.story_count);
            holder.storyDescription = (TextView) rowView.findViewById(R.id.story_description);
            holder.storyTimestamp = (TextView) rowView.findViewById(R.id.story_timestamp);
            holder.storyTitle = (TextView) rowView.findViewById(R.id.story_title);
            holder.storyImage = (ImageView) rowView.findViewById(R.id.story_image);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        
        Item story = (Item) getItem(position);
        
        holder.storyTitle.setText(story.getTitle());
        holder.storyDescription.setText(AnyRSSHelper.cleanHTML(story.getContent()));
        holder.storyTimestamp.setText(AnyRSSHelper
                .toRelativeDateString(story.getPubDate()));
        holder.feedTitle.setText(feed.getTitle());
        holder.storyCount.setText(contextActivity.getString(R.string.story_count,
                (position + 1), getCount()));
        
        Bitmap favicon = AnyRSSHelper.getBitmapFromCache(contextActivity, story.getThumbnail());
        if (favicon != null) {
            holder.storyImage.setImageBitmap(favicon);
            
            // TODO: duplicated code, refactor it in XML
            holder.feedTitle.setTextColor(contextActivity.getResources().getColor(R.color.story_feed_title_with_background));
            holder.feedTitle.setBackgroundColor(R.color.story_background_feed_title);
            
            holder.storyCount.setTextColor(contextActivity.getResources().getColor(R.color.story_feed_title_with_background));
            holder.storyCount.setBackgroundColor(R.color.story_background_feed_title);
            
            holder.storyTitle.setMaxLines(5);
            holder.storyTitle.setPadding(3, 0, 3, 3);
            holder.storyTitle.setTextColor(contextActivity.getResources().getColor(R.color.story_title_with_background));
            holder.storyTitle.setBackgroundColor(R.color.story_background_title); 
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.story_image);
            
            holder.storyTitle.setLayoutParams(params);
        }
        
        // it recicle the views even with the scroll! So sometimes the 8º item appears scrolled.
        rowView.scrollTo(0, 0);
        
        return rowView;
    }
    
}