package com.sabaibrowser;

import android.content.Context;
import android.util.TypedValue;

public class Utils {

    public static int dpToPx(Context ctx, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                ctx.getResources().getDisplayMetrics());
    }
}
