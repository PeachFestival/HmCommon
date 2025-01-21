package com.hengmei.hm_common.utils

import android.widget.TextView
import com.google.gson.Gson
import java.net.URLEncoder
import java.util.Locale

/**
 * 扩展函数
 *
 * @author zhihao
 * @since 2022-03-10
 */

/**
 * String转为百分编码
 */
fun String.encode(enc: String = "utf-8"): String {
    return URLEncoder.encode(this, enc)
}

val String.urlEncode: String
    get() = URLEncoder.encode(this, "utf-8")

/**
 * 保留小数点后${decimal}几位
 */
fun Float.keepDecimal(decimal: Int = 4): Float {
    return String.format("%.${decimal}f", this).toFloat()
}

/**
 * 保留小数点后${decimal}几位
 */
fun Double.keepDecimal(decimal: Int = 4): Double {
    return String.format("%.${decimal}f", this).toDouble()
}

/**
 * 打印
 */
fun <T> T.println(
    block: (T) -> String = {
        it.toString()
    }
): T {
    println(block(this))
    return this
}

/**
 * 对象转json字符串
 */
fun <T : Any> T.toJson(): String {
    return Gson().toJson(this)
}

/**
 * 0和1 切换
 */
fun Int.change01(): Int = if (this == 0) 1 else 0

/**
 * 非空转换，如果是null的话，返回空字符串，否则返回本身
 */
fun String?.notNullConvert(): String =
    if (this == null || this.isEmpty()) {
        ""
    } else {
        this
    }

/**
 * 非空转换
 */
fun Long?.notNullConvert(d: Long = 0L): Long = this ?: d

/**
 * 判断字符串不为null并且不是空字符串
 */
fun CharSequence?.isNotNullAndEmpty(): Boolean = !this.isNullOrEmpty()

/**
 * 判断List不能为空和是空的
 */
fun <T> Collection<T>?.isNotNullAndEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

/**
 * String 转 double
 */
fun String?.toMyDouble(d: Double = 0.0): Double {
    if (this == null || this.isEmpty()) return d
    return try {
        this.toDouble()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        d
    }
}

/**
 * String 转 int
 */
fun String?.toMyInt(d: Int = 0): Int {
    if (this.isNullOrEmpty()) return d
    return try {
        this.toInt()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        d
    }
}

/**
 * String 转 Long
 */
fun String?.toMyLong(d: Long = 0): Long {
    if (this.isNullOrEmpty()) return d
    return try {
        this.toLong()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        d
    }
}

/**
 * 比较double结果值
 * @param   resultType 0小于等于，1大于等于
 */
fun Double.compareWith(value: Double, resultType: Int): Boolean {
    return if (resultType == 0) {
        this <= value
    } else {
        this >= value
    }
}


/**
 * ByteArray转十六进制打印字符串
 */
fun ByteArray.toPrintString(): String {
    return this.joinToString(" ") {
        it.toInt().and(0xFF).toString(16).padStart(2, '0').uppercase(Locale.ROOT)
    }
}
fun TextView.addText(text: CharSequence) {
    this.text = this.text.toString() + text.toString()
}
//
fun TextView.addTextHead(text: CharSequence) {
    this.text = text.toString() + this.text.toString()
}

