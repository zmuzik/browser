package com.sabaibrowser.blocker.disconnect;

import com.sabaibrowser.blocker.Tracker;

import java.util.ArrayList;
import java.util.List;

public class DisconnectTracker extends Tracker {
    public String mainUrl;
    public List<String> urls = new ArrayList<>();
}