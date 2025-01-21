package com.hengmei.hm_common.utils

import android.content.Context
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hengmei.hm_common.event.ApplicationScopeViewModelProvider
import com.hengmei.hm_common.event.EventMessage
import com.hengmei.hm_common.event.FlowBus
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val ISBAN = "ISBAN"
const val PHOTO_PATH = "/Pictures/back.jpg"
const val SPLASHTIME = 8000L //闪屏时间

fun inflate(
    context: Context,
    viewId: Int,
    parent: ViewGroup? = null,
    attachToRoot: Boolean = false
): View {
    return LayoutInflater.from(context).inflate(viewId, parent, attachToRoot)
}

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun TextView.toTrimString(): String {
    return this.text?.toString()?.trim() ?: ""
}

fun AppCompatEditText.toEditString(): String {
    return this.text?.toString()?.trim() ?: ""
}

fun getAndroidId(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ANDROID_ID
    )
}

//返回byte
//exposureTimeByte[1] = (timeOfExposure / 256).toByte()
//exposureTimeByte[2] = (timeOfExposure % 256).toByte()
fun intToBytes(value: Int): ByteArray {
//    定义一个byte数组
    val bytes = ByteArray(2)
    bytes[0] = (value / 256).toByte()
    bytes[1] = (value % 256).toByte()
    return bytes
}

fun formatDateToSecond(date: Date): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    return dateFormat.format(date)
}

fun formatDateToDay(date: Date): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    return dateFormat.format(date)
}

fun formatDateToHour(date: Date): String {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    return dateFormat.format(date)
}

fun getRandomSampleCode(): String {
    val date = Date(System.currentTimeMillis())
    val dateFormat = SimpleDateFormat("MMddHHMMSS", Locale.CHINA)
    return dateFormat.format(date)
}

//将 "yyyy-MM-dd HH:mm:ss" 格式转为 "MMddHHMMSS"
fun formatTimeToSampleCode(inputTime: String): String? {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMddHHmmss", Locale.getDefault())

        // 解析输入时间字符串，并进行空安全检查
        val date = inputFormat.parse(inputTime) ?: return null

        // 格式化输出时间字符串
        outputFormat.format(date)
    } catch (e: ParseException) {
        println("时间格式错误: ${e.message}")
        null // 返回 null 表示转换失败
    }
}

// 无符号
fun byteToInt(byte1: Byte, byte2: Byte): Int {
    return (byte1.toInt() and 0x00ff) * 256 + (byte2.toInt() and 0x00ff)
}

// 无符号
fun byteToInt(byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte): Int {
    return (byte1.toInt() and 0x00ff) * 256 * 256 * 256 + (byte2.toInt() and 0x00ff) * 256 * 256 + (byte3.toInt() and 0x00ff) * 256 + (byte4.toInt() and 0x00ff)
}

// int 转有符号 整数
fun intToByte(value: Int): ByteArray {
    val hexString = String.format("%04X", value and 0xFFFF)
    val len = hexString.length
    val byteArray = ByteArray(len / 2)
    for (i in 0 until len step 2) {
        byteArray[i / 2] = ((Character.digit(hexString[i], 16) shl 4)
                + Character.digit(hexString[i + 1], 16)).toByte()
    }
    return byteArray
}

// byte 输出 字符串
fun byteToString(bytes: ByteArray): String {
    val str = bytes.joinToString("") {
        String.format("%02X", it)
    }
    return str
}

// 将byte数组进行 crc 校验，返回校验后的数组
fun crc16(bytes: ByteArray): ByteArray {
    return CRC16Util.calcCrc16(bytes)
}

// 将 byte 数组转换为 double
fun byteArrayToDouble(bytes: ByteArray): Double {
    // 将 byte 数组转换为 long 值
    // 将 byte 数组转换为 long 值
    var longBits: Long = 0
    for (i in bytes.indices) {
        longBits = longBits or (bytes[i].toLong() and 0xFFL shl 8 * (7 - i))
    }
    // 将 long 值的位表示形式转换为 double
    return java.lang.Double.longBitsToDouble(longBits)
}

//     将 double 转换为 ByteArray
fun doubleToHex(value: Double): ByteArray {
    // Convert double to raw long bits
    val bits = java.lang.Double.doubleToRawLongBits(value)
    // Convert long bits to hexadecimal string
    var str = java.lang.Long.toHexString(bits).uppercase(Locale.getDefault())
    require(str.length % 2 == 0) { "Hex string must have an even length" }

    // 创建一个字节数组，大小为字符串长度的一半
    val byteArray = ByteArray(str.length / 2)

    // 将每一对字符转换为一个字节
    for (i in str.indices step 2) {
        val byte = str.substring(i, i + 2).toInt(16).toByte()
        byteArray[i / 2] = byte
    }
    return byteArray
}


fun Context.getStringRes(stringRes: Int): String {
    return resources.getString(stringRes)
}

//LCE -> Loading/Content/Error
sealed class PageState<out T> {
    data class Success<T>(val data: T) : PageState<T>()
    data class Error<T>(val message: String) : PageState<T>() {
        constructor(t: Throwable) : this(t.message ?: "")
    }
}

fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> {
    return this
}

fun log(message: String) {
    Logger.d(message)
}

sealed class FetchStatus {
    object Fetching : FetchStatus()
    object Fetched : FetchStatus()
    object NotFetched : FetchStatus()
}


//获取泛型的实例
fun <VM> getVMCls(cls: Any, index: Int = 0): VM {
    return (cls.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[index] as VM
}//利用扩展函数

fun LifecycleOwner.observeEvent(
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    isSticky: Boolean = false,
    action: (t: EventMessage) -> Unit
) {
    ApplicationScopeViewModelProvider
        .getApplicationScopeViewModel(FlowBus::class.java)
        .with(EventMessage::class.java, isSticky = isSticky)
        .observeEvent(this@observeEvent, dispatcher, minActiveState, action)
}

/**
 *
 * Dispatchers.Main
Android上的主线程，用于处理UI交互和一些轻量级任务，eg：调用UI函数、调用suspend函数、更新LiveData。

Dispatchers.IO
非主线程，专为磁盘和网络IO进行了优化，eg：数据库、文件读写、网络请求。

Dispatchers.Default
非主线程，协程开启的默认调度器，专为CPU密集型任务进行了优化，eg：数组排序、JSON解析、复杂逻辑处理、计算处理。
 * */
fun postValue(
    event: EventMessage,
    delayTimeMillis: Long = 0,
    isSticky: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) {
    log("FlowBus:send_${Thread.currentThread().name}_${Gson().toJson(event)}")
    ApplicationScopeViewModelProvider
        .getApplicationScopeViewModel(FlowBus::class.java)
        .viewModelScope
        .launch(dispatcher) {
            delay(delayTimeMillis)
            ApplicationScopeViewModelProvider
                .getApplicationScopeViewModel(FlowBus::class.java)
                .with(EventMessage::class.java, isSticky = isSticky)
                .setValue(event)
        }
}
