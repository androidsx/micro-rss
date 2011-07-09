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

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Preferences.this, ChooseFeedsActivity.class));
                return true;
            }

        });
    }
}