package com.hengmei.hm_common.utils

import java.math.BigDecimal
import java.math.RoundingMode

object NumberUtils {

    private const val ZERO = "0"

//    转换3 位 小数
    fun formatStr(value: Double, num: Int): String {
        return try {
            val bd = BigDecimal.valueOf(value)
            bd.setScale(num, RoundingMode.DOWN).toString()
        } catch (e: NumberFormatException) {
            ZERO
        }
    }

    /**
     * 小数相加，参数为空当0处理
     */
    fun additionStr(str1: String?, str2: String?): String {
        return try {
            val bd1 = BigDecimal(str1 ?: ZERO)
            val bd2 = BigDecimal(str2 ?: ZERO)
            bd1.add(bd2).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 小数相减，参数为空当0处理
     */
    fun subtractionStr(str1: String?, str2: String?): String {
        return try {
            val bd1 = BigDecimal(str1 ?: ZERO)
            val bd2 = BigDecimal(str2 ?: ZERO)
            bd1.subtract(bd2).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 乘法保留小数位数，参数为空不运算
     */
    fun multiplicationStr(str1: String?, str2: String?, num: Int): String {
        if (str1.isNullOrEmpty() || str2.isNullOrEmpty()) {
            return ""
        }
        return try {
            val v1 = BigDecimal(str1)
            val v2 = BigDecimal(str2)
            v1.multiply(v2).setScale(num, RoundingMode.HALF_UP).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 除法保留小数位数，参数为空不运算
     *
     */
    fun divisionStr(str1: String?, str2: String?, num: Int): String {
        // 去除后置0
        val strippedStr2 = BigDecimal(str2).stripTrailingZeros().toPlainString()
        if (str1.isNullOrEmpty() || strippedStr2.isEmpty() || ZERO == strippedStr2) {
            return ZERO
        }
        return try {
            val v1 = BigDecimal(str1)
            val v2 = BigDecimal(strippedStr2)
            v1.divide(v2, num, RoundingMode.HALF_UP).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ZERO
        }
    }
}
