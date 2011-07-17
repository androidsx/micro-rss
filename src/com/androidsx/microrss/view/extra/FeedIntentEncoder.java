package com.androidsx.microrss.view.extra;

import android.content.Intent;

public class FeedIntentEncoder extends IntentEncoder {

    public FeedIntentEncoder(Intent intent) {
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
