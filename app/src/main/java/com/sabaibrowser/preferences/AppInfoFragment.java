package com.sabaibrowser.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.sabaibrowser.BuildConfig;
import com.sabaibrowser.PreferenceKeys;
import com.sabaibrowser.R;

public class AppInfoFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.info_preferences);
        findPreference(PreferenceKeys.PREF_VERSION).setSummary(BuildConfig.VERSION_NAME);
        findPreference(PreferenceKeys.PREF_BUILD).setSummary("" + BuildConfig.VERSION_CODE);
    }
}
