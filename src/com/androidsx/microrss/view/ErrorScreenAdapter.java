package com.androidsx.microrss.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidsx.microrss.R;

/**
 * Adapter that contains two elements: the settings view in the left, and the error message in the right.
 */
public class ErrorScreenAdapter extends BaseAdapter implements Draggable {
    private static final int NUMBER_OF_ELEMENTS = 2;
    private static final int POSITION_SETTINGS = 0;
    //private static final int POSITION_ERROR_MESSAGE = 1;
    
    private LayoutInflater inflater;
    private final int messageRes;
    private final int messageDetailedRes;
    private final int imageDrawableRes;
    private final Activity contextActivity;
    private final int textColorRes;

    private String leftEnabledArrow;
    private String leftDisabledArrow;
    private String rightEnabledArrow;
    private String rightDisabledArrow;
    
    static class ViewHolder {
        // For the settings
        public ImageView feedImage;
        public LinearLayout storyListWrapper;
        
        // For the error message
        public ImageView errorImage;
        public TextView errorMessage;
        public TextView errorMessageDetailed;
        
        // For the header
        public TextView feedTitle;
        public TextView leftArrow;
        public TextView rightArrow;
    }

    public ErrorScreenAdapter(Activity contextActivity, int messageRes, int messageDetailedRes,
            int imageDrawableRes, int textColorRes) {
        this.contextActivity = contextActivity;
        this.messageRes = messageRes;
        this.messageDetailedRes = messageDetailedRes;
        this.imageDrawableRes = imageDrawableRes;
        this.textColorRes = textColorRes;
        inflater = contextActivity.getLayoutInflater();
        
        leftEnabledArrow = contextActivity.getString(R.string.left_arrow_enabled);
        leftDisabledArrow = contextActivity.getString(R.string.left_arrow_disabled);
        rightEnabledArrow = contextActivity.getString(R.string.right_arrow_enabled);
        rightDisabledArrow = contextActivity.getString(R.string.right_arrow_disabled);
    }

    @Override
    public int getCount() {
        return NUMBER_OF_ELEMENTS;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        ViewHolder holder;
        if (position == POSITION_SETTINGS) {
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.feed, null, true);
                holder = new ViewHolder();
                holder.feedImage = (ImageView) rowView.findViewById(R.id.feed_image);
                holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
                holder.rightArrow = (TextView) rowView.findViewById(R.id.arrow_right);
                holder.leftArrow = (TextView) rowView.findViewById(R.id.arrow_left);
                holder.storyListWrapper = (LinearLayout) rowView.findViewById(R.id.story_list_wrapper);
                
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
            ViewGroup settingsRowView = (ViewGroup) inflater.inflate(R.layout.button_to_settings, null, true);
            holder.storyListWrapper.addView(settingsRowView);
            holder.feedImage.setImageBitmap(null);
            
            holder.leftArrow.setText(leftDisabledArrow);
            holder.leftArrow.setVisibility(View.INVISIBLE);
            holder.rightArrow.setText(rightEnabledArrow);
            holder.rightArrow.setVisibility(View.VISIBLE);
            
        } else {
            if (rowView == null) {
                LayoutInflater inflater = contextActivity.getLayoutInflater();
                rowView = inflater.inflate(R.layout.error_screen, null, true);
                holder = new ViewHolder();
                holder.feedImage = (ImageView) rowView.findViewById(R.id.feed_image);
                holder.feedTitle = (TextView) rowView.findViewById(R.id.feed_title);
                holder.rightArrow = (TextView) rowView.findViewById(R.id.arrow_right);
                holder.leftArrow = (TextView) rowView.findViewById(R.id.arrow_left);
                holder.errorMessage = (TextView) rowView.findViewById(R.id.error_message);
                holder.errorMessageDetailed = (TextView) rowView
                .findViewById(R.id.error_message_detailed);
                holder.errorImage = (ImageView) rowView.findViewById(R.id.error_image);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
            
            holder.errorMessage.setText(contextActivity.getString(messageRes));
            holder.errorMessageDetailed.setText(contextActivity.getString(messageDetailedRes));
            holder.feedImage.setImageBitmap(null);
            
            if (textColorRes != -1) {
                holder.errorMessage.setTextColor(textColorRes);
                holder.errorMessageDetailed.setTextColor(textColorRes);
            }
            
            holder.errorImage.setImageResource(imageDrawableRes);
            
            holder.leftArrow.setText(leftEnabledArrow);
            holder.leftArrow.setVisibility(View.VISIBLE);
            holder.rightArrow.setText(rightDisabledArrow);
            holder.rightArrow.setVisibility(View.INVISIBLE);
        }

        return rowView;
    }

    @Override
    public boolean dragCanExit() {
        return true;
    }

}