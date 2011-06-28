/*
 * Copyright (C) 2009 Jeff Sharkey, http://jsharkey.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidsx.microrss;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.androidsx.anyrss.FlurryConstants;
import com.androidsx.microrss.R;
import com.flurry.android.FlurryAgent;

/**
 * Information to tell users how to insert widgets, and credit to data sources.
 */
public class InfoActivity extends Activity {
    /** Time in millis that the activity waits for enable the buttons */
    private static final long TIME_ENABLE_BUTTONS_IN_MS = 5 * 1000;

    private Button buttonOk;
    private Button buttonOurApps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        FlurryAgent.setContinueSessionMillis(FlurryConstants.SESSION_MILLIS);
        setContentView(R.layout.info);

        buttonOk = (Button) findViewById(R.id.info_done);
        buttonOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        buttonOurApps = (Button) findViewById(R.id.info_our_apps);
        buttonOurApps.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String url = "market://search?q=pub:Androidsx";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

                finish();
            }
        });

        buttonOurApps.setEnabled(true);
        new CountDownTimer(TIME_ENABLE_BUTTONS_IN_MS, TIME_ENABLE_BUTTONS_IN_MS) {
            @Override
            public void onFinish() {
                buttonOk.setEnabled(true);
            }

            @Override
            public void onTick(@SuppressWarnings("unused") long millisUntilFinished) {
            }
        }.start();
    }
}
