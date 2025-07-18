package com.hengmei.testdemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // 导入 Material Design 3 组件
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.print.PrintHelper

// 确保你的 build.gradle (Module: app) 文件中添加了 Compose 相关的依赖，例如：
// implementation(platform("androidx.compose:compose-bom:2024.06.00")) // 使用最新的 BOM 版本
// implementation("androidx.compose.ui:ui")
// implementation("androidx.compose.ui:ui-graphics")
// implementation("androidx.compose.ui:ui-tooling-preview")
// implementation("androidx.compose.material3:material3")
// debugImplementation("androidx.compose.ui:ui-tooling")
// debugImplementation("androidx.compose.ui:ui-test-manifest")
// implementation("androidx.activity:activity-compose:1.9.0") // 确保 activity-compose 版本兼容

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // 使用 Box 来包裹 TestScreenContent，以便我们可以捕获它的内容
                var captureBitmap by remember { mutableStateOf<Bitmap?>(null) }
                val view = LocalView.current // 获取当前的 View，以便我们可以在必要时从它捕获

                Column(modifier = Modifier.fillMaxSize()) {
                    // 这是您要打印的 UI 内容
                    TestScreenContent(
                        modifier = Modifier.weight(1f) // 让内容填充可用空间
                    )

                    // 打印按钮，现在触发整个界面的截图和打印
                    Button(
                        onClick = {
                            // 启动截图过程

                                captureViewToBitmap(view) { bitmap ->
                                    if (bitmap != null) {
                                        captureBitmap = bitmap
                                        // 捕获成功后立即执行打印
                                        performBitmapPrint(this@TestActivity, bitmap)
                                    } else {
                                        Toast.makeText(this@TestActivity, "未能捕获界面图片！", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("打印整个界面")
                    }
                }
            }
        }
    }

    // 辅助函数：将 View 渲染成 Bitmap
    private fun captureViewToBitmap(view: View, callback: (Bitmap?) -> Unit) {
        if (view.width == 0 || view.height == 0) {
            callback(null)
            return
        }

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas) // 将整个 View 层次结构绘制到 Canvas 上
        callback(bitmap)
    }

    // 辅助函数：执行 Bitmap 打印
    private fun performBitmapPrint(context: Context, bitmap: Bitmap) {
        val printHelper = PrintHelper(context)
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
        printHelper.printBitmap("TestScreenPrint", bitmap)
    }
}

// 独立的 Composable 函数，只包含界面的 UI 元素
@Composable
fun TestScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(), // 填充整个屏幕
        color = MaterialTheme.colorScheme.background // 使用主题背景色
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // 添加内边距
            verticalArrangement = Arrangement.Center, // 垂直居中排列
            horizontalAlignment = Alignment.CenterHorizontally // 水平居中对齐
        ) {
            Text(
                text = "欢迎来到测试界面！",
                style = MaterialTheme.typography.headlineMedium, // 应用标题风格
                color = MaterialTheme.colorScheme.primary // 使用主题主色
            )
            Spacer(modifier = Modifier.height(24.dp)) // 添加垂直间距

            // 原始的按钮，现在不再直接处理打印逻辑，而是让父级处理整个屏幕的截图
            Button(
                onClick = {
                    // TODO: 您可以在这里放置其他与打印无关的按钮点击逻辑
                    Toast.makeText(context, "内部按钮被点击了！", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.7f) // 按钮宽度占父容器的70%
            ) {
                Text("内部按钮")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "这是一个文本输入框", // 初始文本
                onValueChange = { /* TODO: 处理文本变化 */ },
                label = { Text("输入一些内容") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 卡片阴影
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "这是一个卡片",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "你可以在卡片中放置更多信息。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTestScreen() {
    MaterialTheme {
        TestScreenContent() // 预览时也只显示内容
    }
}