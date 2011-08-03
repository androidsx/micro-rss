package com.androidsx.microrss.configure;

import java.util.List;

import com.androidsx.microrss.db.dao.MicroRssDao;
import com.androidsx.microrss.domain.Feed;

public class ChooseGoogleReaderFeedsActivity extends ChooseFeedsAbstractActivity {
    @Override
    protected List<Feed> getFeeds() {
        return new MicroRssDao(getContentResolver()).findGoogleReaderFeeds();
    }
}