package com.androidsx.microrss.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

public abstract class SwipeAwareListener implements View.OnTouchListener {
    private static final int SWIPE_MIN_DISTANCE = 60; //120;
    private static final int SWIPE_MAX_OFF_PATH = 100; //250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 190;
    
    private GestureDetector gestureDetector;
    
    public SwipeAwareListener() {
        super();
        this.gestureDetector = new GestureDetector(new SwipeDetector());
    }

    View.OnTouchListener gestureListener;

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public abstract void onRightToLeftSwipe();

    public abstract void onLeftToRightSwipe();

    public abstract void onTopToBottomSwipe();

    public abstract void onBottomToTopSwipe();

    class SwipeDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onRightToLeftSwipe();
                    
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onLeftToRightSwipe();
                    
                    return true;
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            // needed to detect onFling
            return true;
        }
    }
}
