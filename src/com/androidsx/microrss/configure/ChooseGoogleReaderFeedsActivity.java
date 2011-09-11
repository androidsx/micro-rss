package com.androidsx.microrss.configure;

import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.androidsx.microrss.R;
import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class ChooseGoogleReaderFeedsActivity extends ChooseFeedsAbstractActivity {
    @Override
    protected List<Feed> getFeeds() {
        return new MicroRssDao(getContentResolver()).findGoogleReaderFeeds();
    }

	@Override
	protected ListAdapter configureAdapter() {
        return new ArrayAdapter<String>(this,
                R.layout.custom_simple_list_item_multiple_choice, feedToStringArray(getFeeds()));  
	}

	@Override
	protected boolean isHeader(int position) {
		return false;
	}

	@Override
	protected int getAdapterPosByItemPos(int position) {
		return position;
	}

	@Override
	protected int getItemPosByAdapterPos(int position) {
		return position;
	}
}
