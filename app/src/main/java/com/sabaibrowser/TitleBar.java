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

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Base class for a title bar used by the browser.
 */
public class TitleBar extends RelativeLayout {

    private static final int PROGRESS_MAX = 100;

    private UiController mUiController;
    private UI mUi;
    private FrameLayout mContentView;
    private PageProgressView mProgress;

    private NavigationBar mNavBar;

    //state
    private boolean mShowing;
    private boolean mHideLoad;
    private boolean mInLoad;
    private boolean mIsFixedTitleBar;
    private boolean mShrank;
    private TextView mSmallBar;

    public TitleBar(Context context, UiController controller, UI ui, FrameLayout contentView) {
        super(context, null);
        mUiController = controller;
        mUi = ui;
        mContentView = contentView;
        initLayout(context);
        setFixedTitleBar();
    }

    private void initLayout(Context context) {
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.title_bar, this);
        mProgress = (PageProgressView) findViewById(R.id.progress);
        mNavBar = (NavigationBar) findViewById(R.id.taburlbar);
        mNavBar.setTitleBar(this);
        mSmallBar = (TextView) findViewById(R.id.smallBar);
        mSmallBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                unShrink();
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        setFixedTitleBar();
    }

    private void setFixedTitleBar() {
        ViewGroup parent = (ViewGroup) getParent();
        if (mIsFixedTitleBar && parent != null) return;
        mIsFixedTitleBar = true;
        show();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mIsFixedTitleBar) {
            mUi.addFixedTitleBar(this);
        } else {
            mContentView.addView(this, makeLayoutParams());
            mUi.setContentViewMarginTop(0);
        }
    }

    public UI getUi() {
        return mUi;
    }

    public UiController getUiController() {
        return mUiController;
    }

    void show() {
        setVisibility(View.VISIBLE);
        setTranslationY(0);
        mShowing = true;
    }

    void hide() {
        if (mIsFixedTitleBar) return;
        onScrollChanged();
        mShowing = false;
    }

    public void shrink() {
        if (mShrank || mNavBar == null) return;
        mNavBar.setVisibility(View.GONE);
        mShrank = true;
        if (getUi().mBubbleMenu == null) return;
        getUi().mBubbleMenu.setVisibility(View.GONE);
    }

    public void unShrink() {
        if (!mShrank || mNavBar == null) return;
        mNavBar.setVisibility(View.VISIBLE);
        mShrank = false;
        if (getUi().mBubbleMenu == null) return;
        getUi().mBubbleMenu.setVisibility(View.VISIBLE);
    }

    public boolean isShrank() {
        return mShrank;
    }

    boolean isShowing() {
        return mShowing;
    }

    private int getVisibleTitleHeight() {
        return 0;
    }

    /**
     * Update the progress, from 0 to 100.
     */
    public void setProgress(int newProgress) {
        if (newProgress >= PROGRESS_MAX) {
            mProgress.setProgress(PageProgressView.MAX_PROGRESS);
            mProgress.setVisibility(View.GONE);
            mInLoad = false;
            mHideLoad = false;
            mNavBar.onProgressStopped();
        } else {
            if (!mInLoad) {
                mProgress.setVisibility(View.VISIBLE);
                mInLoad = true;
                mNavBar.onProgressStarted();
            }
            mProgress.setProgress(newProgress * PageProgressView.MAX_PROGRESS
                    / PROGRESS_MAX);
            if (!isHideLoad()) {
                hide();
                mHideLoad = true;
            }
        }
    }

    public int getEmbeddedHeight() {
        if (mIsFixedTitleBar) return 0;
        return calculateEmbeddedHeight();
    }

    private int calculateEmbeddedHeight() {
        return mNavBar.getHeight();
    }

    public boolean isEditingUrl() {
        return mNavBar.isEditingUrl();
    }

    public WebView getCurrentWebView() {
        Tab t = mUi.getActiveTab();
        if (t != null) {
            return t.getWebView();
        } else {
            return null;
        }
    }

    public NavigationBar getNavigationBar() {
        return mNavBar;
    }

    public boolean isHideLoad() {
        return mHideLoad;
    }

    public boolean isInLoad() {
        return mInLoad;
    }

    private ViewGroup.LayoutParams makeLayoutParams() {
        return new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View focusSearch(View focused, int dir) {
        WebView web = getCurrentWebView();
        if (FOCUS_DOWN == dir && hasFocus() && web != null
                && web.hasFocusable() && web.getParent() != null) {
            return web;
        }
        return super.focusSearch(focused, dir);
    }

    public void onTabDataChanged(Tab tab) {
        mSmallBar.setText(tab.getTitleToDisplay());
    }

    public void onScrollChanged() {
        if (!mShowing && !mIsFixedTitleBar) {
            setTranslationY(getVisibleTitleHeight() - getEmbeddedHeight());
        }
    }

    public void onResume() {
        setFixedTitleBar();
    }
}
