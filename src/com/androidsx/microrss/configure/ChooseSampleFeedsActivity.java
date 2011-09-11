package com.androidsx.microrss.configure;

import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class ChooseSampleFeedsActivity extends ChooseFeedsAbstractActivity {
	private SeparatedChooseFeedsAdapter adapter;

	@Override
	protected List<Feed> getFeeds() {
		return new MicroRssDao(getContentResolver()).findSampleFeeds();
	}

    /** FIXME: hack to put headers for eash set of feeds, every time you add/remove new feeds update
     * the ChooseSampleFeedsActivity indexes for the category!
     */
	@Override
	protected ListAdapter configureAdapter() {
		adapter = new SeparatedChooseFeedsAdapter(
				this);

		List<Feed> feeds = getFeeds();
		adapter.addSection("Tech", new ArrayAdapter<String>(this,
				R.layout.custom_simple_list_item_multiple_choice,
				feedToStringArray(feeds.subList(0, 9))));
		adapter.addSection("News", new ArrayAdapter<String>(this,
				R.layout.custom_simple_list_item_multiple_choice,
				feedToStringArray(feeds.subList(9, 18))));
		adapter.addSection("Sports", new ArrayAdapter<String>(this,
				R.layout.custom_simple_list_item_multiple_choice,
				feedToStringArray(feeds.subList(18, 23))));
		adapter.addSection("Finance", new ArrayAdapter<String>(this,
				R.layout.custom_simple_list_item_multiple_choice,
				feedToStringArray(feeds.subList(23, 27))));
		adapter.addSection("Entertainment", new ArrayAdapter<String>(this,
				R.layout.custom_simple_list_item_multiple_choice,
				feedToStringArray(feeds.subList(27, 30))));
		adapter.addSection("Other", new ArrayAdapter<String>(this,
				R.layout.custom_simple_list_item_multiple_choice,
				feedToStringArray(feeds.subList(30, feeds.size()))));
		return adapter;
	}

	@Override
	protected boolean isHeader(int position) {
		return adapter.isHeader(position);
	}

	@Override
	protected int getAdapterPosByItemPos(int position) {
		return adapter.getAdapterPosByItemPos(position);
	}

	@Override
	protected int getItemPosByAdapterPos(int position) {
		return adapter.getItemPosByAdapterPos(position);
	}
}
