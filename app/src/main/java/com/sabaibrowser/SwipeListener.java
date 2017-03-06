package com.sabaibrowser;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeListener implements View.OnTouchListener {

    private final GestureDetector mDetector;

    public SwipeListener(Context ctx) {
        mDetector = new GestureDetector(ctx, new SwipeGestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int DIST_THRESHOLD = 32;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                // too "small" gesture, ignore, don't do anything else;
                if (Math.abs(diffX) < DIST_THRESHOLD && Math.abs(diffY) < DIST_THRESHOLD) {
                    return true;
                }

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (diffX > 0) {
                        onSwipeRight(velocityX);
                    } else {
                        onSwipeLeft(velocityX);
                    }
                } else {
                    if (diffY > 0) {
                        onSwipeDown(velocityY);
                    } else {
                        onSwipeUp(velocityY);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
    }

    public void onSwipeRight(float velocity) {
    }

    public void onSwipeLeft(float velocity) {
    }

    public void onSwipeUp(float velocity) {
    }

    public void onSwipeDown(float velocity) {
    }
}