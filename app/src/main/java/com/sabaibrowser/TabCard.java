package com.sabaibrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabCard extends LinearLayout {

    TextView mTitle;
    ImageView mThumbnail;
    ImageView mIncognitoIndicator;

    public TabCard(Context context) {
        super(context);
        init();
    }

    public TabCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = (int) dpToPx(96);
        int width = (int) dpToPx(160);
        setMeasuredDimension(width | MeasureSpec.EXACTLY, height | MeasureSpec.EXACTLY);
    }

    float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.tab_card, this);
        setBackgroundColor(getResources().getColor(R.color.light_gray));
        mTitle = (TextView) findViewById(R.id.tab_title);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail);
        mIncognitoIndicator = (ImageView) findViewById(R.id.incognito_indicator);
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
