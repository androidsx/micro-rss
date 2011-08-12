package com.androidsx.microrss.view;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidsx.microrss.R;
import com.androidsx.microrss.domain.Item;

public final class StoryTitleAdapter extends BaseAdapter {
    private final Activity contextActivity;
    private final List<Item> stories;

    private static final class StoryViewHolder {
        public TextView storyTitle;
    }
    
    public StoryTitleAdapter(Activity contextActivity, List<Item> stories) {
        this.contextActivity = contextActivity;
        this.stories = stories;
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    @Override
    public Object getItem(int position) {
        return stories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return stories.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        StoryViewHolder holder;
        
        // Recycle existing view if passed as parameter
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = contextActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.story_list_row, null, true);
            holder = new StoryViewHolder();
            holder.storyTitle = (TextView) rowView.findViewById(R.id.story_title);
            rowView.setTag(holder);
        } else {
            holder = (StoryViewHolder) rowView.getTag();
        }
        
        Item story = (Item) getItem(position);
        holder.storyTitle.setText(story.getTitle());
        
        rowView.setTag(holder);
        return rowView;
    }
}
