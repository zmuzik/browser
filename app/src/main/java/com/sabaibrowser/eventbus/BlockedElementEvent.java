package com.sabaibrowser.eventbus;

import com.sabaibrowser.Tab;

import java.lang.ref.WeakReference;

public class BlockedElementEvent {
    WeakReference<Tab> mTab;
    String mUrl;

    public BlockedElementEvent(Tab tab, String url) {
        mTab = new WeakReference<Tab>(tab);
        mUrl = url;
    }

    public Tab getTab() {
        return mTab == null ? null : mTab.get();
    }
}
