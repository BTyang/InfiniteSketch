package com.btyang.infinitesketch.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 *  屏幕工具
 */
public class ScreenUtils {

    public static Point getScreenSize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point out = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(out);
        }else{
            int width = display.getWidth();
            int height = display.getHeight();
            out.set(width, height);
        }
        return out;
    }
    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            //Log.x call replaced
            e1.printStackTrace();
        }
        int statusBarHeight = sbar;
        //Log.x call replaced
        return statusBarHeight;
    }

    /**
     * 返回当前屏幕是否为竖屏。
     *
     * @return 当且仅当当前屏幕为竖屏时返回false, 否则返回true。
     */
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }


    public static void hideInput(View v) {
        InputMethodManager inputManager = (InputMethodManager) v
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //调用系统输入法
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }
    public static void showInput(View v) {
        InputMethodManager inputManager = (InputMethodManager) v
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //调用系统输入法
        inputManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    public static void toggleInput(Context context) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
