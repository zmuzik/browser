/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License
 */

package com.sabaibrowser.preferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.webkit.WebView;

import com.sabaibrowser.BrowserSettings;
import com.sabaibrowser.PreferenceKeys;
import com.sabaibrowser.R;

import java.text.NumberFormat;

public class AccessibilityPreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    NumberFormat mFormat;
    // Used to pause/resume timers, which are required for WebViewPreview
    WebView mControlWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mControlWebView = new WebView(getActivity());
        addPreferencesFromResource(R.xml.accessibility_preferences);
        BrowserSettings settings = BrowserSettings.getInstance();
        mFormat = NumberFormat.getPercentInstance();

        Preference pref = findPreference(PreferenceKeys.PREF_MIN_FONT_SIZE);
        pref.setOnPreferenceChangeListener(this);
        updateMinFontSummary(pref, settings.getMinimumFontSize());
        pref = findPreference(PreferenceKeys.PREF_TEXT_ZOOM);
        pref.setOnPreferenceChangeListener(this);
        updateTextZoomSummary(pref, settings.getTextZoom());
    }

    @Override
    public void onResume() {
        super.onResume();
        mControlWebView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mControlWebView.pauseTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mControlWebView.destroy();
        mControlWebView = null;
    }

    void updateMinFontSummary(Preference pref, int minFontSize) {
        Context c = getActivity();
        pref.setSummary(c.getString(R.string.pref_min_font_size_value, minFontSize));
    }

    void updateTextZoomSummary(Preference pref, int textZoom) {
        pref.setSummary(mFormat.format(textZoom / 100.0));
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (getActivity() == null) {
            // We aren't attached, so don't accept preferences changes from the
            // invisible UI.
            return false;
        }

        if (PreferenceKeys.PREF_MIN_FONT_SIZE.equals(pref.getKey())) {
            updateMinFontSummary(pref, BrowserSettings.getAdjustedMinimumFontSize((Integer) objValue));
        }
        if (PreferenceKeys.PREF_TEXT_ZOOM.equals(pref.getKey())) {
            BrowserSettings settings = BrowserSettings.getInstance();
            updateTextZoomSummary(pref, settings.getAdjustedTextZoom((Integer) objValue));
        }
        return true;
    }

}
