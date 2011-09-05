package com.androidsx.microrss.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidsx.commons.helper.ComponentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;

public class StoryAdapter extends BaseAdapter implements Draggable {
    private Activity contextActivity;
    private Item[] stories;
    private Feed feed;
    private int currentPosition = 0;

    private int colorStoryTitleWithBackground;
    private int colorFeedTitleWithBackground;
    private int colorStoryTitleWithoutBackground;
    private int colorFeedTitleWithoutBackground;
    
    static class ViewHolder {
        public ImageView storyImage;
        public TextView storyTitle;
        public TextView storyDescription;
        public TextView storyTimestamp;
        public TextView storyCount;
        public TextView storyHeader;
        public TextView feedTitle;
        public ViewGroup storyHeaderWrapper;
        public ViewGroup storyTitleWrapper;
    }

    public StoryAdapter(Activity contextActivity, Item[] stories, Feed feed) {
        this.stories = stories;
        this.contextActivity = contextActivity;
        this.feed = feed;

        this.colorStoryTitleWithBackground = contextActivity.getResources().getColor(
                R.color.story_feed_title_with_background);
        this.colorFeedTitleWithBackground = contextActivity.getResources().getColor(
                R.color.story_title_with_background);
        this.colorStoryTitleWithoutBackground = contextActivity.getResources().getColor(
                R.color.story_title);
        this.colorFeedTitleWithoutBackground = contextActivity.getResources().getColor(
                R.color.story_feed_title);
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
        currentPosition  = position;
        ViewHolder holder;

        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = contextActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.story, null, true);
            holder = new ViewHolder();
            holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
            holder.storyCount = (TextView) rowView.findViewById(R.id.story_count);
            holder.storyHeader = (TextView) rowView.findViewById(R.id.story_header);
            holder.storyDescription = (TextView) rowView.findViewById(R.id.story_description);
            holder.storyTimestamp = (TextView) rowView.findViewById(R.id.story_timestamp);
            holder.storyTitle = (TextView) rowView.findViewById(R.id.story_title);
            holder.storyImage = (ImageView) rowView.findViewById(R.id.story_image);
            holder.storyHeaderWrapper = (ViewGroup) rowView.findViewById(R.id.header_wrapper);
            holder.storyTitleWrapper = (ViewGroup) rowView.findViewById(R.id.story_title_wrapper);
            rowView.setTag(holder);

            ComponentHelper.increaseTouchArea(rowView.findViewById(R.id.header_wrapper), parent, 30);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        
        Item story = (Item) getItem(position);
        
        holder.storyTitle.setText(story.getTitle());
        holder.storyDescription.setText(AnyRSSHelper.cleanHTML(story.getContent()));
        holder.storyTimestamp.setText(AnyRSSHelper
                .toRelativeDateString(story.getPubDate()));
        holder.feedTitle.setText(feed.getTitle());
        holder.storyHeader.setText(contextActivity.getString(R.string.story_count,
                (position + 1), getCount()));
        holder.storyHeaderWrapper.setOnClickListener(onClickHeaderListener);
        //holder.storyCount.setText(contextActivity.getString(R.string.story_count,
        //        (position + 1), getCount()));
        
        Bitmap favicon = AnyRSSHelper.getBitmapFromCache(contextActivity, story.getThumbnail(), 
                -1);
        if (favicon != null) {
            // Transform the view to hold images
            RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT); 
            holder.storyImage.setLayoutParams(paramsImage);
            
            holder.storyImage.setImageBitmap(favicon);
            holder.storyImage.setVisibility(View.VISIBLE);
            
            RelativeLayout.LayoutParams paramsStoryTitleWrapper = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT, 137); //FIXME: refactor this
            holder.storyTitleWrapper.setLayoutParams(paramsStoryTitleWrapper);
            
            // TODO: duplicated code, refactor it in XML
            holder.feedTitle.setTextColor(colorFeedTitleWithBackground);
            holder.feedTitle.setBackgroundColor(R.color.story_background_feed_title);
            
            //holder.storyCount.setTextColor(contextActivity.getResources().getColor(R.color.story_feed_title_with_background));
            //holder.storyCount.setBackgroundColor(R.color.story_background_feed_title);
            
            holder.storyTitle.setMaxLines(5);
            holder.storyTitle.setPadding(3, 0, 3, 3);
            holder.storyTitle.setTextColor(colorStoryTitleWithBackground);
            holder.storyTitle.setBackgroundColor(R.color.story_background_title); 
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.story_image);
            
            holder.storyTitle.setLayoutParams(params);
        } else {
            // return to what it was
            
            //FIXME: refactor this
            RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(
                    0, 0); 
            holder.storyImage.setLayoutParams(paramsImage);
            
            holder.storyImage.setImageBitmap(null);
            holder.storyImage.setVisibility(View.INVISIBLE);
            
            RelativeLayout.LayoutParams paramsStoryTitleWrapper = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT); //FIXME: refactor this
            holder.storyTitleWrapper.setLayoutParams(paramsStoryTitleWrapper);
            
            // TODO: duplicated code, refactor it in XML
            holder.feedTitle.setTextColor(colorFeedTitleWithoutBackground);
            holder.feedTitle.setBackgroundColor(-1);
            
            //holder.storyCount.setTextColor(contextActivity.getResources().getColor(R.color.story_feed_title_with_background));
            //holder.storyCount.setBackgroundColor(R.color.story_background_feed_title);
            
            holder.storyTitle.setMaxLines(6);
            holder.storyTitle.setPadding(3, 0, 3, 10);
            holder.storyTitle.setTextColor(colorStoryTitleWithoutBackground);
            holder.storyTitle.setBackgroundColor(-1); 
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.feed_title);
            
            holder.storyTitle.setLayoutParams(params);
        }
        
        // it recicle the views even with the scroll! So sometimes the 8º item appears scrolled.
        rowView.scrollTo(0, 0);
        
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
        for (Item story : stories) {
            if (story.getId() == id) {
                return position;
            }
            position++;
        }
        return defaultPosition;
    }

    @Override
    public boolean dragCanExit() {
        View currentView = getView(currentPosition, null, null);
        StoryView storyView = (StoryView) currentView.findViewById(R.id.main_scroll_story);
        return storyView.dragCanExit();
    }
    
    private OnClickListener onClickHeaderListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
//            Intent intent = IntentHelper.createIntent(contextActivity, null, FeedActivity.class);
//            intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feed.getId());
//            contextActivity.startActivity(intent);
            
            // TODO: Android always will have the other activity on the top of the stack?
            contextActivity.finish();
        }
    };
}
