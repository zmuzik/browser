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
 * limitations under the License.
 */

package com.sabaibrowser;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;

import com.sabaibrowser.TabControl.OnThumbnailUpdatedListener;
import com.sabaibrowser.view.BubbleMenu;
import com.sabaibrowser.view.Carousel;

import java.util.HashMap;

public class NavScreen extends RelativeLayout
        implements OnClickListener, OnMenuItemClickListener, OnThumbnailUpdatedListener {

    UiController mUiController;
    PhoneUi mUi;
    Activity mActivity;

    BubbleMenu mBubbleMenu;
    Carousel mCarousel;

    int mOrientation;
    HashMap<Tab, View> mTabViews;

    public NavScreen(Activity activity, UiController ctl, PhoneUi ui) {
        super(activity);
        mActivity = activity;
        mUiController = ctl;
        mUi = ui;
        mOrientation = activity.getResources().getConfiguration().orientation;
        init();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }

    @Override
    protected void onConfigurationChanged(Configuration newconfig) {
        if (newconfig.orientation != mOrientation) {
            removeAllViews();
            mOrientation = newconfig.orientation;
            init();
        }
    }

    public void refreshAdapter() {
        TabControl tc = mUiController.getTabControl();
        mCarousel.removeAllViews();
        for (int i = 0; i < tc.getTabCount(); i++) {
            final int position = i;
            TabCard card = new TabCard(getContext());
            final Tab tab = tc.getTab(i);
            card.setTab(tab);
            card.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchToTab(tab);
                    close(position, false);
                    //mUi.editUrl(false, true);
                }
            });

            mCarousel.addView(card);
        }
        mCarousel.setSelectedPosition(tc.getCurrentPosition());
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.nav_screen, this);
        setContentDescription(getContext().getResources().getString(
                R.string.accessibility_transition_navscreen));
        mCarousel = (Carousel) findViewById(R.id.carousel);
        TabControl tc = mUiController.getTabControl();
        mTabViews = new HashMap<Tab, View>(tc.getTabCount());

        mBubbleMenu = (BubbleMenu) findViewById(R.id.bubble_menu_nav);
        mBubbleMenu.addMenuItem(R.drawable.ic_new_window, new OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                openNewTab(false);
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_incognito, new OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                openNewTab(true);
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_home, new OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                gotoHomePage();
            }
        });
    }

    @Override
    public void onClick(View v) {
    }

    private void onCloseTab(Tab tab) {
        if (tab != null) {
            if (tab == mUiController.getCurrentTab()) {
                mUiController.closeCurrentTab();
            } else {
                mUiController.closeTab(tab);
            }
            mTabViews.remove(tab);
        }
    }

    private void openNewTab(boolean incognito) {
        // need to call openTab explicitely with setactive false
        final Tab tab = incognito ?
                mUiController.openIncognitoTab() :
                mUiController.openTab(BrowserSettings.getInstance().getHomePage(),
                        false, false, false);
        if (tab != null) {
            mUiController.setBlockEvents(true);
            final int tix = mUi.mTabControl.getTabPosition(tab);
            mUiController.setBlockEvents(false);
        }
    }

    private void gotoHomePage() {
        final Tab tab = findCenteredTab();
        if (tab != null) {
            mUiController.setBlockEvents(true);
            final int tix = mUi.mTabControl.getTabPosition(tab);
            mUiController.setBlockEvents(false);
            mUiController.loadUrl(tab, BrowserSettings.getInstance().getHomePage());
        }
    }

    private Tab findCenteredTab() {
        return mUiController.getTabs().get(0);
    }

    private void switchToTab(Tab tab) {
        if (tab != mUi.getActiveTab()) {
            mUiController.setActiveTab(tab);
        }
    }

    protected void close(int position) {
        close(position, true);
    }

    protected void close(int position, boolean animate) {
        mUi.hideNavScreen(position, animate);
    }

    @Override
    public void onThumbnailUpdated(Tab t) {
        View v = mTabViews.get(t);
        if (v != null) {
            v.invalidate();
        }
    }
}
