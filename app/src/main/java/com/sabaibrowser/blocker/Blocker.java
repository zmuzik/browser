package com.sabaibrowser.blocker;

import android.content.Context;
import android.util.Log;

import com.sabaibrowser.BuildConfig;
import com.sabaibrowser.blocker.disconnect.DisconnectContentBlocker;

public class Blocker {
    private static final String TAG = "Blocker";
    private static boolean logBlockedItems = BuildConfig.DEBUG;
    private static ContentBlocker mContentBlocker;

    public static void init(final Context ctx) {
        new Thread(new Runnable() {
            public void run() {
                mContentBlocker = new DisconnectContentBlocker(ctx);
            }
        }).start();
    }

    public static boolean isAvailable() {
        return mContentBlocker == null;
    }

    public static boolean isBlocked(String url, String pageHost) {
        boolean blocked = (mContentBlocker != null && mContentBlocker.isBlocked(url, pageHost));
        if (blocked && logBlockedItems) {
            Log.d(TAG, "blocked: " + url);
        }
        return blocked;
    }

    public interface ContentBlocker {
        public boolean isBlocked(String url, String pageHost);
    }
}
