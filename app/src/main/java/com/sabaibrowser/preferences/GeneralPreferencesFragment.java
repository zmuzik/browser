/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebStorage;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sabaibrowser.BrowserActivity;
import com.sabaibrowser.BrowserSettings;
import com.sabaibrowser.BrowserSettingsActivity;
import com.sabaibrowser.PreferenceKeys;
import com.sabaibrowser.R;
import com.sabaibrowser.UrlUtils;

import java.util.Map;
import java.util.Set;

public class GeneralPreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    static final String TAG = "PersonalPreferencesFragment";

    public static final String BLANK_URL = "about:blank";

    static final String CURRENT = "current";
    static final String BLANK = "blank";
    static final String DEFAULT = "default";
    static final String MOST_VISITED = "most_visited";
    static final String OTHER = "other";

    static final String PREF_HOMEPAGE_PICKER = "homepage_picker";

    String[] mChoices, mValues;
    String mCurrentPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getActivity().getResources();
        mChoices = res.getStringArray(R.array.pref_homepage_choices);
        mValues = res.getStringArray(R.array.pref_homepage_values);
        mCurrentPage = getActivity().getIntent()
                .getStringExtra(BrowserSettingsActivity.CURRENT_PAGE);

        // Load the XML preferences file
        addPreferencesFromResource(R.xml.general_preferences);

        ListPreference listPref = (ListPreference) findPreference(PREF_HOMEPAGE_PICKER);
        listPref.setSummary(getHomepageSummary());
        listPref.setPersistent(false);
        listPref.setValue(getHomepageValue());
        listPref.setOnPreferenceChangeListener(this);

        // imported from advanced prefs
        PreferenceScreen websiteSettings = (PreferenceScreen) findPreference(
                PreferenceKeys.PREF_WEBSITE_SETTINGS);
        websiteSettings.setFragment(WebsiteSettingsFragment.class.getName());

        Preference pref = findPreference(PreferenceKeys.PREF_DEFAULT_ZOOM);
        pref.setOnPreferenceChangeListener(this);
        pref.setSummary(getVisualDefaultZoomName(
                getPreferenceScreen().getSharedPreferences()
                        .getString(PreferenceKeys.PREF_DEFAULT_ZOOM, null)));

        pref = findPreference(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING);
        pref.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) pref);

        pref = findPreference(PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES);
        pref.setOnPreferenceChangeListener(this);

        pref = findPreference(PreferenceKeys.PREF_SEARCH_ENGINE);
        pref.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) pref);

        pref = findPreference(PreferenceKeys.PREF_PLUGIN_STATE);
        pref.setOnPreferenceChangeListener(this);
        updateListPreferenceSummary((ListPreference) pref);
    }

    @Override
    public void onResume() {
        super.onResume();
        final PreferenceScreen websiteSettings = (PreferenceScreen) findPreference(
                PreferenceKeys.PREF_WEBSITE_SETTINGS);
        websiteSettings.setEnabled(false);
        WebStorage.getInstance().getOrigins(new ValueCallback<Map>() {
            @Override
            public void onReceiveValue(Map webStorageOrigins) {
                if ((webStorageOrigins != null) && !webStorageOrigins.isEmpty()) {
                    websiteSettings.setEnabled(true);
                }
            }
        });
        GeolocationPermissions.getInstance().getOrigins(new ValueCallback<Set<String>>() {
            @Override
            public void onReceiveValue(Set<String> geolocationOrigins) {
                if ((geolocationOrigins != null) && !geolocationOrigins.isEmpty()) {
                    websiteSettings.setEnabled(true);
                }
            }
        });
    }

    void updateListPreferenceSummary(ListPreference e) {
        e.setSummary(e.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (getActivity() == null) {
            // We aren't attached, so don't accept preferences changes from the
            // invisible UI.
            Log.w("PreferencesFragment", "onPreferenceChange called from detached fragment!");
            return false;
        }

        if (pref.getKey().equals(PREF_HOMEPAGE_PICKER)) {
            BrowserSettings settings = BrowserSettings.getInstance();
            if (CURRENT.equals(objValue)) {
                settings.setHomePage(mCurrentPage);
            }
            if (BLANK.equals(objValue)) {
                settings.setHomePage(BLANK_URL);
            }
            if (DEFAULT.equals(objValue)) {
                settings.setHomePage(BrowserSettings.getFactoryResetHomeUrl(
                        getActivity()));
            }
            if (OTHER.equals(objValue)) {
                promptForHomepage((ListPreference) pref);
                return false;
            }
            pref.setSummary(getHomepageSummary());
            ((ListPreference) pref).setValue(getHomepageValue());
            return false;
        } else if (pref.getKey().equals(PreferenceKeys.PREF_DEFAULT_ZOOM)) {
            pref.setSummary(getVisualDefaultZoomName((String) objValue));
            return true;
        } else if (pref.getKey().equals(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING)) {
            pref.setSummary((String) objValue);
            return true;
        } else if (pref.getKey().equals(PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES)) {
            Boolean value = (Boolean) objValue;
            if (value.booleanValue() == true) {
                startActivity(new Intent(BrowserActivity.ACTION_RESTART, null,
                        getActivity(), BrowserActivity.class));
                return true;
            }
        } else if (pref.getKey().equals(PreferenceKeys.PREF_PLUGIN_STATE)
                || pref.getKey().equals(PreferenceKeys.PREF_SEARCH_ENGINE)) {
            ListPreference lp = (ListPreference) pref;
            lp.setValue((String) objValue);
            updateListPreferenceSummary(lp);
            return false;
        }

        return true;
    }

    void promptForHomepage(final ListPreference pref) {
        final BrowserSettings settings = BrowserSettings.getInstance();
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_URI);
        editText.setText(settings.getHomePage());
        editText.setSelectAllOnFocus(true);
        editText.setSingleLine(true);
        editText.setImeActionLabel(null, EditorInfo.IME_ACTION_DONE);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String homepage = editText.getText().toString().trim();
                        homepage = UrlUtils.smartUrlFilter(homepage);
                        settings.setHomePage(homepage);
                        pref.setValue(getHomepageValue());
                        pref.setSummary(getHomepageSummary());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setTitle(R.string.pref_set_homepage_to)
                .create();
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    String getHomepageValue() {
        BrowserSettings settings = BrowserSettings.getInstance();
        String homepage = settings.getHomePage();
        if (TextUtils.isEmpty(homepage) || BLANK_URL.endsWith(homepage)) {
            return BLANK;
        }
        String defaultHomepage = BrowserSettings.getFactoryResetHomeUrl(
                getActivity());
        if (TextUtils.equals(defaultHomepage, homepage)) {
            return DEFAULT;
        }
        if (TextUtils.equals(mCurrentPage, homepage)) {
            return CURRENT;
        }
        return OTHER;
    }

    String getHomepageSummary() {
        BrowserSettings settings = BrowserSettings.getInstance();
        if (settings.useMostVisitedHomepage()) {
            return getHomepageLabel(MOST_VISITED);
        }
        String homepage = settings.getHomePage();
        if (TextUtils.isEmpty(homepage) || BLANK_URL.equals(homepage)) {
            return getHomepageLabel(BLANK);
        }
        return homepage;
    }

    String getHomepageLabel(String value) {
        for (int i = 0; i < mValues.length; i++) {
            if (value.equals(mValues[i])) {
                return mChoices[i];
            }
        }
        return null;
    }

    private CharSequence getVisualDefaultZoomName(String enumName) {
        Resources res = getActivity().getResources();
        CharSequence[] visualNames = res.getTextArray(R.array.pref_default_zoom_choices);
        CharSequence[] enumNames = res.getTextArray(R.array.pref_default_zoom_values);

        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }

        int length = enumNames.length;
        for (int i = 0; i < length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }

        return "";
    }
}
