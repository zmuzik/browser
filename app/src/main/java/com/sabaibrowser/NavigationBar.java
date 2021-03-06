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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.sabaibrowser.UrlInputView.StateListener;
import com.sabaibrowser.UrlInputView.UrlInputListener;
import com.sabaibrowser.os.Search;
import com.sabaibrowser.view.BubbleMenu;

public class NavigationBar extends LinearLayout implements
        StateListener, OnMenuItemClickListener, OnClickListener, UrlInputListener,
        OnDismissListener, OnFocusChangeListener, TextWatcher {

    private int PADDING_SIDE;
    private int PADDING_RIGHT_WITH_FAB;

    protected UI mUi;
    protected TitleBar mTitleBar;
    protected UiController mUiController;
    protected UrlInputView mUrlInput;


    private ImageView mStopButton;
    private ImageView mClearButton;
    private View mBlockedElementsIcon;
    private PopupMenu mPopupMenu;
    private boolean mOverflowMenuShowing;
    private View mIncognitoIcon;
    private BubbleMenu mFabMenu;
    private TextView mBlockedCountIndicator;
    private ImageView mShieldIcon;
    private ImageView mLockIcon;
    private int mUrlInputState;

    public NavigationBar(Context context) {
        super(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        PADDING_SIDE = Utils.dpToPx(getContext(), 8);
        PADDING_RIGHT_WITH_FAB = Utils.dpToPx(getContext(), 76);
        //XXX
        //mLockIcon = (ImageView) findViewById(R.id.lock);
        mUrlInput = (UrlInputView) findViewById(R.id.url);
        mUrlInput.setUrlInputListener(this);
        mUrlInput.setOnFocusChangeListener(this);
        mUrlInput.setSelectAllOnFocus(true);
        mUrlInput.addTextChangedListener(this);

        mStopButton = (ImageView) findViewById(R.id.stop);
        mStopButton.setOnClickListener(this);
        mClearButton = (ImageView) findViewById(R.id.clear);
        mClearButton.setOnClickListener(this);
        mBlockedElementsIcon = findViewById(R.id.blocked_elements_icon);
        setFocusState(false);
        mUrlInput.setContainer(this);
        mUrlInput.setStateListener(this);
        mIncognitoIcon = findViewById(R.id.incognito_icon);
        mUrlInput.setOnTouchListener(new SwipeListener(getContext()) {
            public void onSwipeUp(float velocity) {
                stopEditingUrl();
                mUi.showNavScreen();
            }

            public void onSwipeDown(float velocity) {
                mTitleBar.shrink();
            }
        });
        mBlockedCountIndicator = (TextView) findViewById(R.id.blocked_count_indicator);
        mShieldIcon = (ImageView) findViewById(R.id.shield);
        mShieldIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUi != null) mUi.toggleBlockedInfo();
            }
        });
    }

    public void setTitleBar(TitleBar titleBar) {
        mTitleBar = titleBar;
        mUi = mTitleBar.getUi();
        mUiController = mTitleBar.getUiController();
        mUrlInput.setController(mUiController);
    }

    public void setLock(Drawable d) {
        if (mLockIcon == null) return;
        if (d == null) {
            mLockIcon.setVisibility(View.GONE);
        } else {
            mLockIcon.setImageDrawable(d);
            mLockIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mStopButton) {
            if (mTitleBar.isInLoad()) {
                mUiController.stopLoading();
            } else {
                WebView web = mUi.getWebView();
                if (web != null) {
                    stopEditingUrl();
                    web.reload();
                }
            }
        } else if (mClearButton == v) {
            if (mUrlInput.getText().length() == 0) {
                mUrlInput.clearFocus();
            } else {
                mUrlInput.setText("");
            }
        }
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

        // if losing focus and not in touch mode, leave as is
        if (hasFocus || view.isInTouchMode() || mUrlInput.needsUpdate()) {
            setFocusState(hasFocus);
        }
        if (hasFocus) {
            mUi.showTitleBar();
        } else if (!mUrlInput.needsUpdate()) {
            mUrlInput.dismissDropDown();
            mUrlInput.hideIME();
            if (mUrlInput.getText().length() == 0) {
                Tab currentTab = mUiController.getTabControl().getCurrentTab();
                if (currentTab != null) {
                    setDisplayTitle(currentTab.getUrl());
                }
            }
            mUi.suggestHideTitleBar();
        }
        mUrlInput.clearNeedsUpdate();
    }

    protected void setFocusState(boolean focus) {
    }

    public boolean isEditingUrl() {
        return mUrlInput.hasFocus();
    }

    void stopEditingUrl() {
        WebView currentTopWebView = mUiController.getCurrentTopWebView();
        if (currentTopWebView != null) {
            currentTopWebView.requestFocus();
        }
    }

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

    void setDisplayTitle(Tab tab) {
        mUrlInput.setTag(UrlUtils.stripUrl(tab.mCurrentState.mUrl));
        if (!isEditingUrl()) {
            mUrlInput.setText(tab.getTitleToDisplay());
            mUrlInput.setSelection(0);
        }
    }

    void setIncognitoMode(boolean incognito) {
        mUrlInput.setIncognitoMode(incognito);
    }

    void clearCompletions() {
        mUrlInput.dismissDropDown();
    }

    // UrlInputListener implementation

    /**
     * callback from suggestion dropdown
     * user selected a suggestion
     */
    @Override
    public void onAction(String text, String extra, String source) {
        stopEditingUrl();
        if (UrlInputView.TYPED.equals(source)) {
            String url = UrlUtils.smartUrlFilter(text, false);
            Tab t = mUi.getActiveTab();
            // Only shortcut javascript URIs for now, as there is special
            // logic in UrlHandler for other schemas
            if (url != null && t != null && url.startsWith("javascript:")) {
                mUiController.loadUrl(t, url);
                setDisplayTitle(text);
                return;
            }
        }
        Intent i = new Intent();
        String action = Intent.ACTION_SEARCH;
        i.setAction(action);
        i.putExtra(SearchManager.QUERY, text);
        if (extra != null) {
            i.putExtra(SearchManager.EXTRA_DATA_KEY, extra);
        }
        if (source != null) {
            Bundle appData = new Bundle();
            appData.putString(Search.SOURCE, source);
            i.putExtra(SearchManager.APP_DATA, appData);
        }
        mUiController.handleNewIntent(i);
        setDisplayTitle(text);
    }

    @Override
    public void onDismiss() {
        final Tab currentTab = mUi.getActiveTab();
        mUi.hideTitleBar();
        post(new Runnable() {
            public void run() {
                clearFocus();
                if (currentTab != null) {
                    setDisplayTitle(currentTab.getUrl());
                }
            }
        });
    }

    private void onMenuHidden() {
        mOverflowMenuShowing = false;
        mUi.showTitleBarForDuration();
    }

    /**
     * callback from the suggestion dropdown
     * copy text to input field and stay in edit mode
     */
    @Override
    public void onCopySuggestion(String text) {
        mUrlInput.setText(text, true);
        if (text != null) {
            mUrlInput.setSelection(text.length());
        }
    }

    public void setCurrentUrlIsBookmark(boolean isBookmark) {
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // catch back key in order to do slightly more cleanup than usual
            stopEditingUrl();
            return true;
        }
        return super.dispatchKeyEventPreIme(evt);
    }

    /**
     * called from the UI when the user wants to edit
     *
     * @param clearInput clear the input field
     */
    void startEditingUrl(boolean clearInput, boolean forceIME) {
        // editing takes preference of progress
        setVisibility(View.VISIBLE);
        if (!mUrlInput.hasFocus()) {
            mUrlInput.requestFocus();
        }
        if (clearInput) {
            mUrlInput.setText("");
        }
        if (forceIME) {
            mUrlInput.showIME();
        }
    }

    public void onProgressStarted() {
        mStopButton.setVisibility(View.VISIBLE);
    }

    public void onProgressStopped() {
        mStopButton.setVisibility(View.GONE);
        onStateChanged(mUrlInput.getState());
    }

    @Override
    public void onStateChanged(int state) {
        mUrlInputState = state;
        switch (state) {
            case UrlInputView.StateListener.STATE_NORMAL:
                mStopButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.GONE);
                if (mFabMenu != null && !mTitleBar.isShrank()) {
                    mFabMenu.setVisibility(View.VISIBLE);
                }
                setPadding(PADDING_SIDE, 0, PADDING_RIGHT_WITH_FAB, 0);
                mBlockedElementsIcon.setVisibility(View.VISIBLE);
                break;
            case UrlInputView.StateListener.STATE_HIGHLIGHTED:
                mStopButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
                mFabMenu.setVisibility(View.GONE);
                setPadding(PADDING_SIDE, 0, PADDING_SIDE, 0);
                mBlockedElementsIcon.setVisibility(View.GONE);
                break;
            case UrlInputView.StateListener.STATE_EDITED:
                mStopButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
                setPadding(PADDING_SIDE, 0, PADDING_SIDE, 0);
                mFabMenu.setVisibility(View.GONE);
                mBlockedElementsIcon.setVisibility(View.GONE);
                break;
        }
    }

    public boolean isMenuShowing() {
        return mOverflowMenuShowing;
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

    public void onTabDataChanged(Tab tab) {
        mIncognitoIcon.setVisibility(tab.isPrivateBrowsingEnabled()
                ? View.VISIBLE : View.GONE);
        if (tab != null && tab.mCurrentState != null) {
            mBlockedCountIndicator.setText("" + tab.mCurrentState.mTrackers.size());
        }
    }

    public void onVoiceResult(String s) {
        startEditingUrl(true, true);
        onCopySuggestion(s);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }

    public void setFabMenu(BubbleMenu fabMenu) {
        mFabMenu = fabMenu;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        if (mUrlInputState != UrlInputView.StateListener.STATE_NORMAL) {
//            if (s.length() == 0) {
//                mClearButton.setVisibility(View.GONE);
//            } else {
//                mClearButton.setVisibility(View.VISIBLE);
//            }
//        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }


    @Override
    public void onDismiss(PopupMenu menu) {
        if (menu == mPopupMenu) {
            onMenuHidden();
        }
    }

}
