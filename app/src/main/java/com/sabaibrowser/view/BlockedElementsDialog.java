package com.sabaibrowser.view;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sabaibrowser.R;
import com.sabaibrowser.Tab;
import com.sabaibrowser.UI;
import com.sabaibrowser.blocker.Tracker;

import java.lang.ref.WeakReference;
import java.util.List;

public class BlockedElementsDialog extends ViewGroup {

    WeakReference<UI> mUi;

    private int padding;
    private int bubbleSize;
    private int paddingHoriz;
    private int paddingVert;
    private int closeFabPosX;
    private int closeFabPosY;
    private List<Tracker> mTrackers;
    private Bubble mCloseFab;
    private RecyclerView mRecyclerView;

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
        padding = getResources().getDimensionPixelSize(R.dimen.standard_padding);

        setBackgroundColor(getResources().getColor(R.color.light_gray));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(24f);
        }

        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new TrackersAdapter());
        addView(mRecyclerView);

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
            if (child == mRecyclerView) {
                mRecyclerView.layout(padding, padding,
                        getMeasuredWidth() - padding, getMeasuredHeight() - padding);
            }
        }
    }

    private class TrackersAdapter extends RecyclerView.Adapter<TrackersAdapter.ViewHolder> {

        @Override
        public TrackersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tracker_list_item, parent, false);
            ViewHolder vh = new ViewHolder(root);
            return vh;
        }

        @Override
        public void onBindViewHolder(TrackersAdapter.ViewHolder holder, int position) {
            holder.mName.setText(mTrackers.get(position).name);
            holder.mCategory.setText(mTrackers.get(position).category);
        }

        @Override
        public int getItemCount() {
            return mTrackers.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mName;
            TextView mCategory;

            ViewHolder(View v) {
                super(v);
                mName = (TextView) v.findViewById(R.id.tracker_name);
                mCategory = (TextView) v.findViewById(R.id.tracker_category);
            }
        }
    }
}
