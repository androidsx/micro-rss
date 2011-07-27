package com.androidsx.commons.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class IntentHelper {

    public static Intent createIntent(Context packageContext, Bundle incomingBundle,
            Class<?> destinationActivity) {
        Intent intent = new Intent(packageContext, destinationActivity);
        if (incomingBundle != null) {
            intent.putExtras(incomingBundle);
        }
        return intent;
    }
}
