package com.sabaibrowser.blocker;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DefaultContentBlocker implements Blocker.ContentBlocker {

    ArrayList<Rule> mRules;
    Context mContext;

    public DefaultContentBlocker(Context context) {
        BufferedReader reader;

        try {
            final InputStream file = mContext.getAssets().open("blocklist.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line;
            do {
                line = reader.readLine();
                if (line.charAt(0) == '!') continue;
                Rule r = new Rule(line);
                mRules.add(r);
            } while (line != null);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public boolean isBlocked(String elementUrl, String pageDomain) {
        return false;
    }
}
