package com.androidsx.microrss.configure;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

import com.androidsx.commons.helper.IntentHelper;
import com.androidsx.microrss.R;
import com.androidsx.microrss.sync.SyncIntervalPrefs;
import com.androidsx.microrss.view.AnyRSSHelper;
import com.androidsx.microrss.view.FeedActivity;
import com.androidsx.microrss.view.SwipeAwareListener;

public class Preferences extends PreferenceActivity {
    public static final int ACTIVITY_RESULT_EXIT = 5;
    private SharedPreferences.OnSharedPreferenceChangeListener lastSyncListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init(this); // FIXME: belongs to the ugly copy-paste below
        addPreferencesFromResource(R.xml.preferences);
        
        getListView().setOnTouchListener(swipeListener);
        getListView().setVerticalScrollBarEnabled(false); 
        
        boolean isSyncedWithGoogleReader = PreferenceManager.getDefaultSharedPreferences(
                this).getBoolean(getResources().getString(R.string.pref_synced_with_google_reader),
                false);
        ((Preference) findPreference("chooseGoogleReaderFeeds")).setEnabled(isSyncedWithGoogleReader);

        ((Preference) findPreference("chooseSampleFeeds")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, ChooseSampleFeedsActivity.class));
                Preferences.this
                .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
        });
        
        ((Preference) findPreference("googleReaderAccount")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, GReaderPreferences.class));
                Preferences.this
                .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
        });

        ((Preference) findPreference("chooseGoogleReaderFeeds")).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, ChooseGoogleReaderFeedsActivity.class));
                Preferences.this
                .overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
        });
        
//        ((Preference) findPreference("syncStories")).setOnPreferenceClickListener(
//                new OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Log.i(TAG, "Force the syncronization");
//                
//                SyncIntervalPrefs syncIntervalPrefs = new SyncIntervalPrefs(Preferences.this);
//                if ( syncIntervalPrefs.isSyncing() ) {
//                    Log.i(TAG, "We are already syncing");
//                    Toast.makeText(Preferences.this, "We are already syncing", Toast.LENGTH_SHORT).show();
//                } else {
//                    long syncTime = syncIntervalPrefs.getLastSuccessfulSync();
//                    if ( syncTime != 0 ) {
//                        Time t = new Time();
//                        t.set(syncTime);
//                        Log.i(TAG, "Last sync success: " + t.format("%H:%M:%S") + " " +  t.format("%m/%d/%Y"));
//                    } else {
//                        Log.i(TAG, "We have never succeed to sync");
//                    }
//                    
//                    Toast.makeText(Preferences.this, "Force the sync, it may take a while", Toast.LENGTH_SHORT).show();
//                }
//                
//                syncIntervalPrefs.willForceSync(true);
//                WIMMCompatibleHelper.requestSync(Preferences.this);
//                
//                return true;
//            }
//        });
        
        
        ((Preference) findPreference("syncStoriesMessage")).setTitle(getLastSyncMessage());
        lastSyncListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
                if (key.equals(SyncIntervalPrefs.LAST_SUCCESSFUL_SYNC)
                        || key.equals(SyncIntervalPrefs.SYNC_STATUS)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((Preference) findPreference("syncStoriesMessage"))
                            .setTitle(getLastSyncMessage());
                        }
                    });
                }
            }
        };
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(lastSyncListener);

        // FIXME: Missing: when we change the update interval, do some kind of refresh. For instance, 
        // if it is set to 24 hours and you change it down to 3 minutes, you have to wait another 17 hours
        // to get your 3 minutes! Maybe with WIMM this will radically change
    }
    
    public void onGoBackClick(View target) {
        Intent intent = IntentHelper.createIntent(Preferences.this, null, FeedActivity.class);
        startActivity(intent);
    }
    
    private View.OnTouchListener swipeListener = new SwipeAwareListener() {

        @Override
        public void onTopToBottomSwipe() {
            /*if (getCurrentFocus().getScrollY() == 0) {
                Log.i("Preferences", "finish");
                finish();
            } else {
                Log.e("Preferences", "don't finish");
            }*/
        }

        @Override
        public void onRightToLeftSwipe() {
            Intent intent = IntentHelper.createIntent(Preferences.this, null,
                    FeedActivity.class);
            
            Preferences.this
                    .overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(intent); // TODO: hmm, should we not just exit?
            
            // Connected to #306 too
            //setActivityResult(Activity.RESULT_OK);
            //finish();
        }

        @Override
        public void onLeftToRightSwipe() {
        }

        @Override
        public void onBottomToTopSwipe() {
        }
    };
    
	private String getLastSyncMessage() {
		String msg = "";
		SyncIntervalPrefs syncIntervalPrefs = new SyncIntervalPrefs(
				Preferences.this);
		if (syncIntervalPrefs.isSyncing()) {
			msg = getString(R.string.synchronizing);
		} else {
			long syncTime = syncIntervalPrefs.getLastSyncAttempt();
			if (syncTime != 0) {
				msg = getString(R.string.last_sync) + " "
						+ AnyRSSHelper.toRelativeDateString(new Date(syncTime));
			} else {
				msg = getString(R.string.never_synced);
			}
		}
		return msg;
	}

	/*
	// Pablo: huge copy-paste from WIMM's AppFrame, and some from WIMM's LauncherActivity
    // (requires implements OnGestureListener)
	 
	public Preferences() {
        mOffset = 0;
        mAnimateOffScreen = false;
        mIsWimmApp = false;
    }
	
    public Preferences(Context context) {
        this();
    }

    public Preferences(Context context, AttributeSet attrs) {
        this();
    }

    public Preferences(Context context, AttributeSet attrs, int defStyle) {
        this();
    }

    private void init(Context context) {
        mIsWimmApp = true; //context instanceof LauncherActivity; // pablo: hmm
        mGestureDetector = new GestureDetector(context, this);
        mPixelsToCommit = TypedValue.applyDimension(1, DP_TO_COMMIT, context.getResources()
                .getDisplayMetrics());
        mOffAxisPixelTolerance = mPixelsToCommit * OFF_AXIS_TOLERANCE_FACTOR;
        mAnimation = new EmptyAnimation();
        mAnimation.setDuration(500L);
        mAnimation.setAnimationListener(mAnimationListener);
        mTopShadow = context.getResources().getDrawable(17302336); // pablo: ouch
    }

    public boolean dispatchTouchEvent(MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        if (!mIsInterceptingEvents)
            super.dispatchTouchEvent(e);
        else if (e.getAction() == 1)
            onUpEvent(e);
        else if (e.getAction() == 3)
            animate(false);
        return true;
    }

    // FIXME (pablo): this is probably the key. but it belongs to the framelayout...
//    protected void dispatchDraw(Canvas c) {
//        if (mAnimation.isAnimating()) {
//            mOffset = (int) (mAnimation.getProgress() * (float) getHeight());
//            if (!mAnimateOffScreen)  
//                mOffset = getHeight() - mOffset;
//        }
//        c.save();
//        c.translate(0.0F, mOffset);
//        super.dispatchDraw(c);
//        c.restore();
//        if (mOffset != 0) {
//            mTopShadow.setBounds(0, mOffset - mTopShadow.getIntrinsicHeight(), getWidth(), mOffset);
//            mTopShadow.draw(c);
//        }
//    }

    private void shiftOffset(float delta) {
        mOffset = Math.max(0, Math.min(getHeight(), (int) ((float) mOffset + delta)));
        invalidate();
    }

    private void checkForIntercept(MotionEvent e1, MotionEvent e2) {
        if (!mIsWimmApp || !mCanInterceptEvent)
            return;
        float xDiff = Math.abs(e2.getX() - e1.getX());
        float yDiff = Math.abs(e2.getY() - e1.getY());
        yDiff = Math.abs(yDiff);
        boolean vertical = yDiff > xDiff;
        boolean down = vertical && e2.getY() > e1.getY();
        float big = vertical ? yDiff : xDiff;
        float small = vertical ? xDiff : yDiff;
        if (big < mPixelsToCommit)
            return;
        if (big * mOffAxisPixelTolerance < small) {
            mCanInterceptEvent = false;
            return;
        }
        mCanInterceptEvent = false;
        //if (down && ((LauncherActivity) getContext()).dragCanExit()) { // This is the original event
        if (down) {
            if (dragCanExit()) {
                Log.e("Preferences", "EXIT!");
                mIsInterceptingEvents = true;
                MotionEvent cancel = MotionEvent.obtain(e2);
                cancel.setAction(3);
                super.dispatchTouchEvent(cancel);
                
                // i guess we should be launching the animation and exiting here. hopefully in a decoupled way
                
            } else {
                Log.e("Preferences", "down, but just a scroll");
            }
        }
    }

    public boolean onDown(MotionEvent e1) {
        if (e1.getPointerCount() == 1) {
            mIsInterceptingEvents = false;
            mCanInterceptEvent = true;
        }
        return mIsInterceptingEvents;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        checkForIntercept(e1, e2);
        if (mIsInterceptingEvents)
            shiftOffset(-distanceY);
        return mIsInterceptingEvents;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        checkForIntercept(e1, e2);
        if (mIsInterceptingEvents) {
            animate(velocityY > 0.0F);
            mIsInterceptingEvents = false;
        }
        return mIsInterceptingEvents;
    }

    public void onLongPress(MotionEvent motionevent) {
    }

    public void onShowPress(MotionEvent motionevent) {
    }

    public boolean onSingleTapUp(MotionEvent e1) {
        return mIsInterceptingEvents;
    }

    private void onUpEvent(MotionEvent e) {
        if (mIsInterceptingEvents && e.getPointerCount() == 1)
            animate(mOffset > getHeight() / 2);
    }

    public void animate(boolean offScreen) {
        mAnimateOffScreen = offScreen;
        float progress = (float) mOffset / (float) getHeight();
        mAnimation.setStartProgress(offScreen ? progress : 1.0F - progress);
        startAnimation(mAnimation);
    }

    private static int DP_TO_COMMIT = 20;
    private static float OFF_AXIS_TOLERANCE_FACTOR = 0.6F;
    private static final int ANIMATION_DURATION = 500;
    private int mOffset;
    private GestureDetector mGestureDetector;
    private boolean mCanInterceptEvent;
    private boolean mIsInterceptingEvents;
    private float mPixelsToCommit;
    private float mOffAxisPixelTolerance;
    private EmptyAnimation mAnimation;
    private boolean mAnimateOffScreen;
    private Drawable mTopShadow;
    private boolean mIsWimmApp;
    private android.view.animation.Animation.AnimationListener mAnimationListener = new android.view.animation.Animation.AnimationListener() {

        public void onAnimationEnd(Animation animation) {
            if (mAnimateOffScreen) {
                //Context context = getContext();
                //if (context instanceof LauncherActivity) {
                //    LauncherActivity activity = (LauncherActivity) context;
                //    activity.doFinish();
                //}
                
                // Pablo: it used to be a doFinish on the activity
                setActivityResult(ACTIVITY_RESULT_EXIT);
                finish();
                
            } else if (mOffset != 0) {
                mOffset = 0;
                invalidate();
            }
        }

        public void onAnimationRepeat(Animation animation1) {
        }

        public void onAnimationStart(Animation animation1) {
        }
    };
    
    // Pablo
    private int getHeight() {
        return getListView().getHeight();
    }

    // Pablo
    private void invalidate() {
        getListView().invalidate();
    }

    // Pablo
    private void startAnimation(EmptyAnimation mAnimation2) {
        getListView().startAnimation(mAnimation2);
    }
    
    // Pablo
    private boolean dragCanExit() {;
        return getListView().getFirstVisiblePosition() == 0 ? true : false; // Is this solid enough? It's generic for any ListActivity :)
    }
    
    private void setActivityResult(int resultCode) {
        Intent resultIntent = new Intent();
        if (getParent() == null) {
            Log.e("Preferences", "finish, my parent is null");
            setResult(resultCode, resultIntent);
        } else {
            Log.e("Preferences", "finish, my parent is not null");
            getParent().setResult(resultCode, resultIntent);
        }
    }
    */
}
