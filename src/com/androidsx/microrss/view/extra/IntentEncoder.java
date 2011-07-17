package com.androidsx.microrss.view.extra;

import com.androidsx.microrss.view.NavigationExtras;

import android.content.Context;
import android.content.Intent;

/**
 * Builds an intent to navigate to the activity to the left/right/up/down from the current one, by
 * using the existing extras to derive the extras of the new intent.
 * 
 * <pre>
 * IntentEncoder intentEncoder = new IntentEncoder(getIntent(), new FeedNavigationExtras());
 * if (intentDecoder.canGoLeft()) {
 *     startActivity(intentEncoder.buildGoLeftIntent(this, FeedActivity.class));
 * } else {
 *     // Can't go further to the left
 * }
 * </pre>
 */
public class IntentEncoder {
    private final Intent incomingIntent;
    private final NavigationExtras extrasKeys;

    public IntentEncoder(Intent incomingIntent, NavigationExtras extrasKeys) {
        this.incomingIntent = incomingIntent;
        this.extrasKeys = extrasKeys;
    }
    
    public Intent buildGoLeftIntent(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtras(incomingIntent.getExtras());
        int currentIndex = incomingIntent.getIntExtra(extrasKeys.getCurrentIndexKey(), 0);
        intent.putExtra(extrasKeys.getCurrentIndexKey(), currentIndex - 1);
        return intent;
    }

    public Intent buildGoRightIntent(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtras(incomingIntent.getExtras());
        int currentIndex = incomingIntent.getIntExtra(extrasKeys.getCurrentIndexKey(), 0);
        intent.putExtra(extrasKeys.getCurrentIndexKey(), currentIndex + 1);
        return intent;
    }
}
