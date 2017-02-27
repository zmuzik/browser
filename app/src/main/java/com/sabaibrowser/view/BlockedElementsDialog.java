package com.sabaibrowser.view;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.sabaibrowser.BaseUi;
import com.sabaibrowser.R;

import java.lang.ref.WeakReference;

public class BlockedElementsDialog extends ViewGroup {

    WeakReference<BaseUi> mUi;

    private int bubbleSize;
    private int paddingHoriz;
    private int paddingVert;
    private int closeFabPosX;
    private int closeFabPosY;

    private Bubble mCloseFab;

    public BlockedElementsDialog(Context context) {
        super(context);
        init();
    }

    public BlockedElementsDialog(Context context, BaseUi ui) {
        super(context);
        mUi = new WeakReference<BaseUi>(ui);
        init();
    }

    void init() {
        bubbleSize = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_size);
        paddingHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_horiz);
        paddingVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_vert);

        setBackgroundColor(getResources().getColor(R.color.white));

        mCloseFab = new Bubble(getContext());
        mCloseFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white));
        mCloseFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUi != null && mUi.get() != null) {
                    mUi.get().hideBlockedInfo();
                }
            }
        });
        addView(mCloseFab);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(12f);
            mCloseFab.setElevation(24f);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int componentWidthPx = ((View) getParent().getParent()).getWidth();
        int componentHeightPx = ((View) getParent().getParent()).getHeight() / 2;
        closeFabPosX = componentWidthPx - bubbleSize - paddingHoriz;
        closeFabPosY = componentHeightPx - bubbleSize - paddingVert;
        setMeasuredDimension(componentWidthPx, componentHeightPx);
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == mCloseFab) {
                mCloseFab.layout(closeFabPosX, closeFabPosY,
                        closeFabPosX + bubbleSize, closeFabPosY + bubbleSize);
            }
        }
    }
}
