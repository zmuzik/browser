package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
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
    protected int bubbleDistance;

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

            final double fiStep = Math.PI / 180;
            final double elipsisParam = 1.8d;
            final int bubbleDistance = (int) (1.25f * bubbleSize);

            int x = baseBubbleCenterX;
            int y = baseBubbleCenterY;
            int oldX = baseBubbleCenterX;
            int oldY = baseBubbleCenterY;

            double fi = 0d;

            for (int i = 0; i < count; i++) {
                // stop when we reach the peak of the curve, don't draw any more elements
                if (y > oldY) break;
                View child = menuItems.get(i);
                child.layout(x - bubbleSize / 2, y - bubbleSize / 2, x + bubbleSize / 2, y + bubbleSize / 2);
                oldX = x;
                oldY = y;
                do {
                    fi = fi + fiStep;
                    x = baseBubbleCenterX + (fabDistance - (int) (fabDistance * Math.cos(fi)));
                    y = baseBubbleCenterY - (int) (elipsisParam * fabDistance * Math.sin(fi));
                } while (distance(x, y, oldX, oldY) < bubbleDistance);
            }

        } else {
            mainFab.layout(0, 0, bubbleSize, bubbleSize);
        }
    }

    int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((double) x1 - (double) x2, 2)
                + Math.pow((double) y1 - (double) y2, 2));
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
