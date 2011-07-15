package com.androidsx.microrss.configure;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

import com.androidsx.microrss.R;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference chooseFeedsPref = (Preference) findPreference("chooseFeeds");
        chooseFeedsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, ChooseFeedsActivity.class));
                return true;
            }
        });
        
        // FIXME: Missing: when we change the update interval, do some kind of refresh. For instance, 
        // if it is set to 24 hours and you change it down to 3 minutes, you have to wait another 17 hours
        // to get your 3 minutes! Maybe with WIMM this will radically change
        
    }
}