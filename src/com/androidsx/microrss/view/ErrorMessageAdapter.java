package com.androidsx.microrss.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidsx.microrss.R;

public class ErrorMessageAdapter extends BaseAdapter implements Draggable {

    private final int messageRes;
    private final int messageDetailedRes;
    private final int imageDrawableRes;
    private final Activity contextActivity;
    private final int textColorRes;

    static class ViewHolder {
        public ImageView errorImage;
        public TextView errorMessage;
        public TextView errorMessageDetailed;
    }

    public ErrorMessageAdapter(Activity contextActivity, int messageRes, int messageDetailedRes,
            int imageDrawableRes, int textColorRes) {
        this.contextActivity = contextActivity;
        this.messageRes = messageRes;
        this.messageDetailedRes = messageDetailedRes;
        this.imageDrawableRes = imageDrawableRes;
        this.textColorRes = textColorRes;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return messageRes;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = contextActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.error_message, null, true);
            holder = new ViewHolder();
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
        
        if (textColorRes != -1) {
            holder.errorMessage.setTextColor(textColorRes);
            holder.errorMessageDetailed.setTextColor(textColorRes);
        }
        
        holder.errorImage.setImageResource(imageDrawableRes);

        return rowView;
    }

    @Override
    public boolean dragCanExit() {
        return true;
    }

}