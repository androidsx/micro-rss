package com.androidsx.microrss.view;

import android.content.Intent;

/**
 * Decodes an incoming intent, by providing direct access to the information contained in its
 * extras. It should usually go like this:
 * 
 * <pre>
 * IntentDecoder intentDecoder = new FeedIntentDecoder(getIntent());
 * if (intentDecoder.isValidIndex()) {
 *     // Retrieve the feed with the ID intentDecoder.getCurrentId()
 *     // Update the UI using intentDecoder.getCount()
 *     // ...
 * } else {
 *     Log.e(TAG,
 *             &quot;Wrong index: &quot; + intentDecoder.getCurrentIndex() + &quot;);
 *     finish();
 * }
 * </pre>
 */
abstract class IntentDecoder {
    private final int[] ids;
    private final int currentIndex;

    public IntentDecoder(Intent intent) {
        ids = intent.getIntArrayExtra(getIdsKey());
        currentIndex = intent.getIntExtra(getCurrentIndexKey(), -1);
    }

    boolean isValidIndex() {
        return currentIndex >= 0 && currentIndex < ids.length;
    }

    int getCurrentIndex() {
        return currentIndex;
    }
    
    /** Only meaningful if {@code isValidIndex() == true}. */
    int getCurrentId() {
        return ids[currentIndex];
    }
    
    int getCount() {
        return ids.length;
    }
    
    /** Key of the extra that holds the IDs. */
    protected abstract String getIdsKey();
    
    /** Key of the extra that holds the current index. */
    protected abstract String getCurrentIndexKey();
}
