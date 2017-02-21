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
 * limitations under the License.
 */

package com.sabaibrowser;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.sabaibrowser.UrlInputView.StateListener;

/**
 * Ui for regular phone screen sizes
 */
public class PhoneUi extends BaseUi {

    private static final String LOGTAG = "PhoneUi";
    private static final int MSG_INIT_NAVSCREEN = 100;

    private NavScreen mNavScreen;
    private NavigationBarPhone mNavigationBar;
    private int mActionBarHeight;

    boolean mShowNav = false;

    /**
     * @param browser
     * @param controller
     */
    public PhoneUi(Activity browser, UiController controller) {
        super(browser, controller);
        mNavigationBar = (NavigationBarPhone) mTitleBar.getNavigationBar();
        mNavigationBar.setFab(mFab);
        initBubbleMenu();
        TypedValue heightValue = new TypedValue();
        browser.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, heightValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(heightValue.data,
                browser.getResources().getDisplayMetrics());
    }

    private void initBubbleMenu() {
        mBubbleMenu.addMenuItem(R.drawable.ic_windows, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                showNavScreen();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_new_window, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                mUiController.openTabToHomePage();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_incognito, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                mUiController.openIncognitoTab();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_bookmarks, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_settings, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                mUiController.openPreferences();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_home, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_search, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
            }
        });
    }

    @Override
    public void onDestroy() {
        hideTitleBar();
    }

    @Override
    public void editUrl(boolean clearInput, boolean forceIME) {
        //Do nothing while at Nav show screen.
        if (mShowNav) return;
        super.editUrl(clearInput, forceIME);
    }

    @Override
    public boolean onBackKey() {
        if (showingNavScreen()) {
            mNavScreen.close(mUiController.getTabControl().getCurrentPosition());
            return true;
        }
        return super.onBackKey();
    }

    private boolean showingNavScreen() {
        return mNavScreen != null && mNavScreen.getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean dispatchKey(int code, KeyEvent event) {
        return false;
    }

    @Override
    public void onProgressChanged(Tab tab) {
        super.onProgressChanged(tab);
        if (mNavScreen == null && getTitleBar().getHeight() > 0) {
            mHandler.sendEmptyMessage(MSG_INIT_NAVSCREEN);
        }
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == MSG_INIT_NAVSCREEN) {
            if (mNavScreen == null) {
                mNavScreen = new NavScreen(mActivity, mUiController, this);
                mCustomViewContainer.addView(mNavScreen, COVER_SCREEN_PARAMS);
                mNavScreen.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setActiveTab(final Tab tab) {
        mTitleBar.cancelTitleBarAnimation(true);
        mTitleBar.setSkipTitleBarAnimations(true);
        super.setActiveTab(tab);

        //if at Nav screen show, detach tab like what showNavScreen() do.
        if (mShowNav) {
            detachTab(mActiveTab);
        }

        BrowserWebView view = (BrowserWebView) tab.getWebView();
        // TabControl.setCurrentTab has been called before this,
        // so the tab is guaranteed to have a webview
        if (view == null) {
            Log.e(LOGTAG, "active tab with no webview detected");
            return;
        }
        // Request focus on the top window.
        view.setTitleBar(mTitleBar);
        // update nav bar state
        mNavigationBar.onStateChanged(StateListener.STATE_NORMAL);
        mTitleBar.setSkipTitleBarAnimations(false);
    }

    // menu handling callbacks

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuState(mActiveTab, menu);
        return true;
    }

    @Override
    public void updateMenuState(Tab tab, Menu menu) {
        MenuItem bm = menu.findItem(R.id.bookmarks_menu_id);
        if (bm != null) {
            bm.setVisible(!showingNavScreen());
        }
        MenuItem abm = menu.findItem(R.id.add_bookmark_menu_id);
        if (abm != null) {
            abm.setVisible((tab != null) && !tab.isSnapshot() && !showingNavScreen());
        }
        MenuItem info = menu.findItem(R.id.page_info_menu_id);
        if (info != null) {
            info.setVisible(false);
        }
        MenuItem newTab = menu.findItem(R.id.new_tab_menu_id);
        if (newTab != null) {
            newTab.setVisible(false);
        }
        MenuItem newIncognitoTab = menu.findItem(R.id.new_incognito_tab_menu_id);
        if (newIncognitoTab != null) {
            newIncognitoTab.setVisible(false);
        }
        MenuItem closeOthers = menu.findItem(R.id.close_other_tabs_id);
        if (closeOthers != null) {
            boolean isLastTab = true;
            if (tab != null) {
                isLastTab = (mTabControl.getTabCount() <= 1);
            }
            closeOthers.setEnabled(!isLastTab);
        }
        if (showingNavScreen()) {
            menu.setGroupVisible(R.id.LIVE_MENU, false);
            menu.setGroupVisible(R.id.SNAPSHOT_MENU, false);
            menu.setGroupVisible(R.id.NAV_MENU, false);
            menu.setGroupVisible(R.id.COMBO_MENU, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (showingNavScreen()
                && (item.getItemId() != R.id.history_menu_id)
                && (item.getItemId() != R.id.snapshots_menu_id)) {
            hideNavScreen(mUiController.getTabControl().getCurrentPosition(), false);
        }
        return false;
    }

    @Override
    public void onContextMenuCreated(Menu menu) {
        hideTitleBar();
    }

    @Override
    public void onContextMenuClosed(Menu menu, boolean inLoad) {
        if (inLoad) {
            showTitleBar();
        }
    }

    // action mode callbacks

    @Override
    public void onActionModeStarted(ActionMode mode) {
        if (!isEditingUrl()) {
            hideTitleBar();
        } else {
            mTitleBar.animate().translationY(mActionBarHeight);
        }
    }

    @Override
    public void onActionModeFinished(boolean inLoad) {
        mTitleBar.animate().translationY(0);
        if (inLoad) {
            showTitleBar();
        }
    }

    @Override
    public boolean isWebShowing() {
        return super.isWebShowing() && !showingNavScreen();
    }

    @Override
    public void showWeb(boolean animate) {
        super.showWeb(animate);
        hideNavScreen(mUiController.getTabControl().getCurrentPosition(), animate);
    }

    void showNavScreen() {
        mShowNav = true;
        mUiController.setBlockEvents(true);
        if (mNavScreen == null) {
            mNavScreen = new NavScreen(mActivity, mUiController, this);
            mCustomViewContainer.addView(mNavScreen, COVER_SCREEN_PARAMS);
        } else {
            mNavScreen.setVisibility(View.VISIBLE);
            mNavScreen.setAlpha(1f);
            mNavScreen.refreshAdapter();
        }
        mActiveTab.capture();
        mCustomViewContainer.setVisibility(View.VISIBLE);
        mCustomViewContainer.bringToFront();
        detachTab(mActiveTab);
        mContentView.setVisibility(View.GONE);
        if (showingNavScreen()) {
            mNavScreen.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            mTabControl.setOnThumbnailUpdatedListener(mNavScreen);
        }
        mUiController.setBlockEvents(false);
    }

    void hideNavScreen(int position, boolean animate) {
        mShowNav = false;
        if (!showingNavScreen()) return;
        final Tab tab = mUiController.getTabControl().getTab(position);
        if (tab != null) {
            setActiveTab(tab);
        } else if (mTabControl.getTabCount() > 0) {
            setActiveTab(mTabControl.getCurrentTab());
        }
        mContentView.setVisibility(View.VISIBLE);
        mTabControl.setOnThumbnailUpdatedListener(null);
        mNavScreen.setVisibility(View.GONE);
        mCustomViewContainer.setAlpha(1f);
        mCustomViewContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean needsRestoreAllTabs() {
        return false;
    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return true;
    }

}
