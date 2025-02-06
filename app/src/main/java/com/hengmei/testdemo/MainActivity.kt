package com.hengmei.testdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.hengmei.hm_common.mmkv.DataRepository
import com.hengmei.hm_common.utils.CommonLibInit
import com.hengmei.hm_common.utils.FileUtils
import com.hengmei.hm_common.utils.UpdateUtils
import com.hengmei.testdemo.ui.theme.TestDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataRepository.testUser = "user"
        setContent {
            TestDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var name = DataRepository.testUser
                    Greeting(name,activity = this@MainActivity)
                }
            }
        }
        var sdPath = FileUtils.getSDPath()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, activity: MainActivity? = null) {
    val context = LocalContext.current // 获取当前上下文
    Text(
        text = "Hello $name!",
        modifier = modifier.clickable {
            UpdateUtils.getUpdateUrl("http://manage.hengmeierp.com/api/project/produceApkRela/getByApp","e4fec07c-8917-44ca-99f5-582daa869f02","eb477b2f48026fda","更新", activity!!, callback = { isUpdate, message ->
                if (isUpdate!!) {
                    // 调用回调返回错误信息
                    println("开始更新: $message")
                } else {
                    // 调用回调返回响应数据
                    println("更新失败: $message")
                }
            })
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestDemoTheme {
        Greeting("Android")
    }
}