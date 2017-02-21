package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sabaibrowser.R;
import com.sabaibrowser.Utils;

import java.util.ArrayList;

public class BubbleMenu extends ViewGroup {

    protected boolean isOpen = false;
    protected Bubble mainFab;

    protected ArrayList<Bubble> menuItems;

    protected int componentWidthPx;
    protected int componentHeightPx;
    protected int fabDistance;
    protected int bubbleDistance;

    protected int bubbleSize;
    protected int paddingHoriz;
    protected int paddingVert;

    protected double fiStep;
    protected double elipsisParam;

    protected int mainFabCenterX;
    protected int mainFabCenterY;
    protected int baseBubbleCenterX;
    protected int baseBubbleCenterY;
    private ImageView upperArrow;
    private ImageView lowerArrow;
    private boolean upperArrowVisible = false;
    private boolean lowerArrowVisible = false;


    public BubbleMenu(Context context) {
        super(context);
        init();
    }

    public BubbleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isOpen) closeMenu();
    }

    private void init() {
        bubbleSize = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_size);
        paddingHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_horiz);
        paddingVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_vert);
        fabDistance = getResources().getDimensionPixelSize(R.dimen.bubble_menu_fab_distance);

        fiStep = Math.PI / 180;
        elipsisParam = 1.8d;
        bubbleDistance = (int) (1.24f * bubbleSize);

        LayoutInflater.from(getContext()).inflate(R.layout.bubble_menu, this);
        mainFab = (Bubble) findViewById(R.id.main_fab);
        mainFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleOpenMenu();
            }
        });

        upperArrow = new ImageView(getContext());
        upperArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right));
        addView(upperArrow);

        lowerArrow = new ImageView(getContext());
        lowerArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
        addView(lowerArrow);

//        setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (isOpen) {
//                    toggleOpenMenu();
//                }
//            }
//        });
    }

    private void toggleOpenMenu() {
        if (!isOpen) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    private void openMenu() {
        setBackgroundColor(getResources().getColor(R.color.bubble_menu_bg));
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white));
        isOpen = true;
        invalidate();
    }

    public void closeMenu() {
        setBackground(null);
        mainFab.setImageDrawable(getResources().getDrawable(R.drawable.logo_fab_icon));
        isOpen = false;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        componentWidthPx = ((View) getParent().getParent()).getWidth();
        componentHeightPx = ((View) getParent().getParent()).getHeight();

        mainFabCenterX = componentWidthPx - paddingHoriz - bubbleSize / 2;
        mainFabCenterY = componentHeightPx - paddingVert - bubbleSize / 2;

        baseBubbleCenterX = mainFabCenterX - fabDistance;
        baseBubbleCenterY = mainFabCenterY;

        if (isOpen) {
            setMeasuredDimension(componentWidthPx, componentHeightPx);
        } else {
            setMeasuredDimension(bubbleSize + paddingHoriz, bubbleSize + paddingVert);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = (menuItems == null) ? 0 : menuItems.size();

        if (isOpen) {
            // main bubble
            mainFab.layout(mainFabCenterX - bubbleSize / 2, mainFabCenterY - bubbleSize / 2,
                    mainFabCenterX + bubbleSize / 2, mainFabCenterY + bubbleSize / 2);

            ScreenPosition oldCoords = new ScreenPosition(baseBubbleCenterX, baseBubbleCenterY);
            ScreenPosition coords = new ScreenPosition(baseBubbleCenterX, baseBubbleCenterY);

            double fi = 0d;

            for (int i = 0; i < count; i++) {
                // stop when we reach the peak of the curve, don't draw any more elements, show the arrow
                if (coords.y > mainFabCenterY) {
                    lowerArrowVisible= true;
                    break;
                }
                if (coords.x > mainFabCenterX) {
                    upperArrowVisible = true;
                    break;
                }
                View child = menuItems.get(i);
                child.layout(coords.x - bubbleSize / 2, coords.y - bubbleSize / 2,
                        coords.x + bubbleSize / 2, coords.y + bubbleSize / 2);
                oldCoords = coords;
                do {
                    fi = fi + fiStep;
                    coords = getArcPosition(fi);
                } while (distance(coords.x, coords.y, oldCoords.x, oldCoords.y) < bubbleDistance);
            }

            placeArrows();
        } else {
            mainFab.layout(0, 0, bubbleSize, bubbleSize);
        }
    }

    ScreenPosition getArcPosition(double fi) {
        int x = baseBubbleCenterX + (fabDistance - (int) (fabDistance * Math.cos(fi)));
        int y = baseBubbleCenterY - (int) (elipsisParam * fabDistance * Math.sin(fi));
        return new ScreenPosition(x, y);
    }

    void placeArrows() {
        if (upperArrowVisible) {
            int size = Utils.dpToPx(getContext(), 24);
            int arrowX = mainFabCenterX + bubbleSize / 2 + size / 2;
            int arrowY = baseBubbleCenterY - (int) (elipsisParam * fabDistance);
            upperArrow.layout(arrowX - size / 2,
                    arrowY - size / 2,
                    arrowX + size / 2,
                    arrowY + size / 2);
        }

        if (lowerArrowVisible) {
            int size = Utils.dpToPx(getContext(), 24);
            int arrowX = baseBubbleCenterX;
            int arrowY = baseBubbleCenterY + bubbleSize / 2 + size / 2;
            lowerArrow.layout(arrowX - size / 2,
                    arrowY - size / 2,
                    arrowX + size / 2,
                    arrowY + size / 2);
        }
    }

    int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((double) x1 - (double) x2, 2)
                + Math.pow((double) y1 - (double) y2, 2));
    }

    public void addMenuItem(int icon, OnClickListener listener) {
        if (menuItems == null) {
            menuItems = new ArrayList<Bubble>();
        }
        Bubble item = new Bubble(getContext(), true, icon);
        menuItems.add(item);
        addView(item);
        item.setOnClickListener(listener);
    }
}
