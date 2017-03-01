package com.sabaibrowser.view;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sabaibrowser.UI;
import com.sabaibrowser.R;
import com.sabaibrowser.Tab;

import java.lang.ref.WeakReference;
import java.util.Set;

public class BlockedElementsDialog extends ViewGroup {

    WeakReference<UI> mUi;

    private int bubbleSize;
    private int paddingHoriz;
    private int paddingVert;
    private int closeFabPosX;
    private int closeFabPosY;
    private Set<String> mTrackers;
    private Bubble mCloseFab;
    private TextView mListTv;

    public BlockedElementsDialog(Context context) {
        super(context);
        init();
    }

    public BlockedElementsDialog(Context context, UI ui, Tab tab) {
        super(context);
        mUi = new WeakReference<UI>(ui);
        mTrackers = tab.getTrackers();
        init();
    }

    void init() {
        bubbleSize = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_size);
        paddingHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_horiz);
        paddingVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_vert);

        setBackgroundColor(getResources().getColor(R.color.light_gray));

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
            setElevation(24f);
        }

        mListTv = new TextView(getContext());
        addView(mListTv);
        if (mTrackers != null) {
            StringBuffer sb = new StringBuffer();
            for (String tracker : mTrackers) {
                sb.append(tracker);
                sb.append('\n');
            }
            mListTv.setText(sb.toString());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int componentWidthPx = ((View) getParent().getParent()).getWidth();
        int componentHeightPx = ((View) getParent().getParent()).getHeight();
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
            if (child == mListTv) {
                mListTv.layout(20, 20, getMeasuredWidth() - 40, getMeasuredHeight() - 40);
            }
        }
    }
}
