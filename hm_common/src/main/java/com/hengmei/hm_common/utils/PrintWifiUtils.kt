package com.hengmei.hm_common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.print.PrintHelper

class PrintWifiUtils {


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