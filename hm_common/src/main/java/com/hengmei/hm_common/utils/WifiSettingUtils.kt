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
import androidx.fragment.app.FragmentActivity
import com.hengmei.hm_common.wifi.WifiSettingDialog
import com.hengmei.hm_common.window.FloatBallService
import com.iwdael.wifimanager.IWifi

/**
 * WiFi 设置工具类
 * 提供便捷的 WiFi 对话框调用、多语言国际化适配、权限检查以及状态回调
 */
class WifiSettingUtils {

    /**
     * WiFi 连接事件监听接口（供 Java / Kotlin 外部调用）
     */
    interface OnWifiDialogListener {
        /** 连接成功回调 */
        fun onConnected(wifi: IWifi?) {}
        /** 断开连接回调 */
        fun onDisconnected() {}
        /** 连接超时回调 */
        fun onConnectTimeout() {}
    }

    companion object {
        const val WIFI_PERMISSION_REQUEST_CODE = 2001

        /**
         * 获取当前系统所需的 WiFi 操作权限列表
         */
        @JvmStatic
        fun getRequiredWifiPermissions(): Array<String> {
            val list = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                list.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
                list.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            return list.toTypedArray()
        }

        /**
         * 检查是否已拥有全部 WiFi 权限
         */
        @JvmStatic
        fun hasWifiPermissions(context: Context): Boolean {
            val permissions = getRequiredWifiPermissions()
            for (perm in permissions) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, perm)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }

        /**
         * 检查并申请 WiFi 操作所需权限
         * @return true: 已获取全部权限；false: 已触发权限申请
         */
        @JvmStatic
        fun checkAndRequestWifiPermissions(activity: Activity, requestCode: Int = WIFI_PERMISSION_REQUEST_CODE): Boolean {
            val permissions = getRequiredWifiPermissions()
            val deniedList = mutableListOf<String>()
            for (perm in permissions) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(activity, perm)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    deniedList.add(perm)
                }
            }
            if (deniedList.isNotEmpty()) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    activity,
                    deniedList.toTypedArray(),
                    requestCode
                )
                return false
            }
            return true
        }

        /**
         * 打开新 WiFi 设置（弹窗方式） - 基础调用 / Lambda 回调调用
         *
         * @param activity 当前宿主 Activity
         * @param onConnected 连接成功回调 (可获取当前已连接的 [IWifi] 对象)
         * @param onDisconnected 断开连接回调
         * @param onTimeout 连接超时回调
         * @return 返回创建并展示的 [WifiSettingDialog] 实例
         */
        @JvmStatic
        @JvmOverloads
        fun showWifiDialog(
            activity: FragmentActivity,
            onConnected: ((wifi: IWifi?) -> Unit)? = null,
            onDisconnected: (() -> Unit)? = null,
            onTimeout: (() -> Unit)? = null
        ): WifiSettingDialog {
            return WifiSettingDialog.show(
                activity = activity,
                onConnected = onConnected,
                onDisconnected = onDisconnected,
                onTimeout = onTimeout
            )
        }

        /**
         * 打开新 WiFi 设置（弹窗方式） - 带事件监听接口（Java 调用最友好）
         *
         * @param activity 当前宿主 Activity
         * @param listener 连接状态监听器
         */
        @JvmStatic
        fun showWifiDialogWithListener(
            activity: FragmentActivity,
            listener: OnWifiDialogListener?
        ): WifiSettingDialog {
            return WifiSettingDialog.show(
                activity = activity,
                onConnected = { wifi -> listener?.onConnected(wifi) },
                onDisconnected = { listener?.onDisconnected() },
                onTimeout = { listener?.onConnectTimeout() }
            )
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun checkUpPermission(context: Activity) {
            try {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivityForResult(intent, 100)
                } else {
                    startMaskService(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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