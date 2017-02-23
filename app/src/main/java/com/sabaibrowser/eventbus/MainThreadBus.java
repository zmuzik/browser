package com.sabaibrowser.eventbus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class MainThreadBus extends Bus {

    private static MainThreadBus bus;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static MainThreadBus get() {
        if (bus == null) {
            bus = new MainThreadBus();
        }
        return bus;
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }
}