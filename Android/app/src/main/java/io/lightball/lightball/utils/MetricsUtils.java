package io.lightball.lightball.utils;

import android.content.Context;

// http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
public class MetricsUtils {

    public static float convertPixelsToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float convertDpToPixel(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
