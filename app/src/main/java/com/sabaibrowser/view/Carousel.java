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
    int mTabCardThumbWidth;
    int mTabCardThumbHeight;
    int mTabCardTitleHeight;
    int mTabCardPadding;
    int mStep;

    int mSelectedPos;

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
        mTabCardThumbWidth = (int) getResources().getDimension(R.dimen.tab_thumbnail_width);
        mTabCardThumbHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_height);
        mTabCardTitleHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_title_height);
        mTabCardPadding = (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);
        mStep = (int) dpToPx(64);
    }

    /**
     * return position of the upper left corner of the tab card
     * selected shoule be between 0 and count
     */
    ScreenPosition getPosition(int position, int count, double selected) {
        selected = Math.min(selected, count);
        selected = Math.max(selected, 0);

        // values to be added to get the coords of the upper left corner from the center coords
        int cornerOffsetX = -mTabCardThumbWidth / 2 - mTabCardPadding;
        int cornerOffsetY = -mTabCardThumbHeight / 2 - mTabCardPadding - mTabCardTitleHeight;

        // default position of the selected item
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;

        int x = centerX;
        int y = centerY + mStep * (position - (int)selected);
        return new ScreenPosition(x + cornerOffsetX, y + cornerOffsetY, 0);
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
            ScreenPosition coords = getPosition(i, count, getSelectedPosition());
            child.layout(coords.x,
                    coords.y,
                    coords.x + child.getMeasuredWidth(),
                    coords.y + child.getMeasuredHeight());
        }
        if (front != null) front.bringToFront();
    }

    public void setSelectedItem(int position) {
        mSelectedPos = position;
    }

    public void setSelectedItem(View v) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == v && child != null) {
                mSelectedPos = i;
            }
        }
    }

    public double getSelectedPosition() {
        return mSelectedPos;
    }

    public View getSelectedItem() {
        try {
            return getChildAt(mSelectedPos);
        } catch (Exception e) {
            return null;
        }
    }

    private static class ScreenPosition {
        public int x, y;
        public double fi;

        public ScreenPosition(int x, int y, double fi) {
            this.x = x;
            this.y = y;
            this.fi = fi;
        }
    }
}
