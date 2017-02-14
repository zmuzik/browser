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

public class BubbleMenu extends ViewGroup {

    protected boolean isOpen = false;
    protected Bubble mainFab;

    protected ArrayList<Bubble> menuItems;

    protected int componentWidthPx;
    protected int componentHeightPx;
    protected int fabDistance;

    protected int bubbleSize;
    protected int paddingHoriz;
    protected int paddingVert;

    protected int mainFabCenterX;
    protected int mainFabCenterY;
    protected int baseBubbleCenterX;
    protected int baseBubbleCenterY;

    public BubbleMenu(Context context) {
        super(context);
        init();
    }

    public BubbleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isOpen) closeMenu();
    }

    private void init() {
        bubbleSize = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_size);
        paddingHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_horiz);
        paddingVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_vert);
        fabDistance = getResources().getDimensionPixelSize(R.dimen.bubble_menu_fab_distance);

        LayoutInflater.from(getContext()).inflate(R.layout.bubble_menu, this);
        mainFab = (Bubble) findViewById(R.id.main_fab);
        mainFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleOpenMenu();
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    toggleOpenMenu();
                }
            }
        });
    }

    private void toggleOpenMenu() {
        if (!isOpen) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    private void openMenu() {
        setBackgroundColor(getResources().getColor(R.color.bubble_menu_bg));
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white));
        isOpen = true;
        invalidate();
    }

    public void closeMenu() {
        setBackground(null);
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.logo_fab_icon));
        isOpen = false;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        componentWidthPx = ((View) getParent().getParent()).getWidth();
        componentHeightPx = ((View) getParent().getParent()).getHeight();

        mainFabCenterX = componentWidthPx - paddingHoriz - bubbleSize / 2;
        mainFabCenterY = componentHeightPx - paddingVert - bubbleSize / 2;

        baseBubbleCenterX = mainFabCenterX - fabDistance;
        baseBubbleCenterY = mainFabCenterY;

        if (isOpen) {
            setMeasuredDimension(componentWidthPx, componentHeightPx);
        } else {
            setMeasuredDimension(bubbleSize + paddingHoriz, bubbleSize + paddingVert);
        }
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

    public void addMenuItem(int icon, OnClickListener listener) {
        if (menuItems == null) {
            menuItems = new ArrayList<Bubble>();
        }
        Bubble item = new Bubble(getContext(), true, icon);
        menuItems.add(item);
        addView(item);
        item.setOnClickListener(listener);
    }
}
