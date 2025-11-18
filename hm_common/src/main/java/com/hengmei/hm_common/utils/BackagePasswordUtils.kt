package com.hengmei.hm_common.utils

import android.app.Activity
import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object BackagePasswordUtils {
    val TAG = "BackagePasswordUtils"
    // 获取后台动态密码
    fun getBackagePassword(
        context: Activity,
        callback: (Boolean, String) -> Unit
    ){
        Thread({
            val gson = Gson()
            val url = "http://manage.hengmeierp.com/api/project/produceApkInstrumentCipher/getCipher?apkKey=bb5c39445e5564e709f0f578d604d95e"

            val getUrl = URL(url)
            val connection = getUrl.openConnection() as HttpURLConnection
            connection.apply {
                setDoOutput(false)  // GET请求不需要设置DoOutput
                setDoInput(true)
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
            }

            // 获取响应码
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // 解析JSON响应
                val jsonResponse = gson.fromJson(response.toString(), Map::class.java)
                val code = jsonResponse["code"] as? Number ?: 0
                val msg = jsonResponse["msg"] as? String ?: ""
                val data = jsonResponse["data"] as? String

                context.runOnUiThread {
                    if (code.toInt() == 200 && data != null) {
                        // 请求成功，返回data
                        callback(true,data)
                    } else {
                        // 请求失败
                        val msg1 = "请求后台密码失败: $msg"
                        Log.d(TAG, msg1)
                        callback(false,msg1)
                    }
                }
            } else {
                context.runOnUiThread {
                    val msg = "请求后台密码时请求失败: $responseCode"
                    Log.d(TAG, msg)
                    callback(false,msg)
                }
            }
        }).start()
    }
}