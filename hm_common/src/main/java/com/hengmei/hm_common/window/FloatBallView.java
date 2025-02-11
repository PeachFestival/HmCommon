package com.hengmei.hm_common.window;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.hengmei.hm_common.R;

import java.lang.reflect.Field;

/**
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallView extends LinearLayout {
	private TextView mImgBg;
	private TextView bt_wifi;

	private WindowManager mWindowManager;

	private WindowManager.LayoutParams mLayoutParams;

	private long  mLastDownTime;
	private float mLastDownX;
	private float mLastDownY;

	private boolean mIsLongTouch;

	private boolean mIsTouching;

	private              float mTouchSlop;
	private final static long  LONG_CLICK_LIMIT = 300;
	private final static long  REMOVE_LIMIT     = 1500;
	private final static long  CLICK_LIMIT      = 200;

	private int mStatusBarHeight;

	private AccessibilityService mService;

	private int mCurrentMode;


	private int      mOffsetToParent;
	private Vibrator mVibrator;
	private long[]   mPattern = {0, 100};
	private Context  mContext;

	public FloatBallView(Context context) {
		super(context);
		this.mContext = context;
		mService = (AccessibilityService) context;
		mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		initView();
	}

	private void initView() {
		inflate(getContext(), R.layout.layout_ball, this);
		//        mImgBall = findViewById(R.id.img_ball);
		//        mImgBigBall = findViewById(R.id.img_big_ball);
		mImgBg = findViewById(R.id.bt_bg);
		bt_wifi = findViewById(R.id.bt_wifi);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();


		mStatusBarHeight = getStatusBarHeight();
		mOffsetToParent = dip2px(25);

		//        mImgBigBall.post(() -> {
		//            mBigBallX = mImgBigBall.getX();
		//            mBigBallY = mImgBigBall.getY();
		//
		//        });

		mImgBg.setOnClickListener((v) -> {
			AccessibilityUtil.doBack(mService);

		});

		bt_wifi.setOnClickListener((v) -> {
			Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
//			intent.putExtra("wifi_enable_next_on_connect", true);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
				intent.putExtra("extra_prefs_show_button_bar", true);
				intent.putExtra("extra_prefs_set_back_text", "返回");
				intent.putExtra("extra_prefs_set_next_text", "完成");
			}
			mService.startActivity(intent);

//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//				Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
////				intent.putExtra("only_access_points", true);
//////                      intent1.putExtra("extra_prefs_show_button_bar", true);
////				intent.putExtra("wifi_enable_next_on_connect", true);
////				intent.putExtra("extra_prefs_show_button_bar", true);
////				intent.putExtra("extra_prefs_set_back_text", "返回");
////				intent.putExtra("extra_prefs_set_next_text", "完成");
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				mService.startActivity(intent);
//			} else {
//				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				mService.startActivity(intent);
//			}
		});

	}


	/**
	 * 移除悬浮球
	 */
	private void toRemove() {
//		mVibrator.vibrate(mPattern, -1);
		FloatWindowManager.removeBallView(getContext());
	}

	/**
	 * 获取通知栏高度
	 *
	 * @return
	 */
	private int getStatusBarHeight() {
		int statusBarHeight = 0;
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object o = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = (Integer) field.get(o);
			statusBarHeight = getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	public int dip2px(float dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics());
	}
}
