package com.androidsx.microrss.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;

import com.androidsx.microrss.R;

public class StoryView extends DragAwareScrollView {
    private boolean pendingAnimation = false;

    public StoryView(Context paramContext) {
        super(paramContext);
    }

    public StoryView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public StoryView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    private void animateIn() {
        this.pendingAnimation = false;
        AlphaAnimation localAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        LinearInterpolator localLinearInterpolator = new LinearInterpolator();
        localAlphaAnimation.setInterpolator(localLinearInterpolator);
        localAlphaAnimation.setDuration(300L);
        findViewById(R.id.story_image).setAnimation(localAlphaAnimation);
        localAlphaAnimation.startNow();
    }

    @Override
    protected void onAttachedToWindow() {
        this.pendingAnimation = true;
    }

    @Override
    public void onDraw(Canvas paramCanvas) {
        if (this.pendingAnimation)
            animateIn();
        super.onDraw(paramCanvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean onInterceptTouchEvent = super.onInterceptTouchEvent(ev);
        //Log.e("SCROLL", "on intercept " + ev + ", " + ev.getRawX() + ", " + ev.getRawY() + ", " + ev.getX() + ", " + ev.getY());
        return onInterceptTouchEvent;
    }
}
