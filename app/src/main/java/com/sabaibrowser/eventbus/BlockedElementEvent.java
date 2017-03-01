package com.sabaibrowser.eventbus;

import com.sabaibrowser.Tab;
import com.sabaibrowser.blocker.Tracker;

import java.lang.ref.WeakReference;

public class BlockedElementEvent {
    WeakReference<Tab> mTab;
    Tracker mTracker;

    public BlockedElementEvent(Tab tab, Tracker tracker) {
        mTab = new WeakReference<Tab>(tab);
        mTracker = tracker;
    }

    public Tab getTab() {
        return mTab == null ? null : mTab.get();
    }
}
