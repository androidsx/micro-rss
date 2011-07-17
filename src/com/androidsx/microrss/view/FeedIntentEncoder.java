package com.androidsx.microrss.view;

import android.content.Intent;

class FeedIntentEncoder extends IntentEncoder {

    FeedIntentEncoder(Intent intent) {
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
