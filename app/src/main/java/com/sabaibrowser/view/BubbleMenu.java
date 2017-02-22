package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sabaibrowser.R;
import com.sabaibrowser.Utils;

import java.util.ArrayList;

public class BubbleMenu extends ViewGroup implements View.OnTouchListener {

    protected boolean isOpen = false;
    protected Bubble mainFab;

    protected ArrayList<Bubble> menuItems;

    protected int componentWidthPx;
    protected int componentHeightPx;
    protected int fabDistanceHoriz;
    protected int fabDistanceVert;

    protected int bubbleSize;
    protected int paddingHoriz;
    protected int paddingVert;

    protected int mainFabCenterX;
    protected int mainFabCenterY;
    protected int baseBubbleCenterX;
    protected int baseBubbleCenterY;
    private ImageView upperArrow;
    private ImageView lowerArrow;
    private boolean upperArrowVisible = false;
    private boolean lowerArrowVisible = false;
    private int gestureStartX;
    private int gestureStartY;
    private boolean mIsScrolling;
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private int gestureScrollFactor;
    private int scrollFactor;

    // elipsis and its approximation's parameters, mathematical explanation at
    // http://www.had2know.com/makeit/ellipse-approximation-normal-circular-arc.html
    double a, b, r1, r2, c1x, c1y, c2x, c2y, th1, th2, ea1, ea2;

    double bubbleDistance = 30d;
    double fadeLength = 50d;


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
        // init params defined as dimensions
        bubbleSize = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_size);
        bubbleDistance = getResources().getDimensionPixelSize(R.dimen.bubble_menu_bubble_distance);
        paddingHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_horiz);
        paddingVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_padding_vert);
        fabDistanceHoriz = getResources().getDimensionPixelSize(R.dimen.bubble_menu_fab_distance_horiz);
        fabDistanceVert = getResources().getDimensionPixelSize(R.dimen.bubble_menu_fab_distance_vert);
        fadeLength = getResources().getDimensionPixelSize(R.dimen.bubble_menu_fade_length);

        // elipsis approximation parameters initialization
        a = fabDistanceHoriz;
        b = fabDistanceVert;
        r1 = ((Math.pow(a, 2) + Math.pow(b, 2) - (a - b) * Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2))) / (2 * a));
        r2 = ((Math.pow(a, 2) + Math.pow(b, 2) + (a - b) * Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2))) / (2 * b));
        th2 = Math.atan(b / a);
        th1 = Math.atan(a / b);
        ea1 = getEquidistantAngle(r1, bubbleDistance);
        ea2 = getEquidistantAngle(r2, bubbleDistance);
        c2x = 0;
        c2y = b - r2;
        c1x = a - r1;
        c1y = 0;

        // view configuration parameters initialization
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();

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

        setOnTouchListener(this);
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

        baseBubbleCenterX = mainFabCenterX - fabDistanceHoriz;
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
            for (int i = 0; i < menuItems.size(); i++) {
                Placement placement = transformPlacement(getPlacement(i * bubbleDistance - getTotalScrollFactor()));
                if (placement == null) continue;
                placeView(menuItems.get(i), placement.x, placement.y, bubbleSize);
            }

        } else {
            mainFab.layout(0, 0, bubbleSize, bubbleSize);
        }
    }

    Placement transformPlacement(Placement original) {
        if (original == null) return null;
        int x = mainFabCenterX - original.x;
        int y = mainFabCenterY - original.y;
        float opacity = original.opacity;
        return new Placement(x, y, opacity);
    }

    Placement getPlacement(double distance) {
        // circumferences of the two circle parts
        int c1Length = (int) (th1 * r1);
        int c2Length = (int) (th2 * r2);
        distance = (c1Length + c2Length) - distance;

        if (distance < 0) {
            if (distance > -fadeLength) {
                float opacity = (float) ((fadeLength + distance) / -fadeLength);
                return new Placement((int) (c2x + distance), (int) b, opacity);
            } else {
                return null;
            }
        }

        if (distance > (c1Length + c2Length)) {
            if (distance < (c1Length + c2Length) + fadeLength) {
                float opacity = (float) ((distance - (c1Length + c2Length)) / -fadeLength);
                return new Placement((int) a, (int) (c1y - (distance - (c1Length + c2Length))), opacity);
            } else {
                return null;
            }
        }

        int x = 0;
        int y = 0;
        if (distance <= c2Length) {
            double fi = distance / c2Length * th2;
            x = (int) (c2x + r2 * Math.sin(fi));
            y = (int) (c2y + r2 * Math.cos(fi));
        } else {
            distance = distance - c2Length;
            double fi = th2 + distance / c1Length * th1;
            x = (int) (c1x + r1 * Math.sin(fi));
            y = (int) (c1y + r1 * Math.cos(fi));
        }
        return new Placement(x, y);
    }

    void placeArrows() {
        if (!upperArrowVisible && !lowerArrowVisible) return;
        int size = Utils.dpToPx(getContext(), 24);
        if (upperArrowVisible) {
            int arrowX = mainFabCenterX + bubbleSize / 2 + size / 2;
            int arrowY = baseBubbleCenterY - fabDistanceVert;
            placeView(upperArrow, arrowX, arrowY, size);
        }
        if (lowerArrowVisible) {
            int arrowX = baseBubbleCenterX;
            int arrowY = baseBubbleCenterY + bubbleSize / 2 + size / 2;
            placeView(lowerArrow, arrowX, arrowY, size);
        }
    }

    void placeView(View view, int centerX, int centerY, int width, int height) {
        view.layout(centerX - width / 2,
                centerY - height / 2,
                centerX + width / 2,
                centerY + height / 2);
    }

    void placeView(View view, int centerX, int centerY, int size) {
        placeView(view, centerX, centerY, size, size);
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent me) {
        switch (me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gestureStartX = (int) me.getX();
                gestureStartY = (int) me.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsScrolling) return true;
                if (Math.abs(gestureStartY - me.getY()) > mSlop) {
                    mIsScrolling = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsScrolling = false;
                return false;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        switch (me.getAction()) {
            case MotionEvent.ACTION_MOVE:
                gestureScrollFactor = (int) (me.getY() - gestureStartY);
                requestLayout();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                scrollFactor = getTotalScrollFactor();
                gestureScrollFactor = 0;
                break;
        }
        return true;
    }

    int getTotalScrollFactor() {
        return scrollFactor + gestureScrollFactor;
    }

    double getEquidistantAngle(double r, double distance) {
        return Math.acos((2 * Math.pow(r, 2) - Math.pow(distance, 2)) / 2 * Math.pow(r, 2));
    }
}
