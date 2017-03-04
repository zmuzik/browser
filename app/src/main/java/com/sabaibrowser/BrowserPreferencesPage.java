/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.sabaibrowser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.sabaibrowser.preferences.AccessibilityPreferencesFragment;
import com.sabaibrowser.preferences.AdvancedPreferencesFragment;
import com.sabaibrowser.preferences.AppInfoFragment;
import com.sabaibrowser.preferences.GeneralPreferencesFragment;
import com.sabaibrowser.preferences.PrivacySecurityPreferencesFragment;

import java.util.ArrayList;
import java.util.List;

public class BrowserPreferencesPage extends Activity {

    public static final String CURRENT_PAGE = "currentPage";
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_screen);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        BottomNavigationView bottomNavigationView =
                (BottomNavigationView) findViewById(R.id.bottom_navigation);
        setupViewPager(viewPager);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
//                            case R.id.action_favorites:
//
//                            case R.id.action_schedules:
//
//                            case R.id.action_music:

                        }
                        return true;
                    }
                });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        adapter.addFragment(new GeneralPreferencesFragment(), "general");
        adapter.addFragment(new PrivacySecurityPreferencesFragment(), "security");
        adapter.addFragment(new AdvancedPreferencesFragment(), "advanced");
        adapter.addFragment(new AccessibilityPreferencesFragment(), "accessibility");
        adapter.addFragment(new AppInfoFragment(), "info");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
