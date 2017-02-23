package com.sabaibrowser;

public class Blocker {

    public static boolean isAvailable() {
        return false;
    }

    public interface ContentBlocker {
        public boolean isBlocked(String url, String pageHost);
    }
}
