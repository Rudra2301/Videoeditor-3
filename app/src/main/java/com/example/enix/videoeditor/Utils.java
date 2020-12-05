package com.example.enix.videoeditor;

import android.content.res.Resources;

public class Utils {
    public static int dpToPx(int dp) {
        return (int) (((float) dp) * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
