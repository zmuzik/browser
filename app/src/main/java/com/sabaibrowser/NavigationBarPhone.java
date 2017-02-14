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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.sabaibrowser.UrlInputView.StateListener;
import com.sabaibrowser.view.Bubble;

public class NavigationBarPhone extends NavigationBarBase implements
        StateListener, OnMenuItemClickListener, OnDismissListener {

    private ImageView mStopButton;
    private ImageView mMagnify;
    private ImageView mClearButton;
    //private ImageView mFabFilling;
    private Drawable mStopDrawable;
    private Drawable mRefreshDrawable;
    private String mStopDescription;
    private String mRefreshDescription;
    private View mTitleContainer;
    private Drawable mTextfieldBgDrawable;
    private PopupMenu mPopupMenu;
    private boolean mOverflowMenuShowing;
    private View mIncognitoIcon;
    private Bubble mFab;

    public NavigationBarPhone(Context context) {
        super(context);
    }

    public NavigationBarPhone(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationBarPhone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mStopButton = (ImageView) findViewById(R.id.stop);
        mStopButton.setOnClickListener(this);
        mClearButton = (ImageView) findViewById(R.id.clear);
        mClearButton.setOnClickListener(this);
        mMagnify = (ImageView) findViewById(R.id.magnify);
        mTitleContainer = findViewById(R.id.title_bg);
        //mFabFilling = (ImageView) findViewById(R.id.fab_filling);
        setFocusState(false);
        Resources res = getContext().getResources();
        mStopDrawable = res.getDrawable(R.drawable.ic_stop);
        mRefreshDrawable = res.getDrawable(R.drawable.ic_refresh);
        mStopDescription = res.getString(R.string.accessibility_button_stop);
        mRefreshDescription = res.getString(R.string.accessibility_button_refresh);
        //mTextfieldBgDrawable = res.getDrawable(R.drawable.textfield_active_holo_dark);
        mUrlInput.setContainer(this);
        mUrlInput.setStateListener(this);
        mIncognitoIcon = findViewById(R.id.incognito_icon);
        mUrlInput.setOnTouchListener(new SwipeListener(getContext()) {
            public void onSwipeUp() {
                stopEditingUrl();
                ((PhoneUi)mBaseUi).showNavScreen();
            }
        });
    }

    @Override
    public void onProgressStarted() {
        super.onProgressStarted();
        if (mStopButton.getDrawable() != mStopDrawable) {
            mStopButton.setImageDrawable(mStopDrawable);
            mStopButton.setContentDescription(mStopDescription);
            if (mStopButton.getVisibility() != View.VISIBLE) {
                mStopButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onProgressStopped() {
        super.onProgressStopped();
        mStopButton.setImageDrawable(mRefreshDrawable);
        mStopButton.setContentDescription(mRefreshDescription);
        onStateChanged(mUrlInput.getState());
    }

    /**
     * Update the text displayed in the title bar.
     * @param title String to display.  If null, the new tab string will be
     *      shown.
     */
    @Override
    void setDisplayTitle(String title) {
        mUrlInput.setTag(title);
        if (!isEditingUrl()) {
            if (title == null) {
                mUrlInput.setText(R.string.new_tab);
            } else {
                mUrlInput.setText(UrlUtils.stripUrl(title), false);
            }
            mUrlInput.setSelection(0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mStopButton) {
            if (mTitleBar.isInLoad()) {
                mUiController.stopLoading();
            } else {
                WebView web = mBaseUi.getWebView();
                if (web != null) {
                    stopEditingUrl();
                    web.reload();
                }
            }
        } else if (mClearButton == v) {
            mUrlInput.setText("");
        } else {
            super.onClick(v);
        }
    }

    @Override
    public boolean isMenuShowing() {
        return super.isMenuShowing() || mOverflowMenuShowing;
    }

    void showMenu(View anchor) {
        Activity activity = mUiController.getActivity();
        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(getContext(), anchor);
            mPopupMenu.setOnMenuItemClickListener(this);
            mPopupMenu.setOnDismissListener(this);
            if (!activity.onCreateOptionsMenu(mPopupMenu.getMenu())) {
                mPopupMenu = null;
                return;
            }
        }
        Menu menu = mPopupMenu.getMenu();
        if (activity.onPrepareOptionsMenu(menu)) {
            mOverflowMenuShowing = true;
            mPopupMenu.show();
        }
    }

    @Override
    public void onDismiss(PopupMenu menu) {
        if (menu == mPopupMenu) {
            onMenuHidden();
        }
    }

    private void onMenuHidden() {
        mOverflowMenuShowing = false;
        mBaseUi.showTitleBarForDuration();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == mUrlInput) {
            if (hasFocus && !mUrlInput.getText().toString().equals(mUrlInput.getTag())) {
                // only change text if different
                mUrlInput.setText((String) mUrlInput.getTag(), false);
                mUrlInput.selectAll();
            } else {
                setDisplayTitle(mUrlInput.getText().toString());
            }
        }
        super.onFocusChange(view, hasFocus);
    }

    @Override
    public void onStateChanged(int state) {
        switch(state) {
        case StateListener.STATE_NORMAL:
            mStopButton.setVisibility(View.GONE);
            mClearButton.setVisibility(View.GONE);
            mMagnify.setVisibility(View.GONE);
            mTitleContainer.setBackgroundDrawable(null);
            //mFabFilling.setVisibility(View.VISIBLE);
            if (mFab != null) {
                mFab.setVisibility(View.VISIBLE);
            }
            break;
        case StateListener.STATE_HIGHLIGHTED:
            mStopButton.setVisibility(View.VISIBLE);
            mClearButton.setVisibility(View.GONE);
            mMagnify.setVisibility(View.GONE);
            //mFabFilling.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
            break;
        case StateListener.STATE_EDITED:
            mStopButton.setVisibility(View.GONE);
            mClearButton.setVisibility(View.VISIBLE);
            mMagnify.setVisibility(View.VISIBLE);
            //mFabFilling.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
            break;
        }
    }

    @Override
    public void onTabDataChanged(Tab tab) {
        super.onTabDataChanged(tab);
        mIncognitoIcon.setVisibility(tab.isPrivateBrowsingEnabled()
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }

    public void setFab(Bubble fab) {
        mFab = fab;
    }

}
