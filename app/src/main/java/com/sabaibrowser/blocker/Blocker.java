package com.sabaibrowser.blocker;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;

public class Blocker {

    private static final String DEFAULT_CONTENT_BLOCKER = "com.sabaibrowser.blocker.DefaultContentBlocker";

    static ContentBlocker mContentBlocker;

    public static void init(Context ctx) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(DEFAULT_CONTENT_BLOCKER);
        } catch (ClassNotFoundException e) {
            Log.d("Blocker", "Default content blocker not found");
            return;
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor(Context.class);
            mContentBlocker = (ContentBlocker) constructor.newInstance(ctx);
        } catch (Throwable e) {
            Log.e("Blocker", "Error instantiating default blocker");
            return;
        }
        Log.d("Blocker", "Default content blocker initialized");
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
