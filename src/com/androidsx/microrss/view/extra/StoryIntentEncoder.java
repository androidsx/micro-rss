package com.androidsx.microrss.view.extra;


import android.content.Intent;

public class StoryIntentEncoder extends IntentEncoder {

    public StoryIntentEncoder(Intent intent) {
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
