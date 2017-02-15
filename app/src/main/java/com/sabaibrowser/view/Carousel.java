package com.sabaibrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class Carousel extends ViewGroup {

    protected int childWidth;
    protected int childHeight;

    public Carousel(Context context) {
        super(context);
        init();
    }

    public Carousel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        childWidth = (int) dpToPx(160);
        childHeight = (int) dpToPx(96);
    }

    public float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null) continue;
            child.layout(100 + i * 50,
                    100 + i * 50,
                    100 + i * 50 + child.getMeasuredWidth(),
                    100 + i * 50 + child.getMeasuredHeight());
        }
    }
}
