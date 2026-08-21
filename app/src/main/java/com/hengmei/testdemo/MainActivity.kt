package com.hengmei.testdemo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.dylanc.longan.longToast
import com.hengmei.hm_common.local.utils.LocaleHelper
import com.hengmei.hm_common.utils.BackagePasswordUtils
import com.hengmei.hm_common.utils.UpdateUtils
import com.hengmei.hm_common.utils.WifiSettingUtils
import com.hengmei.hm_common.utils.getAndroidId
import java.util.Locale

class MainActivity : FragmentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.getInstance().updateContext(newBase))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启动时主动申请 WiFi 操作相关权限
        WifiSettingUtils.checkAndRequestWifiPermissions(this)

        setContent {
            ButtonList { buttonIndex -> // 传递点击处理函数
                when (buttonIndex) {
                    0 -> {
                        WifiSettingUtils.checkUpPermission(this@MainActivity)
                    }
                    1 -> {
                        WifiSettingUtils.setWifi(this@MainActivity, "返回", "确认")
                    }
                    2 -> {
                        // 打开 WiFi 弹窗（支持国际化多语言及事件回调）
                        WifiSettingUtils.showWifiDialog(
                            activity = this@MainActivity,
                            onConnected = { wifi ->
                                longToast("WiFi 连接成功: ${wifi?.name()}")
                            },
                            onTimeout = {
                                longToast("WiFi 连接超时")
                            }
                        )
                    }
                    3 -> {
                        UpdateUtils.getUpdateUrl(
                            "http://manage.hengmeierp.com/api/project/produceApkRela/getByApp",
                            "e4fec07c-8917-44ca-99f5-582daa869f02",
                            getAndroidId(),
                            "更新",
                            this@MainActivity,
                            callback = { isUpdate, message ->
                                if (isUpdate == true) {
                                    longToast("开始更新: $message")
                                } else {
                                    longToast("更新失败: $message")
                                }
                            }
                        )
                    }
                    4 -> {
                        startActivity(Intent(this, TestActivity::class.java))
                    }
                    5 -> {
                        BackagePasswordUtils.getBackagePassword(this, callback = { isSuccess, message ->
                            if (isSuccess) {
                                longToast("获取到的后台密码为：$message")
                            } else {
                                longToast(message)
                            }
                        })
                    }
                    6 -> {
                        // 切换语言对话框
                        showLanguageSelectDialog()
                    }
                }
            }
        }
    }

    /**
     * 切换语言对话框
     */
    private fun showLanguageSelectDialog() {
        val languages = arrayOf(
            "简体中文 (Simplified Chinese)",
            "English (英文)",
            "繁體中文 (Traditional Chinese)",
            "跟随系统 (Auto)"
        )

        val currentLocale = LocaleHelper.getInstance().getCurrentAppLocale(this)
        var checkedItem = when {
            currentLocale.language == Locale.ENGLISH.language -> 1
            currentLocale.country == Locale.TAIWAN.country || currentLocale.country == "TW" || currentLocale.country == "HK" -> 2
            currentLocale.language == Locale.CHINESE.language || currentLocale.language == "zh" -> 0
            else -> 3
        }

        AlertDialog.Builder(this)
            .setTitle("选择语言 / Select Language")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val targetLocale = when (which) {
                    0 -> Locale.SIMPLIFIED_CHINESE
                    1 -> Locale.ENGLISH
                    2 -> Locale.TRADITIONAL_CHINESE
                    else -> Locale("") // 跟随系统
                }
                dialog.dismiss()
                LocaleHelper.getInstance().language(targetLocale).apply(this@MainActivity)
            }
            .setNegativeButton("取消 / Cancel", null)
            .show()
    }
}

@Composable
fun ButtonList(onButtonClick: (Int) -> Unit) {
    val buttonLabels = remember {
        mutableStateListOf(
            "悬浮窗权限",
            "打开WIFI设置",
            "打开新WIFI设置(弹窗)",
            "应用更新",
            "打印界面",
            "获取动态密码",
            "切换语言 / Switch Language"
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buttonLabels.forEachIndexed { index, label ->
            Button(
                onClick = {
                    onButtonClick(index)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
            ) {
                Text(text = label)
            }
        }
    }
}