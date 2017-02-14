package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sabaibrowser.R;

import java.util.ArrayList;

public class ParabolicBubbleMenu extends BubbleMenu {

    public ParabolicBubbleMenu(Context context) {
        super(context);
    }

    public ParabolicBubbleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = (menuItems == null) ? 0 : menuItems.size();

        if (isOpen) {
            // main bubble
            mainFab.layout(mainFabCenterX - bubbleSize / 2, mainFabCenterY - bubbleSize / 2,
                    mainFabCenterX + bubbleSize / 2, mainFabCenterY + bubbleSize / 2);

            // menu item bubbles
            double fi = 0d, fiStep = 0d;
            if (count == 1) {
                fi = Math.PI / 4;
            } else if (count == 2) {
                fi = Math.PI / 8;
                fiStep = Math.PI / 4;
            } else if (count == 3) {
                fi = Math.PI / 12;
                fiStep = Math.PI / 6;
            } else {
                fiStep = Math.PI / 2 / (count - 1);
            }

            int screenX = 0, screenY = 0, x = 0, y = 0, oldX = 0, oldY = 0;
            int bubbleDistance = (int) (1.1f * bubbleSize);
            int currentDistance = 0;
            int bend = 200;

            for (int i = 0; i < count; i++) {
                View child = menuItems.get(i);
                // translation of clean coordinates to the screen coordinates
                screenX = mainFabCenterX - x;
                screenY = mainFabCenterY - (int) (1.5f * fabDistance) + y;
                child.layout(screenX - bubbleSize / 2, screenY - bubbleSize / 2,
                        screenX + bubbleSize / 2, screenY + bubbleSize / 2);
                Log.d("COORDINATES", "placing bubble at x: " + x + " y:" + y);
                Log.d("COORDINATES", "translated into   x: " + screenX + " y:" + screenY);

                // find the center of the next bubble
                do {
                    y++;
                    // function determining the shape
                    x = (int) Math.sqrt(y * bend);
                    currentDistance = (int) Math.sqrt(Math.pow(x - oldX, 2) + Math.pow(y - oldY, 2));
                }
                while (currentDistance < bubbleDistance);
                oldX = x;
                oldY = y;
            }
        } else {
            mainFab.layout(0, 0, bubbleSize, bubbleSize);
        }
    }
}
