package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.sabaibrowser.R;
import com.sabaibrowser.Tab;
import com.sabaibrowser.TabCard;
import com.sabaibrowser.Utils;

public class RollingTabSwitcher extends ViewGroup implements View.OnTouchListener {

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
    boolean isPortrait = true;

    public RollingTabSwitcher(Context context) {
        super(context);
        init();
    }

    public RollingTabSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        int orientation = getResources().getConfiguration().orientation;
        isPortrait = (orientation != Configuration.ORIENTATION_LANDSCAPE);

        mPadding = (int) getResources().getDimension(R.dimen.tab_carousel_padding);
        mTabCardSize = Tab.getTabCardThumbWidth(getContext())
                + 2 * (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();

        mStep = Utils.dpToPx(getContext(), 64);
        setChildrenDrawingOrderEnabled(true);
        setOnTouchListener(this);
    }

    Placement getArcPosition(double percentage) {
        percentage = Math.max(-1d, percentage);
        percentage = Math.min(percentage, 1d);

        final int RADIUS_X = getMeasuredWidth() / 2 - mPadding - mTabCardSize / 2;
        final int RADIUS_Y = getMeasuredHeight() / 2 - mPadding - mTabCardSize / 2;
        final int CENTER_X = getMeasuredWidth() / 2;
        final int CENTER_Y = getMeasuredHeight() / 2;
        final double FI_MINUS_ONE = 0d;
        final double FI_ZERO = Math.PI / 2;
        final double FI_PLUS_ONE = Math.PI;

        int x = 0;
        int y = 0;
        double fi = FI_ZERO;

        if (percentage > 0) {
            fi = FI_ZERO + percentage * (FI_PLUS_ONE - FI_ZERO);
        } else if (percentage < 0) {
            fi = FI_ZERO + percentage * (FI_ZERO - FI_MINUS_ONE);
        }

        if (isPortrait) {
            x = CENTER_X;
            y = CENTER_Y - (int) (Math.cos(fi) * RADIUS_Y);
        } else {
            x = CENTER_X - (int) (Math.cos(fi) * RADIUS_X);
            y = CENTER_Y;
        }

        return new Placement(x, y);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int cornerOffset = -mTabCardSize / 2;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            // correct placement of title
            ((TabCard) child).setTitleDown(i > getFrontPosition());

            // position
            Placement coords = getScreenPosition(i, count, getSelectedPosition());
            //int perspective = (int) (Math.abs(coords.y + mTabCardSize / 2 - (getMeasuredHeight() / 2)) * .2d);
            int perspective = 0;
            child.layout(coords.x + cornerOffset + perspective,
                    coords.y + cornerOffset + perspective,
                    coords.x + cornerOffset - perspective + child.getMeasuredWidth(),
                    coords.y + cornerOffset - perspective + child.getMeasuredHeight());
        }
    }

    /**
     * return position of the center of the tab card
     * selected shoule be between 0 and count
     */
    Placement getScreenPosition(int position, int count, double selected) {
        selected = Math.min(selected, count);
        selected = Math.max(selected, 0);

        double arc = 1d / count * (position - selected) + 1d / count * (((double) getTotalScrollFactor()) / mStep);

        Placement center = getArcPosition(arc);
        return new Placement(center.x, center.y);
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

    public void setSelectedPosition(int position) {
        mSelectedPos = position;
        scrollFactor = 0;
    }

    public int getSelectedPosition() {
        return mSelectedPos;
    }

    public int getFrontPosition() {
        int result = mSelectedPos;
        result -= getTotalScrollFactor() / mStep;
        result = Math.min(result, getChildCount());
        result = Math.max(result, 0);

        return result;
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
            case MotionEvent.ACTION_MOVE:
                if (isPortrait) {
                    gestureScrollFactor = (int) (me.getY() - gestureStartY);
                } else {
                    gestureScrollFactor = (int) (me.getX() - gestureStartX);
                }
                requestLayout();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                scrollFactor = getTotalScrollFactor();
                gestureScrollFactor = 0;
                break;
        }
        return true;
    }

    int getTotalScrollFactor() {
        return scrollFactor + gestureScrollFactor;
    }
}
