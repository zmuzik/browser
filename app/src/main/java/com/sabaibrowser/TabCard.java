package com.sabaibrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
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
    private ImageView mCloseBtn;
    private NavScreen mNavScreen;
    private Tab mTab;

    public TabCard(Context context) {
        super(context);
        init();
    }

    public TabCard(Context context, NavScreen navScreen) {
        super(context);
        mNavScreen = navScreen;
        init();
    }

    private void init() {
        mPadding = (int) getResources().getDimension(R.dimen.tab_thumbnail_card_padding);
        mTitleHeight = (int) getResources().getDimension(R.dimen.tab_thumbnail_title_height);
        mThumbnailWidth = Tab.getTabCardThumbWidth(getContext());
        mThumbnailHeight = Tab.getTabCardThumbHeight(getContext());

        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundColor(getResources().getColor(R.color.light_gray));

        mTitle = new TextView(getContext());
        mTitle.setHeight(mTitleHeight);
        mTitle.setPadding(0, 0, 0, 0);
        mTitle.setIncludeFontPadding(false);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.tab_thumbnail_title_text_size));
        //XXX mTitle set width wrap content
        addView(mTitle);

        mThumbnail = new ImageView(getContext());
        mThumbnail.setMinimumWidth(mThumbnailWidth);
        mThumbnail.setMinimumHeight(mThumbnailHeight);
        addView(mThumbnail);

        mCloseBtn = new ImageView(getContext());
        mCloseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_window));
        mCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNavScreen != null) {
                    mNavScreen.closeTab(mTab);
                    mNavScreen.refreshAdapter();
                }
            }
        });
        addView(mCloseBtn);
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
                } else if (child == mCloseBtn) {
                    x = mThumbnailWidth + mPadding - Utils.dpToPx(getContext(), 24);
                    y = mThumbnailWidth + mPadding - Utils.dpToPx(getContext(), 24);
                }
            } else {
                if (child == mThumbnail) {
                    x = mPadding;
                    y = mPadding + mTitleHeight;
                } else if (child == mTitle) {
                    x = mPadding;
                    y = mPadding;
                } else if (child == mCloseBtn) {
                    x = mThumbnailWidth + mPadding - Utils.dpToPx(getContext(), 24);
                    y = 0;
                }
            }
            child.layout(x, y, x + child.getMeasuredWidth(), y + child.getMeasuredHeight());
        }
    }

    public void setTab(Tab tab) {
        if (tab == null) return;
        mTab = tab;
        mTitle.setText(tab.getTitleToDisplay());
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
