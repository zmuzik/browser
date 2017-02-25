package com.sabaibrowser.blocker;

import android.content.Context;

import com.sabaibrowser.blocker.disconnect.DisconnectContentBlocker;

public class Blocker {

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
        return (mContentBlocker != null && mContentBlocker.isBlocked(url, pageHost));
    }

    public interface ContentBlocker {
        public boolean isBlocked(String url, String pageHost);
    }
}
