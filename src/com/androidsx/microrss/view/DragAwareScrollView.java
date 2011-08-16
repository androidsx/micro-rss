package com.androidsx.microrss.view;

import android.content.Context;
import android.util.AttributeSet;

import com.wimm.framework.view.ScrollView;

/**
 * Scroll view that is aware of the WIMM drag-down mechanism: see the operation {@link #dragCanExit}
 * .
 */
public class DragAwareScrollView extends ScrollView implements Draggable {
    private static boolean dragCanExit = false;

    public DragAwareScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DragAwareScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragAwareScrollView(Context context) {
        super(context);
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        dragCanExit = t <= 0;
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean dragCanExit() {
        return dragCanExit;
    }
}
