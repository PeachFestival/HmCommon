package com.hengmei.hm_common.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Created by 朱良浩 on 2016/11/25.
 */

public class FloatWindowManager {
    private static FloatBallView mBallView;

    private static WindowManager mWindowManager;


    public static void addBallView(Context context) {
        if (mBallView == null) {
            WindowManager windowManager = getWindowManager(context);
            int screenWidth = windowManager.getDefaultDisplay().getWidth();
            int screenHeight = windowManager.getDefaultDisplay().getHeight();
            System.out.println("screenWidth" + screenWidth);
            System.out.println("screenHeight" + screenHeight);
            mBallView = new FloatBallView(context);
            //            LayoutParams params = new LayoutParams();
            //            params.x = screenWidth;
            //            params.y = screenHeight / 2;
            //            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            //            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            //            params.gravity = Gravity.LEFT | Gravity.TOP;
            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //            } else {
            //                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            //            }
            //            params.format =  PixelFormat.TRANSLUCENT;
            //            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            //            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;


            final LayoutParams params = new LayoutParams();
            params.height = LayoutParams.WRAP_CONTENT;
            params.width = LayoutParams.MATCH_PARENT;
            params.format = PixelFormat.TRANSLUCENT;
            //        params.windowAnimations = com.android.internal.R.style.Animation_Toast;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = LayoutParams.TYPE_PHONE;
            }
            //        params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
            //        params.setTitle("Toast");
            params.flags = LayoutParams.FLAG_KEEP_SCREEN_ON | LayoutParams.FLAG_NOT_FOCUSABLE;
            params.gravity = Gravity.CENTER + Gravity.CENTER;
            //            mBallView.setLayoutParams(params);
            windowManager.addView(mBallView, params);
        }
    }

    public static void removeBallView(Context context) {
        if (mBallView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mBallView);
            mBallView = null;
        }
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

}
