/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (c) 2012, Code Aurora Forum. All rights reserved.
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
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sabaibrowser.eventbus.BlockedElementEvent;
import com.sabaibrowser.eventbus.MainThreadBus;
import com.sabaibrowser.view.BlockedElementsDialog;
import com.sabaibrowser.view.Bubble;
import com.sabaibrowser.view.BubbleMenu;
import com.squareup.otto.Subscribe;

import java.util.List;

public class UI {

    private static final String LOGTAG = "UI";

    private static final int MSG_INIT_NAVSCREEN = 100;

    private NavScreen mNavScreen;
    private int mActionBarHeight;

    boolean mShowNav = false;

    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

    protected static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER);

    private static final int MSG_HIDE_TITLEBAR = 1;
    private static final int MSG_HIDE_CUSTOM_VIEW = 2;
    public static final int HIDE_TITLEBAR_DELAY = 1500; // in ms
    public static final int HIDE_CUSTOM_VIEW_DELAY = 200; // in ms
    BubbleMenu mBubbleMenu;

    Activity mActivity;
    UiController mUiController;
    TabControl mTabControl;
    protected Tab mActiveTab;
    private InputMethodManager mInputManager;

    protected SwipeRefreshLayout mSwipeContainer;
    protected FrameLayout mContentView;
    protected FrameLayout mCustomViewContainer;
    protected FrameLayout mFullscreenContainer;
    private FrameLayout mFixedTitlebarContainer;
    private FrameLayout mBlockedElementsContainer;

    private View mCustomView;
    private View mDecorView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;

    private UrlBarAutoShowManager mUrlBarAutoShowManager;

    private Toast mStopToast;

    // the default <video> poster
    private Bitmap mDefaultVideoPoster;
    // the video progress view
    private View mVideoProgressView;

    private boolean mActivityPaused;
    protected TitleBar mTitleBar;
    private NavigationBar mNavigationBar;
    private BlockedElementsDialog mBlockedElementsDialog;

    private int mLastWebViewScrollY;

    public static enum ComboViews {
        History,
        Bookmarks,
        Snapshots,
    }

    public UI(Activity browser, UiController controller) {
        mActivity = browser;
        mUiController = controller;
        mTabControl = controller.getTabControl();
        Resources res = mActivity.getResources();
        mInputManager = (InputMethodManager)
                browser.getSystemService(Activity.INPUT_METHOD_SERVICE);
        FrameLayout frameLayout = (FrameLayout) mActivity.getWindow()
                .getDecorView().findViewById(android.R.id.content);
        LayoutInflater.from(mActivity)
                .inflate(R.layout.custom_screen, frameLayout);
        mFixedTitlebarContainer = (FrameLayout) frameLayout.findViewById(
                R.id.fixed_titlebar_container);
        mContentView = (FrameLayout) frameLayout.findViewById(
                R.id.main_content);
        mCustomViewContainer = (FrameLayout) frameLayout.findViewById(
                R.id.fullscreen_custom_content);
        mBlockedElementsContainer = (FrameLayout)
                frameLayout.findViewById(R.id.blocked_dialog_container);
        mBubbleMenu = (BubbleMenu) frameLayout.findViewById(R.id.bubble_menu);
        setImmersiveFullscreen(false);
        mTitleBar = new TitleBar(mActivity, mUiController, this,
                mContentView);
        mTitleBar.setProgress(100);
        mNavigationBar = mTitleBar.getNavigationBar();
        mUrlBarAutoShowManager = new UrlBarAutoShowManager(this);
        mSwipeContainer = (SwipeRefreshLayout) frameLayout.findViewById(R.id.swipe_container);
        mSwipeContainer.setColorSchemeColors(browser.getResources().getColor(R.color.primary));
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mUiController.getCurrentTopWebView().reload();
                mSwipeContainer.setRefreshing(false);
            }
        });
        mSwipeContainer.getViewTreeObserver().addOnScrollChangedListener(
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (getWebView() == null) return;
                        int scrollY = getWebView().getScrollY();

                        // workaround for swipetorefresh not "stealing" the scrolling
                        if (scrollY == 0) {
                            mSwipeContainer.setEnabled(true);
                        } else {
                            mSwipeContainer.setEnabled(false);
                        }

                        if (mLastWebViewScrollY < scrollY
                                && mLastWebViewScrollY != 0
                                && !mTitleBar.isShrank()) {
                            mTitleBar.shrink();
                        } else if (mLastWebViewScrollY > scrollY
                                && scrollY != 0
                                && mTitleBar.isShrank()) {
                            mTitleBar.unShrink();
                        }
                        mLastWebViewScrollY = scrollY;
                    }
                }
        );

        mNavigationBar = mTitleBar.getNavigationBar();
        mNavigationBar.setFabMenu(mBubbleMenu);
        initBubbleMenu();
        TypedValue heightValue = new TypedValue();
        browser.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, heightValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(heightValue.data,
                browser.getResources().getDisplayMetrics());

        MainThreadBus.get().register(this);
    }

    private void initBubbleMenu() {
        mBubbleMenu.addMenuItem(R.drawable.ic_windows, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                showNavScreen();
            }
        });
        mBubbleMenu.addMenuItem(R.drawable.ic_shield_white, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBubbleMenu.closeMenu();
                showBlockedInfo();
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

    public void onDestroy() {
        MainThreadBus.get().unregister(this);
        hideTitleBar();
    }

    private void cancelStopToast() {
        if (mStopToast != null) {
            mStopToast.cancel();
            mStopToast = null;
        }
    }

    // lifecycle

    public void onPause() {
        if (isCustomViewShowing()) {
            onHideCustomView();
        }
        hideBlockedInfo();
        if (mBubbleMenu != null) mBubbleMenu.closeMenu();
        cancelStopToast();
        mActivityPaused = true;
    }

    public void onResume() {
        mActivityPaused = false;
        // check if we exited without setting active tab
        // b: 5188145
        final Tab ct = mTabControl.getCurrentTab();
        if (ct != null) {
            setActiveTab(ct);
        }
        mTitleBar.onResume();
    }

    protected boolean isActivityPaused() {
        return mActivityPaused;
    }

    public void onConfigurationChanged(Configuration config) {
        hideBlockedInfo();
        if (mBubbleMenu != null) mBubbleMenu.closeMenu();
    }

    public Activity getActivity() {
        return mActivity;
    }

    public boolean onBackKey() {
        if (showingNavScreen()) {
            mNavScreen.close(mUiController.getTabControl().getCurrentPosition());
            return true;
        }
        if (mCustomView != null) {
            mUiController.hideCustomView();
            return true;
        }
        if (mBlockedElementsDialog != null) {
            hideBlockedInfo();
            return true;
        }
        if (mBubbleMenu != null && mBubbleMenu.isOpen()) {
            mBubbleMenu.closeMenu();
            return true;
        }
        return false;
    }

    private boolean showingNavScreen() {
        return mNavScreen != null && mNavScreen.getVisibility() == View.VISIBLE;
    }

    public boolean dispatchKey(int code, KeyEvent event) {
        return false;
    }

    public boolean onMenuKey() {
        return false;
    }

    public void onTabDataChanged(Tab tab) {
        setUrlTitle(tab);
        updateNavigationState(tab);
        mTitleBar.onTabDataChanged(tab);
        mNavigationBar.onTabDataChanged(tab);
        onProgressChanged(tab);
    }

    public void onProgressChanged(Tab tab) {
        int progress = tab.getLoadProgress();
        if (tab.inForeground()) {
            mTitleBar.setProgress(progress);
        }
        if (mNavScreen == null && getTitleBar().getHeight() > 0) {
            mHandler.sendEmptyMessage(MSG_INIT_NAVSCREEN);
        }
    }

    public void bookmarkedStatusHasChanged(Tab tab) {
        if (tab.inForeground()) {
            boolean isBookmark = tab.isBookmarkedSite();
            mNavigationBar.setCurrentUrlIsBookmark(isBookmark);
        }
    }

    public void onPageStopped(Tab tab) {
        cancelStopToast();
        if (tab.inForeground()) {
            mStopToast = Toast
                    .makeText(mActivity, R.string.stopping, Toast.LENGTH_SHORT);
            mStopToast.show();
        }
    }

    public boolean needsRestoreAllTabs() {
        return false;
    }

    public boolean shouldCaptureThumbnails() {
        return true;
    }

    public void addTab(Tab tab) {
    }

    public void setActiveTab(final Tab tab) {
        if (tab == null) return;
        // block unnecessary focus change animations during tab switch
        mHandler.removeMessages(MSG_HIDE_TITLEBAR);
        if ((tab != mActiveTab) && (mActiveTab != null)) {
            removeTabFromContentView(mActiveTab);
            WebView web = mActiveTab.getWebView();
            if (web != null) {
                web.setOnTouchListener(null);
            }
        }
        mActiveTab = tab;
        BrowserWebView web = (BrowserWebView) mActiveTab.getWebView();
        updateUrlBarAutoShowManagerTarget();
        attachTabToContentView(tab);
        if (web != null) {
            web.setTitleBar(mTitleBar);
            mTitleBar.onScrollChanged();
        }
        mTitleBar.bringToFront();
        tab.getTopWindow().requestFocus();
        setShouldShowErrorConsole(tab, mUiController.shouldShowErrorConsole());
        onTabDataChanged(tab);
        onProgressChanged(tab);
        mNavigationBar.setIncognitoMode(tab.isPrivateBrowsingEnabled());

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
        mNavigationBar.onStateChanged(UrlInputView.StateListener.STATE_NORMAL);
        mLastWebViewScrollY = 0;
    }

    protected void updateUrlBarAutoShowManagerTarget() {
        WebView web = mActiveTab != null ? mActiveTab.getWebView() : null;
        if (web instanceof BrowserWebView) {
            mUrlBarAutoShowManager.setTarget((BrowserWebView) web);
        } else {
            mUrlBarAutoShowManager.setTarget(null);
        }
    }

    Tab getActiveTab() {
        return mActiveTab;
    }

    public void updateTabs(List<Tab> tabs) {
    }

    public void removeTab(Tab tab) {
        if (mActiveTab == tab) {
            removeTabFromContentView(tab);
            mActiveTab = null;
        }
    }

    public void detachTab(Tab tab) {
        removeTabFromContentView(tab);
    }

    public void attachTab(Tab tab) {
        attachTabToContentView(tab);
    }

    protected void attachTabToContentView(Tab tab) {
        if ((tab == null) || (tab.getWebView() == null)) {
            return;
        }
        View container = tab.getViewContainer();
        WebView mainView = tab.getWebView();
        // Attach the WebView to the container and then attach the
        // container to the content view.
        FrameLayout wrapper =
                (FrameLayout) container.findViewById(R.id.webview_wrapper);
        ViewGroup parent = (ViewGroup) mainView.getParent();
        if (parent != wrapper) {
            if (parent != null) {
                parent.removeView(mainView);
            }
            wrapper.addView(mainView);
        }
        parent = (ViewGroup) container.getParent();
        if (parent != mContentView) {
            if (parent != null) {
                parent.removeView(container);
            }
            mContentView.addView(container, COVER_SCREEN_PARAMS);
        }
    }

    private void removeTabFromContentView(Tab tab) {
        hideTitleBar();
        // Remove the container that contains the main WebView.
        WebView mainView = tab.getWebView();
        View container = tab.getViewContainer();
        if (mainView == null) {
            return;
        }
        // Remove the container from the content and then remove the
        // WebView from the container. This will trigger a focus change
        // needed by WebView.
        FrameLayout wrapper =
                (FrameLayout) container.findViewById(R.id.webview_wrapper);
        wrapper.removeView(mainView);
        mContentView.removeView(container);
        mUiController.endActionMode();
    }

    public void onSetWebView(Tab tab, WebView webView) {
        View container = tab.getViewContainer();
        if (container == null) {
            // The tab consists of a container view, which contains the main
            // WebView, as well as any other UI elements associated with the tab.
            container = mActivity.getLayoutInflater().inflate(R.layout.tab,
                    mContentView, false);
            tab.setViewContainer(container);
        }
        if (tab.getWebView() != webView) {
            // Just remove the old one.
            FrameLayout wrapper =
                    (FrameLayout) container.findViewById(R.id.webview_wrapper);
            wrapper.removeView(tab.getWebView());
        }
    }

    protected void refreshWebView() {
        WebView web = getWebView();
        if (web != null) {
            web.invalidate();
        }
    }

    public void editUrl(boolean clearInput, boolean forceIME) {
        if (mShowNav) return;
        if (mUiController.isInCustomActionMode()) {
            mUiController.endActionMode();
        }
        showTitleBar();
        if ((getActiveTab() != null) && !getActiveTab().isSnapshot()) {
            mNavigationBar.startEditingUrl(clearInput, forceIME);
        }

    }

    boolean canShowTitleBar() {
        return !isTitleBarShowing()
                && !isActivityPaused()
                && (getActiveTab() != null)
                && (getWebView() != null)
                && !mUiController.isInCustomActionMode();
    }

    protected void showTitleBar() {
        mHandler.removeMessages(MSG_HIDE_TITLEBAR);
        if (canShowTitleBar()) {
            mTitleBar.show();
        }
    }

    protected void hideTitleBar() {
        if (mTitleBar.isShowing()) {
            mTitleBar.hide();
        }
    }

    protected boolean isTitleBarShowing() {
        return mTitleBar.isShowing();
    }

    public boolean isEditingUrl() {
        return mTitleBar.isEditingUrl();
    }

    public void stopEditingUrl() {
        mTitleBar.getNavigationBar().stopEditingUrl();
    }

    public TitleBar getTitleBar() {
        return mTitleBar;
    }

    public void showCustomView(View view, int requestedOrientation,
                               WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        mOriginalOrientation = mActivity.getRequestedOrientation();
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(mActivity);
        mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        mCustomView = view;
        setImmersiveFullscreen(true);
        ((BrowserWebView) getWebView()).setVisibility(View.INVISIBLE);
        mCustomViewCallback = callback;
        mActivity.setRequestedOrientation(requestedOrientation);
    }

    private void hideCustomViewAfterDuration() {
        Message msg = Message.obtain(mHandler, MSG_HIDE_CUSTOM_VIEW);
        mHandler.sendMessageDelayed(msg, HIDE_CUSTOM_VIEW_DELAY);
    }

    private void hideCustomView() {
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer = null;
        mCustomView = null;
        // Show the content view.
        mActivity.setRequestedOrientation(mOriginalOrientation);
    }

    public void onHideCustomView() {
        setImmersiveFullscreen(false);
        ((BrowserWebView) getWebView()).setVisibility(View.VISIBLE);
        if (mCustomView == null)
            return;
        mCustomViewCallback.onCustomViewHidden();
        hideCustomViewAfterDuration();
    }

    public boolean isCustomViewShowing() {
        return mCustomView != null;
    }

    protected void dismissIME() {
        if (mInputManager.isActive()) {
            mInputManager.hideSoftInputFromWindow(mContentView.getWindowToken(),
                    0);
        }
    }

    public boolean isWebShowing() {
        return mCustomView == null && !showingNavScreen();
    }

    // -------------------------------------------------------------------------

    protected void updateNavigationState(Tab tab) {
    }

    protected void setUrlTitle(Tab tab) {
        if (tab.inForeground()) {
            mNavigationBar.setDisplayTitle(tab);
        }
    }

    public void onActionModeStarted(ActionMode mode) {
        if (!isEditingUrl()) {
            hideTitleBar();
        } else {
            mTitleBar.animate().translationY(mActionBarHeight);
        }
    }

    public void onActionModeFinished(boolean inLoad) {
        mTitleBar.animate().translationY(0);
        if (inLoad) {
            showTitleBar();
        }
    }

    // menu handling callbacks

    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuState(mActiveTab, menu);
        return true;
    }

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

    public void onOptionsMenuOpened() {
    }

    public void onExtendedMenuOpened() {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (showingNavScreen()
                && (item.getItemId() != R.id.history_menu_id)
                && (item.getItemId() != R.id.snapshots_menu_id)) {
            hideNavScreen(mUiController.getTabControl().getCurrentPosition(), false);
        }
        return false;
    }

    public void onOptionsMenuClosed(boolean inLoad) {
    }

    public void onExtendedMenuClosed(boolean inLoad) {
    }

    public void onContextMenuCreated(Menu menu) {
        hideTitleBar();
    }

    public void onContextMenuClosed(Menu menu, boolean inLoad) {
        if (inLoad) {
            showTitleBar();
        }
    }

    // error console

    //TODO remove this from the api if possible
    public void setShouldShowErrorConsole(Tab tab, boolean flag) {
    }

    // -------------------------------------------------------------------------
    // Helper function for WebChromeClient
    // -------------------------------------------------------------------------

    public Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = BitmapFactory.decodeResource(
                    mActivity.getResources(), R.drawable.video_placeholder);
        }
        return mDefaultVideoPoster;
    }

    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            mVideoProgressView = inflater.inflate(
                    R.layout.video_loading_progress, null);
        }
        return mVideoProgressView;
    }

    public void showMaxTabsWarning() {
        Toast warning = Toast.makeText(mActivity,
                mActivity.getString(R.string.max_tabs_warning),
                Toast.LENGTH_SHORT);
        warning.show();
    }

    protected WebView getWebView() {
        if (mActiveTab != null) {
            return mActiveTab.getWebView();
        } else {
            return null;
        }
    }

//    protected Menu getMenu() {
//        MenuBuilder menu = new MenuBuilder(mActivity);
//        PopupMenu popup = new PopupMenu(mContext, mMore);
//        mActivity.getMenuInflater().inflate(R.menu.browser, menu);
//        return menu;
//    }
//
//    public void setFullscreen(boolean enabled) {
//        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
//        int systemUiVisibility = decor.getSystemUiVisibility();
//        final int bits = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        if (enabled) {
//            systemUiVisibility |= bits;
//        } else {
//            systemUiVisibility &= ~bits;
//        }
//        decor.setSystemUiVisibility(systemUiVisibility);
//    }

    protected void setImmersiveFullscreen(boolean enabled) {
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        int systemUiVisibility = decor.getSystemUiVisibility();
        final int bits = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (enabled) {
            systemUiVisibility |= bits;
        } else {
            systemUiVisibility &= ~bits;
        }
        decor.setSystemUiVisibility(systemUiVisibility);
    }

    public boolean isLoading() {
        return mActiveTab != null ? mActiveTab.inPageLoad() : false;
    }

    /**
     * Suggest to the UI that the title bar can be hidden. The UI will then
     * decide whether or not to hide based off a number of factors, such
     * as if the user is editing the URL bar or if the page is loading
     */
    public void suggestHideTitleBar() {
        if (!isLoading() && !isEditingUrl() && !mNavigationBar.isMenuShowing()) {
            hideTitleBar();
        }
    }

    protected final void showTitleBarForDuration() {
        showTitleBarForDuration(HIDE_TITLEBAR_DELAY);
    }

    protected final void showTitleBarForDuration(long duration) {
        showTitleBar();
        Message msg = Message.obtain(mHandler, MSG_HIDE_TITLEBAR);
        mHandler.sendMessageDelayed(msg, duration);
    }

    protected Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_HIDE_TITLEBAR) {
                suggestHideTitleBar();
            } else if (msg.what == MSG_HIDE_CUSTOM_VIEW) {
                hideCustomView();
            }
            UI.this.handleMessage(msg);
        }
    };

    protected void handleMessage(Message msg) {
        if (msg.what == MSG_INIT_NAVSCREEN) {
            if (mNavScreen == null) {
                mNavScreen = new NavScreen(mActivity, mUiController, this);
                mCustomViewContainer.addView(mNavScreen, COVER_SCREEN_PARAMS);
                mNavScreen.setVisibility(View.GONE);
            }
        }
    }

    public void showWeb(boolean animate) {
        mUiController.hideCustomView();
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

    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

    }

    public void addFixedTitleBar(View view) {
        mFixedTitlebarContainer.addView(view);
    }

    public void setContentViewMarginTop(int margin) {
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        if (params.topMargin != margin) {
            params.topMargin = margin;
            mContentView.setLayoutParams(params);
        }
    }

    public void onVoiceResult(String result) {
        mNavigationBar.onVoiceResult(result);
    }

    public void toggleBlockedInfo() {
        if (mBlockedElementsDialog == null) {
            showBlockedInfo();
        } else {
            hideBlockedInfo();
        }
    }

    public void showBlockedInfo() {
        if (mBlockedElementsDialog != null) return;
        mBlockedElementsDialog = new BlockedElementsDialog(getActivity(), this, getActiveTab());
        mBlockedElementsDialog.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM));
        mBlockedElementsContainer.addView(mBlockedElementsDialog);
        mBlockedElementsContainer.setVisibility(View.VISIBLE);
        mBubbleMenu.setVisibility(View.GONE);
    }

    public void hideBlockedInfo() {
        if (mBlockedElementsDialog == null) return;
        mContentView.removeView(mBlockedElementsDialog);
        mBlockedElementsContainer.removeView(mBlockedElementsDialog);
        mBlockedElementsDialog = null;
        mBlockedElementsContainer.setVisibility(View.GONE);
        mBubbleMenu.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void updateBlockedElementsCount(BlockedElementEvent event) {
        if (!showingNavScreen() && !isActivityPaused()) {
            mNavigationBar.onTabDataChanged(event.getTab());
        }
    }

}
