package com.sabaibrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.sabaibrowser.TabCard;

public class RollingTabSwitcher extends TabSwitcher implements View.OnTouchListener {

    public RollingTabSwitcher(Context context) {
        super(context);
    }

    public RollingTabSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    ScreenPosition getArcPosition(double percentage) {
        percentage = Math.max(-1d, percentage);
        percentage = Math.min(percentage, 1d);

        //final int RADIUS_X = getMeasuredWidth() - 2 * mPadding - mTabCardSize;
        final int RADIUS_Y = getMeasuredHeight() / 2 - mPadding - mTabCardSize / 2;
        final int CENTER_X = getMeasuredWidth() / 2;
        final int CENTER_Y = getMeasuredHeight() / 2;
        final double FI_MINUS_ONE = 0d;
        final double FI_ZERO = Math.PI / 2;
        final double FI_PLUS_ONE = Math.PI;
        final double ratio = (double) (getMeasuredHeight())
                / (getMeasuredWidth());

        double fi = FI_ZERO;
        if (percentage > 0) {
            fi = FI_ZERO + percentage * (FI_PLUS_ONE - FI_ZERO);
        } else if (percentage < 0) {
            fi = FI_ZERO + percentage * (FI_ZERO - FI_MINUS_ONE);
        }
        int x = CENTER_X;
        int y = CENTER_Y - (int) (Math.cos(fi) * RADIUS_Y);

        return new ScreenPosition(x, y);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);

            // correct placement of title
            ((TabCard) child).setTitleDown(i > getFrontPosition());

            // position
            ScreenPosition coords = getScreenPosition(i, count, getSelectedPosition());
            int perspective = (int) (Math.abs(coords.y + mTabCardSize / 2 - (getMeasuredHeight() / 2)) * .2d);
            child.layout(coords.x + perspective,
                    coords.y + perspective,
                    coords.x - perspective + child.getMeasuredWidth(),
                    coords.y - perspective + child.getMeasuredHeight());
        }
    }
}
