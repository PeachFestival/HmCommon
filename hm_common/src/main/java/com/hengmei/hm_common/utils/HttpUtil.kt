package com.hengmei.hm_common.utils

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class HttpUtil {

    private val client = OkHttpClient()

    // 发起 GET 请求
    fun get(url: String, callback: (String?, String?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, "请求失败: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(response.body?.string(), null)
                } else {
                    callback(null, "请求失败: ${response.code}")
                }
            }
        })
    }

    // 发起 POST 请求
    fun post(url: String, json: String, callback: (String?, String?) -> Unit) {
        val mediaType = "application/json".toMediaType()
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, "请求失败: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(response.body?.string(), null)
                } else {
                    callback(null, "请求失败: ${response.code}")
                }
            }
        })
    }
}