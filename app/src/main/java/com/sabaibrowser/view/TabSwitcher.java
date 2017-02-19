package com.sabaibrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.sabaibrowser.R;
import com.sabaibrowser.Tab;
import com.sabaibrowser.TabCard;

public abstract class TabSwitcher extends ViewGroup implements View.OnTouchListener {

    int mTabCardSize;

    double mStep;
    int scrollFactor;
    int gestureScrollFactor;

    int mSelectedPos;
    float gestureStartX;
    float gestureStartY;
    boolean mIsScrolling;
    int mSlop;
    int mMinFlingVelocity;
    int mMaxFlingVelocity;
    int mPadding;

    public TabSwitcher(Context context) {
        super(context);
        init();
    }

    public TabSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        mPadding = (int) getResources().getDimension(R.dimen.tab_carousel_padding);
        mTabCardSize = Tab.getTabCardThumbWidth(getContext())
                + 2 * (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();

        mStep = dpToPx(64);
        setChildrenDrawingOrderEnabled(true);
        setOnTouchListener(this);
    }

    /**
     * return position of the upper left corner of the tab card
     * selected shoule be between 0 and count
     */
    ScreenPosition getScreenPosition(int position, int count, double selected) {
        selected = Math.min(selected, count);
        selected = Math.max(selected, 0);

        // values to be added to get the coords of the upper left corner from the center coords
        int cornerOffset = -mTabCardSize / 2;

        double arc = 1d / count * (position - selected) + 1d / count * (((double) scrollFactor + gestureScrollFactor) / mStep);

        ScreenPosition center = getArcPosition(arc);
        return new ScreenPosition(center.x + cornerOffset, center.y + cornerOffset);
    }

    /**
     * @param percentage -1...1; 0 representing the selected element
     * @return position of center of the tab on the arc
     */
    ScreenPosition getArcPosition(double percentage) {
        percentage = Math.max(-1d, percentage);
        percentage = Math.min(percentage, 1d);

        final int RADIUS_X = getMeasuredWidth() - 2 * mPadding - mTabCardSize;
        final int RADIUS_Y = getMeasuredHeight() / 2 - mPadding - mTabCardSize / 2;
        final int CENTER_X = mPadding + mTabCardSize / 2;
        final int CENTER_Y = getMeasuredHeight() / 2;
        final double FI_MINUS_ONE = 0d;
        final double FI_ZERO = Math.PI * 3 / 4;
        final double FI_PLUS_ONE = Math.PI;
        final double ratio = (double) (getMeasuredHeight())
                / (getMeasuredWidth());

        double fi = FI_ZERO;
        if (percentage > 0) {
            fi = FI_ZERO + percentage * (FI_PLUS_ONE - FI_ZERO);
        } else if (percentage < 0) {
            fi = FI_ZERO + percentage * (FI_ZERO - FI_MINUS_ONE);
        }
        int x = CENTER_X + (int) (Math.sin(fi) * RADIUS_X);
        int y = CENTER_Y - (int) (Math.cos(fi) * RADIUS_Y);

        return new ScreenPosition(x, y);
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
    protected int getChildDrawingOrder(int count, int i) {
        // ensure proper visibility of tabs according to the perspective
        int frontPosition = getFrontPosition();
        int result = count - 1; // last element for the active tab;
        if (i < frontPosition) {
            result = i;
        } else if (i > frontPosition) {
            result = count - 1 + frontPosition - i;
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("Carousel", "w: " + getMeasuredWidth() + " h: " + getMeasuredHeight());
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            // correct placement of title
            ((TabCard) child).setTitleDown(i > getFrontPosition());

            // position
            ScreenPosition coords = getScreenPosition(i, count, getSelectedPosition());
            child.layout(coords.x,
                    coords.y,
                    coords.x + child.getMeasuredWidth(),
                    coords.y + child.getMeasuredHeight());
        }
    }

    public void setSelectedPosition(int position) {
        mSelectedPos = position;
        scrollFactor = 0;
    }

    public int getSelectedPosition() {
        return mSelectedPos;
    }

    public int getFrontPosition() {
        int result = mSelectedPos;
        result -= (scrollFactor + gestureScrollFactor) / mStep;
        result = Math.min(result, getChildCount());
        result = Math.max(result, 0);

        return result;
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

    public View getSelectedItem() {
        try {
            return getChildAt(mSelectedPos);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent me) {
        switch (me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gestureStartX = (int) me.getX();
                gestureStartY = (int) me.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsScrolling) return true;
                if (Math.abs(gestureStartY - me.getY()) > mSlop) {
                    mIsScrolling = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsScrolling = false;
                return false;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        switch (me.getAction()) {
            //handled in onInterceptTouchEvent
//            case MotionEvent.ACTION_DOWN:
//                gestureStartX = (int) me.getX();
//                gestureStartY = (int) me.getY();
//                break;
            case MotionEvent.ACTION_MOVE:
                gestureScrollFactor = (int) (me.getY() - gestureStartY);
                requestLayout();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                scrollFactor = scrollFactor + gestureScrollFactor;
                gestureScrollFactor = 0;
                break;
        }
        return true;
    }

    class ScreenPosition {
        int x, y;

        ScreenPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}