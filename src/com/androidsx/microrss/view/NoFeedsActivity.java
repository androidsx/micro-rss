package com.androidsx.microrss.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.androidsx.microrss.R;

public class NoFeedsActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_wrapper);
        
        ((TextView) findViewById(R.id.feed_title)).setText(getString(R.string.no_feeds_configured));
    }

}
