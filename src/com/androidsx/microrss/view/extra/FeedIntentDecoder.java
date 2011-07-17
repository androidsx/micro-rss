package com.androidsx.microrss.view.extra;

import android.content.Intent;

public class FeedIntentDecoder extends IntentDecoder {

    public FeedIntentDecoder(Intent intent) {
        super(intent);
    }

    @Override
    protected String getIdsKey() {
        return ExtrasConstants.FEED_IDS;
    }

    @Override
    protected String getCurrentIndexKey() {
        return ExtrasConstants.FEED_INDEX;
    }
}
