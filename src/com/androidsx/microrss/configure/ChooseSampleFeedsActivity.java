package com.androidsx.microrss.configure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.widget.ListAdapter;

import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class ChooseSampleFeedsActivity extends ChooseFeedsAbstractActivity {
	private SeparatedChooseFeedsAdapter adapter;
	
	private Map<String, List<Feed>> mapCategoriesWithFeeds = new LinkedHashMap<String, List<Feed>>();

	@Override
	protected List<Feed> getFeeds() {
		return new MicroRssDao(getContentResolver()).findSampleFeeds();
	}

	@Override
	protected ListAdapter configureAdapter(List<Feed> feeds) {
		adapter = new SeparatedChooseFeedsAdapter(
				this);

		for (Feed feed : feeds) {
			List<Feed> categoryFeeds = mapCategoriesWithFeeds.get(feed.getCategory());
			if (categoryFeeds == null) {
				categoryFeeds = new ArrayList<Feed>();
				categoryFeeds.add(feed);
				mapCategoriesWithFeeds.put(feed.getCategory(), categoryFeeds);
			} else {
				categoryFeeds.add(feed);
			}
		}
		
		for (String category : mapCategoriesWithFeeds.keySet()) {
			adapter.addSection(category, new FeedAdapter(this, mapCategoriesWithFeeds.get(category)));
		}

		return adapter;
	}

	@Override
	protected boolean isHeader(int position) {
		return adapter.isHeader(position);
	}

	@Override
	protected int getAdapterPos(Feed feed) {
		int numItems = adapter.getCount();
		for (int i = 0; i < numItems; i++) {
			Object item = adapter.getItem(i);
			if (item != null && item instanceof Feed && ((Feed) item).getId() == feed.getId()) {
				return i;
			}
		}
		return 0;
	}

	@Override
	protected Feed getFeed(int adapterPposition) {
		return (Feed) ((!isHeader(adapterPposition)) ? adapter.getItem(adapterPposition) : null);
	}
}
