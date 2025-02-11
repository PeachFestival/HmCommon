package com.hengmei.hm_common.utils

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.hengmei.hm_common.R
import com.hengmei.hm_common.window.FloatBallService

//wifi 连接工具类
//加入 遮挡
class WifiSettingUtils {
    companion object {
        @RequiresApi(Build.VERSION_CODES.M)
        fun checkUpPermission(context: Activity) {
            try {
                // 检查权限
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivityForResult(intent, 100)
                }else{
                    startMaskService(context)
                }
            } catch (e: Exception) {

            }
        }

        fun startMaskService(context: Activity) {
            context.startService(Intent(context, FloatBallService::class.java).apply {
                putExtras(Bundle().apply {
                    putInt("type", FloatBallService.TYPE_DEL)
                })
            })
        }

        fun setWifi(context: Activity, backStr: String, confirmStr: String) {

            if (!isAccessibilityServiceEnabled(context, FloatBallService::class.java)) {

                context.startService(Intent(context, FloatBallService::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt("type", FloatBallService.TYPE_ADD)
                    })
                })

                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)


            } else {
                context.startService(Intent(context, FloatBallService::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt("type", FloatBallService.TYPE_ADD)
                    })
                })
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.startActivityForResult(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK).apply {
//                    putExtra("extra_prefs_show_button_bar", true)
                        putExtra("only_access_points", true)
                        putExtra("wifi_enable_next_on_connect", true)
                        putExtra("extra_prefs_show_button_bar", true)
                        putExtra("extra_prefs_set_back_text", backStr)
                        putExtra("extra_prefs_set_next_text", confirmStr)
                    }, 100)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        putExtra("extra_prefs_show_button_bar", true)
                        putExtra("extra_prefs_set_back_text", backStr)
                        putExtra("extra_prefs_set_next_text", confirmStr)
                    })
                } else {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            }

        }


        fun isAccessibilityServiceEnabled(
            context: Context,
            service: Class<out AccessibilityService>
        ): Boolean {
            val expectedComponentName = ComponentName(context, service)
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(
                        expectedComponentName.flattenToString(),
                        ignoreCase = true
                    )
                ) {
                    return true
                }
            }
            return false
        }
    }

}