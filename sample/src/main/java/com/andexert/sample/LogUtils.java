package com.andexert.sample;

import android.util.Log;

/**
 * @author WeiDeng
 * @date 16/5/25
 * @description
 */
public class LogUtils {

    public static final String TAG = "LogUtils";
    private static boolean isDebug = true;

    public static void d(String message) {
        d(TAG, message);
    }

    public static void d(String tag, String message) {
        if(isDebug) {
            Log.d(tag, message);
        }
    }
}
