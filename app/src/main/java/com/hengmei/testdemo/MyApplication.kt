package com.hengmei.testdemo

import android.app.Application
import android.os.Environment
import com.hengmei.hm_common.crashlog.CrashHandler
import com.hengmei.hm_common.mmkv.MMKVOwner
import com.hengmei.hm_common.utils.CommonLibInit
import com.tencent.mmkv.MMKV

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val dir = filesDir.absolutePath + "/mmkv_hengmei"
        MMKV.initialize(this, dir)
        MMKVOwner.default = MMKV.defaultMMKV()
        CommonLibInit().init(this)
        CrashHandler.getInstance(applicationContext).setCrashLogDir(getCrashLogDir())
    }

    private fun getCrashLogDir() : String {
        return "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/log"
    }

}