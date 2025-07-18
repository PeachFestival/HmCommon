package com.hengmei.testdemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dylanc.longan.activity
import com.dylanc.longan.longToast
import com.hengmei.hm_common.mmkv.DataRepository
import com.hengmei.hm_common.utils.UpdateUtils
import com.hengmei.hm_common.utils.WifiSettingUtils
import com.hengmei.hm_common.utils.getAndroidId

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataRepository.testUser = "user"
        setContent {
            ButtonList { buttonIndex -> // 传递点击处理函数
                    when(buttonIndex) {
                        0 -> {
                            WifiSettingUtils.checkUpPermission(this@MainActivity)
                        }
                        1 -> {
                            WifiSettingUtils.setWifi(this@MainActivity,"返回","确认")
                        }

                        2 -> {
                            UpdateUtils.getUpdateUrl("http://manage.hengmeierp.com/api/project/produceApkRela/getByApp",
                                "e4fec07c-8917-44ca-99f5-582daa869f02", getAndroidId()
                                ,"更新",
                                activity!!, callback = { isUpdate, message ->
                                if (isUpdate!!) {
                                    // 调用回调返回错误信息
                                    longToast("开始更新: $message")
                                } else {
                                    // 调用回调返回响应数据
                                    longToast("更新失败: $message")
                                }
                            })
                        }
                        3 -> {

                            startActivity(Intent(this, TestActivity::class.java))
                        }
                    }
            }
        }
    }

}



@Composable
fun ButtonList(onButtonClick: (Int) -> Unit) { // 接收点击处理函数作为参数
    val buttonLabels = remember {
        mutableStateListOf("悬浮窗权限", "打开WIFI设置", "应用更新", "打印界面"
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(top = 56.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buttonLabels.forEachIndexed { index, label ->
            Button(
                onClick = {
                    onButtonClick(index) // 调用 MainActivity 中提供的点击处理函数，传递按钮索引
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)

            ) {
                Text(text = label)
            }
        }
    }
}