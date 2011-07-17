package com.androidsx.microrss.view;

import android.content.Intent;

class StoryIntentEncoder extends IntentEncoder {

    StoryIntentEncoder(Intent intent) {
        super(intent);
    }

    @Override
    protected String getIdsKey() {
        return ExtrasConstants.STORY_IDS;
    }

    @Override
    protected String getCurrentIndexKey() {
        return ExtrasConstants.STORY_INDEX;
    }
}
