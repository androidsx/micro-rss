package com.androidsx.commons.helper;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

/**
 * Helper and utility methods for the UI components.
 */
public class ComponentHelper {
    
    public static void increaseTouchArea(final View view, View parent, final int pixels) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                View delegate = view;
                delegate.getHitRect(delegateArea);
                delegateArea.left -= pixels;
                delegateArea.right += pixels;
                delegateArea.top -= pixels;
                delegateArea.bottom += pixels;
                TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);
                if (View.class.isInstance(delegate.getParent())) {
                    ((View) delegate.getParent()).setTouchDelegate(expandedArea);
                }
            }
        });

    }
}
