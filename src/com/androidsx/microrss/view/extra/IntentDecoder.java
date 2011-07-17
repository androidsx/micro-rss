package com.androidsx.microrss.view.extra;

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
public abstract class IntentDecoder {
    private final int[] ids;
    private final int currentIndex;

    protected IntentDecoder(Intent incomingIntent) {
        ids = incomingIntent.getIntArrayExtra(getIdsKey());
        currentIndex = incomingIntent.getIntExtra(getCurrentIndexKey(), -1);
    }

    public boolean isValidIndex() {
        return currentIndex >= 0 && currentIndex < ids.length;
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < ids.length;
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    /** Only meaningful if {@code isValidIndex() == true}. */
    public int getCurrentId() {
        return ids[currentIndex];
    }
    
    public int getCount() {
        return ids.length;
    }
    
    public boolean canGoLeft() {
        return isValidIndex(currentIndex - 1);
    }
    
    public boolean canGoRight() {
        return isValidIndex(currentIndex + 1);
    }
    
    /** Key of the extra that holds the IDs. */
    protected abstract String getIdsKey();
    
    /** Key of the extra that holds the current index. */
    protected abstract String getCurrentIndexKey();
}
