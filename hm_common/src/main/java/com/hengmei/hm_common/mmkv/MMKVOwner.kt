package com.hengmei.hm_common.mmkv;

import android.os.Parcelable
import com.tencent.mmkv.MMKV

import kotlin.jvm.JvmStatic;
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A class that has a MMKV instance. If you want to customize the MMKV, you can override
 * the kv property. For example:
 *
 * ```kotlin
 * object DataRepository : MMKVOwner {
 *   override val kv = MMKV.mmkvWithID("MyID")
 * }
 * ```
 *
 * @author Dylan Cai
 */
interface MMKVOwner {
  val kv: MMKV
    get() = default ?: throw IllegalStateException("If you use MMKV in Application, you should set MMKVOwner.default first.")

  companion object {
    @JvmStatic
    var default: MMKV? = null
  }
}

fun MMKVOwner.mmkvInt(default: Int = 0) =
  MMKVProperty({ kv.decodeInt(it, default) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvLong(default: Long = 0L) =
  MMKVProperty({ kv.decodeLong(it, default) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvBool(default: Boolean = false) =
  MMKVProperty({ kv.decodeBool(it, default) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvFloat(default: Float = 0f) =
  MMKVProperty({ kv.decodeFloat(it, default) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvDouble(default: Double = 0.0) =
  MMKVProperty({ kv.decodeDouble(it, default) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvString() =
  MMKVProperty({ kv.decodeString(it) }, { kv.encode(first, second) })

// 只做初始赋值，值改变后后期为空便为空
fun MMKVOwner.mmkvStringFirst(first: String) =
  MMKVProperty({ kv.decodeString(it) ?: first }, { kv.encode(first, second) })

// 值改变后后期为空便为default的值
fun MMKVOwner.mmkvString(default: String) =
  MMKVProperty(
    decode = { key ->
      val value = kv.decodeString(key)
      if (value.isNullOrEmpty()) default else value
    },
    encode = {
      kv.encode(first, second)
    }
  )

fun MMKVOwner.mmkvStringSet() =
  MMKVProperty({ kv.decodeStringSet(it) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvStringSet(default: Set<String>) =
  MMKVProperty({ kv.decodeStringSet(it) ?: default }, { kv.encode(first, second) })

fun MMKVOwner.mmkvBytes() =
  MMKVProperty({ kv.decodeBytes(it) }, { kv.encode(first, second) })

fun MMKVOwner.mmkvBytes(default: ByteArray) =
  MMKVProperty({ kv.decodeBytes(it) ?: default }, { kv.encode(first, second) })

inline fun <reified T : Parcelable> MMKVOwner.mmkvParcelable() =
  MMKVProperty({ kv.decodeParcelable(it, T::class.java) }, { kv.encode(first, second) })

inline fun <reified T : Parcelable> MMKVOwner.mmkvParcelable(default: T) =
  MMKVProperty({ kv.decodeParcelable(it, T::class.java) ?: default }, { kv.encode(first, second) })

class MMKVProperty<V>(
  private val decode: (String) -> V,
  private val encode: Pair<String, V>.() -> Boolean
) : ReadWriteProperty<MMKVOwner, V> {
  private var cache: V? = null

  override fun getValue(thisRef: MMKVOwner, property: KProperty<*>): V =
    cache ?: decode(property.name).also { cache = it }

  override fun setValue(thisRef: MMKVOwner, property: KProperty<*>, value: V) {
    if (encode(property.name to value)) {
      cache = value
    }
  }
}