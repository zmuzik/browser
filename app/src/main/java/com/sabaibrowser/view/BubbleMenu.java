package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.sabaibrowser.R;

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

        int componentWidthPx;
        int componentHeightPx;

        public BubbleScroller(Context context) {
            super(context);
            setBackgroundColor(getResources().getColor(R.color.bubble_menu_bg));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            componentWidthPx = ((View) getParent().getParent()).getWidth();
            componentHeightPx = ((View) getParent().getParent()).getHeight();
            setMeasuredDimension(componentWidthPx, componentHeightPx);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                child.layout(0, 0, 0, 0);
            }
        }
    }
}
