package com.hengmei.testdemo

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun ButtonList() {
    val buttonLabels = remember {
        mutableStateListOf("Button 1", "Button 2", "Button 3", "Button 4", "Button 5",
            "Button 6", "Button 7", "Button 8", "Button 9", "Button 10")
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buttonLabels.forEachIndexed { index, label ->
            Button(
                onClick = {
                    // 在这里处理按钮点击事件
                    Toast.makeText(context, "你点击了 ${label} (Index: ${index + 1})", Toast.LENGTH_SHORT).show()
                    // 你可以在这里添加你需要的其他逻辑
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(text = label)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonListPreview() {
    MaterialTheme {
        ButtonList()
    }
}