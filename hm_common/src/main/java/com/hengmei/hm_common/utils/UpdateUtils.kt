package com.hengmei.hm_common.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import com.allenliu.versionchecklib.core.http.HttpRequestMethod
import com.allenliu.versionchecklib.utils.AppUtils
import com.allenliu.versionchecklib.v2.AllenVersionChecker
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder
import com.allenliu.versionchecklib.v2.builder.UIData
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener
import com.google.gson.Gson
import com.hengmei.hm_common.Bean.ApiResponse
import org.w3c.dom.Text


object UpdateUtils {

    // 根据链接获取更新包
    /**
     * @param mUrl 请求地址
     * @param updateApk 更新包的标识
     * @param acCode 仪器编号
     * @param title 更新标题
     * @param context activity
     * @param callback 回调函数
     */
    fun getUpdateUrl(
        mUrl: String,
        updateApk: String,
        acCode: String,
        title: String,
        context: Activity,
        callback: (Boolean?,String?) -> Unit  // 回调函数
    ) {
        var devId = acCode
        if(TextUtils.isEmpty(acCode)){
            devId = getAndroidId()
        }
        val gson = Gson()
        val url = "$mUrl?updateAPK=$updateApk&acCode=$devId"
        AllenVersionChecker
            .getInstance()
            .requestVersion()
            .setRequestMethod(HttpRequestMethod.GET)
            .setRequestUrl(url)
            .request(object : RequestVersionListener {
                override fun onRequestVersionSuccess(
                    downloadBuilder: DownloadBuilder?,
                    response: String?
                ): UIData? {
                    val result = gson.fromJson(response, ApiResponse::class.java)
                    var  content="测试更新"
                    if(TextUtils.isEmpty(result.data.content)) {
                        content = "测试更新"
                    }else{
                        content = result.data.content
                    }
//                    判断版本号是否相同
                    if(result.data.apkRevision == getVersionCode(context).toString() ){
                        callback(false,"当前版本为最新版本")
                        return null
                    }else{
                        callback(true,result.data.content)
                        return UIData.create()
                            .setDownloadUrl(result.data.apkOssUrl)
                            .setContent(content)
                            .setTitle(title)
                    }

                }

                override fun onRequestVersionFailure(message: String?) {
                    callback(false,message!!)
                }

            })
            .executeMission(context);

    }


    fun getVersionCode(sContext: Context): Int {
        return try {
            val packageInfo: PackageInfo =
                sContext.getPackageManager()
                    .getPackageInfo(
                        sContext.getPackageName(),
                        0
                    )
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0 // 默认值
        }
    }


}