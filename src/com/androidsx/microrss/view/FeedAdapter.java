package com.androidsx.microrss.view;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;
import com.androidsx.microrss.domain.Item;

public class FeedAdapter extends BaseAdapter {
    private final Activity contextActivity;
    private final Feed[] feeds;
    private MicroRssDao dao;
    private LayoutInflater inflater;
    
    private String leftEnabledArrow;
    private String leftDisabledArrow;
    private String rightEnabledArrow;
    private String rightDisabledArrow;
    private int arrowsNormalColor;
    private int arrowsSettingsColor;
    private int errorMessageColor;

    private static final class FeedViewHolder {
        public ImageView feedImage;
        public TextView feedTitle;
        public TextView feedCount;
        public LinearLayout storyListWrapper;
        public TextView leftArrow;
        public TextView rightArrow;
    }

    public FeedAdapter(Activity contextActivity, Feed[] feeds) {
        this.feeds = feeds;
        this.contextActivity = contextActivity;
        dao = new MicroRssDao(contextActivity.getContentResolver());
        inflater = contextActivity.getLayoutInflater();
        
        leftEnabledArrow = contextActivity.getString(R.string.left_arrow_enabled);
        leftDisabledArrow = contextActivity.getString(R.string.left_arrow_disabled);
        rightEnabledArrow = contextActivity.getString(R.string.right_arrow_enabled);
        rightDisabledArrow = contextActivity.getString(R.string.right_arrow_disabled);
        
        arrowsNormalColor = contextActivity.getResources().getColor(R.color.white);
        arrowsSettingsColor = contextActivity.getResources().getColor(R.color.almost_white);
        errorMessageColor = contextActivity.getResources().getColor(R.color.error_message_info);
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

    /**
     * TODO: implement a layer of caching on dao side or something not to
     * ask DB so many times.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedViewHolder holder;

        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.feed, null, true);
            holder = new FeedViewHolder();
            holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
            holder.feedCount = (TextView) rowView.findViewById(R.id.feed_count);
            holder.feedImage = (ImageView) rowView.findViewById(R.id.feed_image);
            holder.rightArrow = (TextView) rowView.findViewById(R.id.arrow_right);
            holder.leftArrow = (TextView) rowView.findViewById(R.id.arrow_left);
            holder.storyListWrapper = (LinearLayout) rowView.findViewById(R.id.story_list_wrapper);

            rowView.setTag(holder);
        } else {
            holder = (FeedViewHolder) rowView.getTag();
        }

        Feed feed = (Feed) getItem(position);

        holder.feedTitle.setText(feed.getTitle());
        holder.feedCount.setText(contextActivity.getString(R.string.feed_count, (position + 1),
                getCount()));

        Bitmap favicon = AnyRSSHelper.getBitmapFromCache(contextActivity, AnyRSSHelper
                .retrieveFaviconUrl(feed.getURL()), R.drawable.favicon_default_brightness_100);
        if (favicon != null) {
            holder.feedImage.setImageBitmap(favicon);
        }

        final int feedId = (int) getItemId(position);
        holder.storyListWrapper.removeAllViews();
        if (feedId == Feed.SETTINGS_ID) { 
            ViewGroup settingsRowView = (ViewGroup) inflater.inflate(R.layout.button_to_settings, null, true);
            holder.storyListWrapper.addView(settingsRowView);
            holder.feedImage.setImageBitmap(null);
        } else {
            List<Item> stories = dao.findStories(feedId);
            
            if (stories.size() == 0) {
                ViewGroup noItemsfeedRowView = (ViewGroup) inflater.inflate(R.layout.error_message,
                        null, true);
    
                TextView errorMsg = (TextView) noItemsfeedRowView.findViewById(R.id.error_message);
                TextView errorMsgDetailed = (TextView) noItemsfeedRowView
                        .findViewById(R.id.error_message_detailed);
    
                errorMsg.setText(contextActivity.getString(R.string.error_message_feed_no_items));
                errorMsg.setTextColor(errorMessageColor);
    
                errorMsgDetailed.setText(contextActivity
                        .getString(R.string.error_message_feed_no_items_detailed));
                errorMsgDetailed.setTextColor(errorMessageColor);
    
                holder.storyListWrapper.addView(noItemsfeedRowView);
            } else {
                for (Item item : stories) {
                    ViewGroup feedRowView = (ViewGroup) inflater.inflate(R.layout.feed_list_row, null,
                            true);
    
                    final int storyId = item.getId();
                    TextView textView = (TextView) feedRowView.findViewById(R.id.title);
                    textView.setText(item.getTitle());
                    feedRowView.setOnClickListener(new OnClickListener() {
    
                        @Override
                        public void onClick(View v) {
                            Intent intent = IntentHelper.createIntent(contextActivity, null,
                                    StoryActivity.class);
                            intent.putExtra(new FeedNavigationExtras().getCurrentIdKey(), feedId);
                            intent.putExtra(new StoryNavigationExtras().getCurrentIdKey(), storyId);
                            contextActivity.startActivity(intent);
                        }
                    });
                    holder.storyListWrapper.addView(feedRowView);
                }
            }
        }

        updateArrows(position, 0, getCount(), holder, feedId == Feed.SETTINGS_ID);

        // it recicle the views even with the scroll! So sometimes the 8º item appears scrolled.
        rowView.scrollTo(0, 0);

        return rowView;
    }

    private View createSettingsView() {
        View view = inflater.inflate(R.layout.settings, null, true);
        return view;
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
     * side-navigation is possible. Currently, we just hide the arrows that don't make sense, but we
     * could also use the "dark" arrows for that.
     */
    private void updateArrows(int position, int minPosition, int maxPosition, FeedViewHolder holder, boolean isSettingsFeed) {
        if (position == minPosition) {
            holder.leftArrow.setText(leftDisabledArrow);
            holder.leftArrow.setVisibility(View.INVISIBLE);
            if ((position + 1) == maxPosition) {
                holder.rightArrow.setText(rightEnabledArrow);
                holder.rightArrow.setVisibility(View.INVISIBLE);
            } else {
                holder.rightArrow.setText(rightEnabledArrow);
                holder.rightArrow.setVisibility(View.VISIBLE);
            }
        } else if (position + 1 == maxPosition) {
            holder.leftArrow.setText(leftEnabledArrow);
            holder.leftArrow.setVisibility(View.VISIBLE);
            holder.rightArrow.setText(rightDisabledArrow);
            holder.rightArrow.setVisibility(View.INVISIBLE);
        } else {
            holder.leftArrow.setText(leftEnabledArrow);
            holder.leftArrow.setVisibility(View.VISIBLE);
            holder.rightArrow.setText(rightEnabledArrow);
            holder.rightArrow.setVisibility(View.VISIBLE);
        }
        
        if (isSettingsFeed) {
            holder.leftArrow.setTextColor(arrowsNormalColor);
            holder.rightArrow.setTextColor(arrowsSettingsColor);
        } else if (position == 1) {
            holder.leftArrow.setTextColor(arrowsSettingsColor);
            holder.rightArrow.setTextColor(arrowsNormalColor);
        } else {
            holder.leftArrow.setTextColor(arrowsNormalColor);
            holder.rightArrow.setTextColor(arrowsNormalColor);
        }
    }
}
