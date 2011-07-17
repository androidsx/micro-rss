package com.androidsx.microrss.view.extra;

import android.content.Context;
import android.content.Intent;

import com.androidsx.microrss.view.NavigationExtras;

/**
 * Builds an intent to navigate to the activity to the left/right/up/down from the current one, by
 * using the existing extras to derive the extras of the new intent.
 * 
 * <pre>
 * IntentEncoder intentEncoder = IntentEncoder(this, getIntent());
 * if (intentDecoder.canGoLeft()) {
 *     startActivity(intentEncoder.buildGoLeftIntent(FeedActivity.class, new FeedNavigationExtras()));
 * } else {
 *     // Can't go further to the left
 * }
 * </pre>
 */
public class IntentEncoder {
    private final Intent incomingIntent;
    private final Context packageContext;

    public IntentEncoder(Context packageContext, Intent incomingIntent) {
        this.packageContext = packageContext;
        this.incomingIntent = incomingIntent;
    }
    
    public Intent buildGoLeftIntent(Class<?> destinationActivity, NavigationExtras incomingExtras) {
        Intent intent = new Intent(packageContext, destinationActivity);
        intent.putExtras(incomingIntent.getExtras());
        int currentIndex = incomingIntent.getIntExtra(incomingExtras.getCurrentIndexKey(), 0);
        intent.putExtra(incomingExtras.getCurrentIndexKey(), currentIndex - 1);
        return intent;
    }

    public Intent buildGoRightIntent(Class<?> destinationActivity, NavigationExtras incomingExtras) {
        Intent intent = new Intent(packageContext, destinationActivity);
        intent.putExtras(incomingIntent.getExtras());
        int currentIndex = incomingIntent.getIntExtra(incomingExtras.getCurrentIndexKey(), 0);
        intent.putExtra(incomingExtras.getCurrentIndexKey(), currentIndex + 1);
        return intent;
    }
    
    public Intent buildGoUpIntent(Class<?> destinationActivity) {
        Intent intent = new Intent(packageContext, destinationActivity);
        intent.putExtras(incomingIntent.getExtras());
        return intent;
    }
    
    public Intent buildGoDownIntent(Class<?> destinationActivity, NavigationExtras lowerLevelExtras, int[] nextLevelIds) {
        Intent intent = new Intent(packageContext, destinationActivity);
        if (incomingIntent.getExtras() != null) {
            intent.putExtras(incomingIntent.getExtras());
        }
        intent.putExtra(lowerLevelExtras.getAllIdsKey(), nextLevelIds);
        intent.putExtra(lowerLevelExtras.getCurrentIndexKey(), 0);
        return intent;
    }
}
