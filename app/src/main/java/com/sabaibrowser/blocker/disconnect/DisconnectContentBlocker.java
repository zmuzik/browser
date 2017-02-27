package com.sabaibrowser.blocker.disconnect;

import android.content.Context;
import android.util.JsonReader;

import com.sabaibrowser.blocker.Blocker;
import com.sabaibrowser.os.WebAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DisconnectContentBlocker implements Blocker.ContentBlocker {

    private final String TAG = getClass().getSimpleName();
    private List<Tracker> mTrackers;
    private Context mContext;

    public DisconnectContentBlocker(Context context) {
        mContext = context;
        mTrackers = readTrackers("lists/services.json");
    }

    private List<Tracker> readTrackers(String fileName) {
        List<Tracker> result = new ArrayList<Tracker>();
        try {
            final InputStream in = mContext.getAssets().open(fileName);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            // whole file
            reader.beginObject();
            // skip license
            reader.nextName();
            reader.skipValue();
            // open categories
            if (!"categories".equals(reader.nextName())) return null;
            reader.beginObject();
            // iterate categories
            while (reader.hasNext()) {
                String category = reader.nextName();
                if ("Content".equals(category)) {
                    reader.skipValue();
                } else {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        result.add(getTracker(reader));
                    }
                    reader.endArray();
                }
            }
            // end categories
            reader.endObject();
            // end file
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    private Tracker getTracker(JsonReader reader) throws IOException {
        Tracker result = new Tracker();
        reader.beginObject();
        while (reader.hasNext()) {
            ArrayList<String> urls = new ArrayList<String>();
            result.name = reader.nextName();
            reader.beginObject();
            result.mainUrl = reader.nextName();
            reader.beginArray();
            while (reader.hasNext()) {
                result.urls.add(reader.nextString());
            }
            reader.endArray();
            reader.endObject();
        }
        reader.endObject();
        return result;
    }

    @Override
    public boolean isBlocked(String elementUrl, String pageDomain) {
        String elementDomain = new WebAddress(elementUrl).getHost();
        // don't block elements from the same domain
        if (elementDomain.equals(pageDomain)) return false;
        for (Tracker tracker : mTrackers) {
            for (String trackerDomain : tracker.urls) {
                if (elementDomain.endsWith(trackerDomain)) {
                    for (String otherTrackerDomain : tracker.urls) {
                        if (trackerDomain.equals(otherTrackerDomain)) continue;
                        if (elementDomain.endsWith(otherTrackerDomain)) return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
