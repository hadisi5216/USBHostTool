package com.hadisi.usbhosttool.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by wugang on 2015/11/13.
 */
public class Utils {
    /**
     * 显示toast
     *
     * @param context
     * @param text
     */
    private static Toast mToast;
    public static void showToast(Context context, String text) {
        if (context != null && text != null) {
            try {
                if (mToast == null) {
                    mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(text);
                    mToast.setDuration(Toast.LENGTH_SHORT);
                }
                mToast.show();
            } catch (NullPointerException e) {
            }
        }
    }
}
