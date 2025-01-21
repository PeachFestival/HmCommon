package com.hengmei.testdemo

import android.app.Application
import com.hengmei.hm_common.event.FlowBusInitializer.application
import com.hengmei.hm_common.mmkv.MMKVOwner
import com.hengmei.hm_common.utils.CommonLibInit
import com.tencent.mmkv.MMKV

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val dir = filesDir.absolutePath + "/mmkv_hengmei"
        MMKV.initialize(this, dir)
        MMKVOwner.default = MMKV.defaultMMKV()
        CommonLibInit().init(this);

    }
}