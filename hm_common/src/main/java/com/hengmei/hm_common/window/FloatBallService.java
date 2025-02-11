package com.hengmei.hm_common.window;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;



/**
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallService extends AccessibilityService {
	public static final int TYPE_ADD = 0;
	public static final int TYPE_DEL = 1;

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

	}

	@Override
	public void onInterrupt() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("开启了悬浮球");
		Bundle data = intent.getExtras();
		if (data != null) {
			int type = data.getInt("type");
			if (type == TYPE_ADD) {
				FloatWindowManager.addBallView(this);
				System.out.println("添加了");
			} else {
				FloatWindowManager.removeBallView(this);
				System.out.println("移除了");
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FloatWindowManager.removeBallView(this);
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		// Optionally bring the app to the foreground

		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
}
