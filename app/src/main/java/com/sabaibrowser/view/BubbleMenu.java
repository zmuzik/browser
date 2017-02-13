package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sabaibrowser.R;

import java.util.ArrayList;

public class BubbleMenu extends ViewGroup {

    boolean isOpen = false;
    Bubble mainFab;

    ArrayList<Bubble> menuItems;

    int componentWidthPx;
    int componentHeightPx;
    int fabDistance;

    int bubbleSize;
    int paddingHoriz;
    int paddingVert;

    int mainFabCenterX;
    int mainFabCenterY;
    int baseBubbleCenterX;
    int baseBubbleCenterY;

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

    void init() {
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

    void toggleOpenMenu() {
        if (!isOpen) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    void openMenu() {
        setBackgroundColor(getResources().getColor(R.color.bubble_menu_bg));
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white));

        // init dimensions

        // init data
        addMenuItem(R.drawable.ic_refresh, new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "test", Toast.LENGTH_SHORT).show();
            }
        });
        addMenuItem(R.drawable.ic_home, null);
        addMenuItem(R.drawable.ic_bookmarks, null);
        addMenuItem(R.drawable.ic_back_hierarchy, null);
        addMenuItem(R.drawable.ic_settings, null);

        isOpen = true;
        invalidate();
    }

    void closeMenu() {
        setBackground(null);
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.logo_fab_icon));
        for (Bubble item : menuItems) {
            removeView(item);
        }
        menuItems = null;
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

        if (count == 0) {
            mainFab.layout(0, 0, bubbleSize, bubbleSize);
        } else {
            mainFab.layout(mainFabCenterX - bubbleSize / 2, mainFabCenterY - bubbleSize / 2,
                    mainFabCenterX + bubbleSize / 2, mainFabCenterY + bubbleSize / 2);

            double fi = 0d, fiStep = 0d;
            if (count == 1) {
                fi = Math.PI / 4;
            } else if (count == 2) {
                fi = Math.PI / 8;
                fiStep = Math.PI / 4;
            } else {
                fiStep = Math.PI / 2 / (count - 1);
            }

            int x = 0, y = 0;

            for (int i = 0; i < count; i++) {
                View child = menuItems.get(i);
                x = baseBubbleCenterX + (fabDistance - (int) (fabDistance * Math.cos(fi)));
                y = baseBubbleCenterY - (int) (fabDistance * Math.sin(fi));
                child.layout(x - bubbleSize / 2, y - bubbleSize / 2, x + bubbleSize / 2, y + bubbleSize / 2);
                fi += fiStep;
            }
        }
    }

    void addMenuItem(int icon, OnClickListener listener) {
        if (menuItems == null) {
            menuItems = new ArrayList<Bubble>();
        }
        Bubble item = new Bubble(getContext(), true, icon);
        menuItems.add(item);
        addView(item);
        item.setOnClickListener(listener);
    }
}
