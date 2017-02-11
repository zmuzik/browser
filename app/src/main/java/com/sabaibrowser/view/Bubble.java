package com.sabaibrowser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sabaibrowser.R;

public class Bubble extends ImageView {

    private static final int DEFAULT_DIAMETER = 56;
    private static final int DEFAULT_BORDER_SIZE = 2;

    public Bubble(Context context) {
        super(context);
        init(false);
    }

    public Bubble(Context context, boolean solid, int drawableId) {
        super(context);
        init(solid);
        Drawable drawable = context.getResources().getDrawable(drawableId);
        setImageDrawable(drawable);
    }

    public Bubble(Context context, boolean solid, Drawable drawable) {
        super(context);
        init(solid);
        setImageDrawable(drawable);
    }

    public Bubble(Context context, boolean solid, Bitmap bitmap) {
        super(context);
        init(solid);
        setImageBitmap(bitmap);
    }

    public Bubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Bubble, 0, 0);
        boolean solid = array.getBoolean(R.styleable.Bubble_solid, false);
        init(solid);

        Drawable drawable = array.getDrawable(R.styleable.Bubble_icon);
        if (drawable != null) {
            setImageDrawable(drawable);
        }
    }

    void init(boolean solid) {
        setScaleType(ScaleType.CENTER);
        int bg = solid ? R.drawable.bubble_bg_solid : R.drawable.bubble_bg_transparent;
        setBackground(getResources().getDrawable(bg));
    }

}
