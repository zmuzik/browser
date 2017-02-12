package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.sabaibrowser.R;

import java.util.ArrayList;

public class BubbleMenu extends FrameLayout {

    boolean isOpen = false;
    Bubble mainFab;

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
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white));
        BubbleScroller scroller = new BubbleScroller(getContext());
        addView(scroller);
        invalidate();
        isOpen = true;
    }

    void closeMenu() {
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.logo_fab_icon));
        removeViewAt(1);
        invalidate();
        isOpen = false;
    }

    private static class BubbleScroller extends ViewGroup {

        private static final String ID_REFRESH = "REFRESH";
        private static final String ID_SETTINGS = "SETTINGS";

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

        public BubbleScroller(Context context) {
            super(context);
            setBackgroundColor(getResources().getColor(R.color.bubble_menu_bg));

            // init dimensions
            bubbleSize = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_size);
            paddingHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_horiz);
            paddingVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_vert);
            fabDistance = getResources().getDimensionPixelSize(R.dimen.bubble_menu_fab_distance);

            // init data
            menuItems = new ArrayList<Bubble>();
            menuItems.add(new Bubble(getContext(), true, R.drawable.ic_refresh));
            menuItems.add(new Bubble(getContext(), true, R.drawable.ic_home));
            menuItems.add(new Bubble(getContext(), true, R.drawable.ic_bookmarks));
            menuItems.add(new Bubble(getContext(), true, R.drawable.ic_back_hierarchy));
            menuItems.add(new Bubble(getContext(), true, R.drawable.ic_settings));

            for (Bubble bubble : menuItems) {
                addView(bubble);
            }
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

            setMeasuredDimension(componentWidthPx, componentHeightPx);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int count = menuItems.size();

            // step to rotate;
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
                View child = getChildAt(i);
                x = baseBubbleCenterX + (fabDistance - (int) (fabDistance * Math.cos(fi)));
                y = baseBubbleCenterY - (int) (fabDistance * Math.sin(fi));
                child.layout(x - bubbleSize / 2, y - bubbleSize / 2, x + bubbleSize / 2, y + bubbleSize / 2);
                fi += fiStep;
            }
        }
    }
}
