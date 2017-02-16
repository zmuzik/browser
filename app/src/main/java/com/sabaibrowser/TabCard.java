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

    TextView mTitle;
    ImageView mThumbnail;
    ImageView mIncognitoIndicator;
    int mPadding;
    int mThumbnailWidth;
    int mThumbnailHeight;
    int mTitleHeight;

    public TabCard(Context context) {
        super(context);
        init();
    }

    public TabCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(getContext()).inflate(R.layout.tab_card, this);
        setBackgroundColor(getResources().getColor(R.color.light_gray));

        mPadding = (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);
        mTitleHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_title_height);
        mThumbnailWidth = (int) getResources().getDimension(R.dimen.tab_thumbnail_width);
        mThumbnailHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_height);

        mTitle = (TextView) findViewById(R.id.tab_title);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail);
        mIncognitoIndicator = (ImageView) findViewById(R.id.incognito_indicator);
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
            if (child == mThumbnail) {
                x = mPadding;
                y = mPadding;
            } else if (child == mTitle) {
                x = mPadding;
                y = mPadding + mThumbnailHeight;
            } else if (child == mIncognitoIndicator) {
                x = mPadding + mThumbnailWidth - mTitleHeight;
                y = mPadding + mThumbnailHeight;
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
        mIncognitoIndicator.setVisibility(tab.isPrivateBrowsingEnabled() ? View.VISIBLE : View.GONE);
        Bitmap image = tab.getScreenshot();
        if (image != null) {
            mThumbnail.setImageBitmap(image);
        }
    }
}
