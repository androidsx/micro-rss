package com.androidsx.microrss.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.wimm.framework.view.AdapterViewTray;
import com.wimm.framework.view.MotionInterpreter.ScrollAxis;

/**
 * Custom view to inject some listeners for detecting swipe up/down/left/right
 */
public class CustomAdapterViewTray extends AdapterViewTray {

    private OnDragEndListener dragEndListener = null;

    public CustomAdapterViewTray(Context context) {
        super(context);
    }

    public CustomAdapterViewTray(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomAdapterViewTray(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDragEnd(MotionEvent arg0, ScrollAxis arg1, float arg3) {
        super.onDragEnd(arg0, arg1, arg3);
        if (dragEndListener != null) {
            dragEndListener.onDragEnd(arg0, arg1, arg3);
        }
    }

    public void setOnDragEndListener(OnDragEndListener dragEndListener) {
        this.dragEndListener = dragEndListener;
    }

    public static interface OnDragEndListener {
        public void onDragEnd(MotionEvent arg0, ScrollAxis arg1, float arg3);
    }

}