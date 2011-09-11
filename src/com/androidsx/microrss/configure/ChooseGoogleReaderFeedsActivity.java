package com.androidsx.microrss.configure;

import java.util.List;

import android.widget.ListAdapter;

import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class ChooseGoogleReaderFeedsActivity extends ChooseFeedsAbstractActivity {
	
	private FeedAdapter adapter;
    @Override
    protected List<Feed> getFeeds() {
        return new MicroRssDao(getContentResolver()).findGoogleReaderFeeds();
    }

	@Override
	protected ListAdapter configureAdapter(List<Feed> feeds) {
		adapter = new FeedAdapter(this,
                feeds);
        return adapter;  
	}

	@Override
	protected boolean isHeader(int position) {
		return false;
	}

	@Override
	protected int getAdapterPos(Feed feed) {
		int numItems = adapter.getCount();
		for (int i = 0; i < numItems; i++) {
			if (adapter.getItem(i).getId() == feed.getId()) {
				return i;
			}
		}
		return 0;
	}

	@Override
	protected Feed getFeed(int adapterPosition) {
		return adapter.getItem(adapterPosition);
	}
	
}
