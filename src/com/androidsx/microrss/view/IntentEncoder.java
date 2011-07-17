package com.androidsx.microrss.view;

import android.content.Context;
import android.content.Intent;

/**
 * Builds an intent to navigate to the activity to the left/right/up/down from the current one, by
 * using the existing extras to derive the extras of the new intent.
 * 
 * <pre>
 * IntentEncoder intentEncoder = new FeedIntentEncoder(getIntent());
 * if (intentDecoder.canGoLeft()) {
 *     startActivity(intentEncoder.buildGoLeftIntent(this, FeedActivity.class));
 * } else {
 *     // Can't go further to the left
 * }
 * </pre>
 */
abstract class IntentEncoder {
    private final Intent incomingIntent;

    IntentEncoder(Intent incomingIntent) {
        this.incomingIntent = incomingIntent;
    }
    
    Intent buildGoLeftIntent(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtras(incomingIntent.getExtras());
        int currentIndex = incomingIntent.getIntExtra(getCurrentIndexKey(), 0);
        intent.putExtra(getCurrentIndexKey(), currentIndex - 1);
        return intent;
    }

    Intent buildGoRightIntent(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtras(incomingIntent.getExtras());
        int currentIndex = incomingIntent.getIntExtra(getCurrentIndexKey(), 0);
        intent.putExtra(getCurrentIndexKey(), currentIndex + 1);
        return intent;
    }
    
    /** Key of the extra that holds the IDs. */
    protected abstract String getIdsKey();
    
    /** Key of the extra that holds the current index. */
    protected abstract String getCurrentIndexKey();
}
