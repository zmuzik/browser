package com.sabaibrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabCard extends ViewGroup {

    private TextView mTitle;
    private ImageView mThumbnail;
    private int mPadding;
    private int mThumbnailWidth;
    private int mThumbnailHeight;
    private int mTitleHeight;
    private boolean mTitleDown = false;

    public TabCard(Context context) {
        super(context);
        init();
    }

    public TabCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPadding = (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);
        mTitleHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_title_height);
        mThumbnailWidth = (int) getResources().getDimension(R.dimen.tab_thumbnail_width);
        mThumbnailHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_height);

        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundColor(getResources().getColor(R.color.light_gray));

        mTitle = new TextView(getContext());
        mTitle.setHeight(mTitleHeight);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.tab_thumbnail_title_text_size));
        //XXX mTitle set width wrap content
        addView(mTitle);

        mThumbnail = new ImageView(getContext());
        mThumbnail.setMinimumWidth(mThumbnailWidth);
        mThumbnail.setMinimumHeight(mThumbnailHeight);
        addView(mThumbnail);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }

        int paddingHoriz = 2 * mPadding;
        int paddingVert = 2 * mPadding;
        int width = mThumbnailWidth + paddingHoriz;
        int height = mThumbnailHeight + mTitleHeight + paddingVert;
        setMeasuredDimension(width | MeasureSpec.EXACTLY, height | MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int x = 0, y = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == View.GONE) continue;
            if (mTitleDown) {
                if (child == mThumbnail) {
                    x = mPadding;
                    y = mPadding;
                } else if (child == mTitle) {
                    x = mPadding;
                    y = mPadding + mThumbnailHeight;
                }
            } else {
                if (child == mThumbnail) {
                    x = mPadding;
                    y = mPadding + mTitleHeight;
                } else if (child == mTitle) {
                    x = mPadding;
                    y = mPadding;
                }
            }
            child.layout(x, y, x + child.getMeasuredWidth(), y + child.getMeasuredHeight());
        }
    }

    int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setTab(Tab tab) {
        if (tab == null) return;
        mTitle.setText((tab.getTitle() != null) ? tab.getTitle() : tab.getUrl());
        boolean isPrivate = tab.isPrivateBrowsingEnabled();

        int backgroundColor = (isPrivate)
                ? R.color.incognito_tab_card_bg
                : R.color.normal_tab_card_bg;
        setBackgroundColor(getResources().getColor(backgroundColor));

        int titleColor = (isPrivate)
                ? R.color.incognito_tab_card_title
                : R.color.normal_tab_card_title;

        if (mTitle != null) mTitle.setTextColor(getResources().getColor(titleColor));

        Bitmap image = tab.getScreenshot();
        if (image != null) {
            mThumbnail.setImageBitmap(image);
        }
    }

    public void setTitleDown(boolean down) {
        mTitleDown = down;
    }

    public boolean isTitleDown() {
        return mTitleDown;
    }
}
