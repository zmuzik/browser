package com.sabaibrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.sabaibrowser.R;

public class Carousel extends ViewGroup {

    final int SIDE_PADDING = 8;
    final int ARC_CENTER_Y = 160 + 8;
    final int ARC_RADIUS = 160;

    int mSidePadding;
    int mArcCenterY;
    int mArcRadius;
    int mTabCardWidth;
    int mTabCardPadding;
    int mTabCardHeight;

    public Carousel(Context context) {
        super(context);
        init();
    }

    public Carousel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        mSidePadding = dpToPx(SIDE_PADDING);
        mArcCenterY = dpToPx(ARC_CENTER_Y);
        mArcRadius = dpToPx(ARC_RADIUS);
        mTabCardWidth = (int) getResources().getDimension(R.dimen.tab_thumbnail_width);
        mTabCardHeight= (int) getResources().getDimension(R.dimen.tab_thumbnail_height);
        mTabCardPadding = (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);
    }

    //Position getPositionOnArc(int percentage) { sequence number of element, count,  scroll factor -1..1

    Position getPositionOnArc(int percentage) {
        int x = (getMeasuredWidth() - mTabCardWidth) / 2 - mTabCardPadding;
        int y = (getMeasuredHeight() - mTabCardHeight) / 2 - mTabCardPadding + percentage * 20;
        return new Position(x, y, percentage / 100f);
    }

    public int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("Carousel", "w: " + getMeasuredWidth() + " h: " + getMeasuredHeight());
        final int count = getChildCount();
        View front = null;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null) continue;
            if (i == 3) {
                front = child;
            }
            Position coords = getPositionOnArc(10 * i);
            child.layout(coords.x,
                    coords.y,
                    coords.x + child.getMeasuredWidth(),
                    coords.y + child.getMeasuredHeight());
        }
        if (front != null) front.bringToFront();
    }

    private static class Position {
        public int x,y;
        public double fi;
        public Position(int x, int y, double fi) {
            this.x = x;
            this.y = y;
            this.fi = fi;
        }
    }
}
