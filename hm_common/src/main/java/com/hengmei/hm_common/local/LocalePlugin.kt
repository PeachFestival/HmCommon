package com.hengmei.hm_common.local
import android.app.Application
import com.hengmei.hm_common.local.lifecycle.LocaleActivityLifecycleCallbacks
import com.hengmei.hm_common.local.lifecycle.LocaleComponentLifecycleCallbacks
import com.hengmei.hm_common.local.utils.ActivityHelper
import com.hengmei.hm_common.local.utils.BroadcastReceiverManager
import com.hengmei.hm_common.local.utils.LocaleHelper

/**
 * Created by reborn on 2019-12-10.
 */
object LocalePlugin {

    private var localeActivityLifecycleCallbacks: LocaleActivityLifecycleCallbacks? = null
    private var localeComponentLifecycleCallbacks: LocaleComponentLifecycleCallbacks?  = null

    // 注册 LifecycleCallbacks
    private fun registerLifecycleCallbacks(application: Application) {
        localeActivityLifecycleCallbacks = LocaleActivityLifecycleCallbacks()
        localeComponentLifecycleCallbacks = LocaleComponentLifecycleCallbacks(application)
        application.registerActivityLifecycleCallbacks(localeActivityLifecycleCallbacks)
        application.registerComponentCallbacks(localeComponentLifecycleCallbacks)
    }

    // 注销 LifecycleCallbacks
    private fun unregisterLifecycleCallbacks(application: Application) {
        localeActivityLifecycleCallbacks?.let {
            application.unregisterActivityLifecycleCallbacks(it)
            localeActivityLifecycleCallbacks = null
        }
        localeComponentLifecycleCallbacks?.let {
            application.unregisterComponentCallbacks(it)
            localeComponentLifecycleCallbacks = null
        }
    }

    fun init(application: Application, updateInterfaceWay: Int = LocaleConstant.RECREATE_CURRENT_ACTIVITY): LocalePlugin {
        return this.apply {

            // 初始化所有用到的工具类
            ActivityHelper.init(updateInterfaceWay)
            LocaleDefaultSPHelper.init(application)
            LocaleHelper.init(application)

            registerLifecycleCallbacks(application)

            // 缓存系统语言
            LocaleHelper.getInstance().cacheSystemLocale()
            // 设置语言
            LocaleHelper.getInstance().updateApplicationContext(application)
        }
    }

    // App 结束时调用
    fun terminate(application: Application) {
        unregisterLifecycleCallbacks(application)
        BroadcastReceiverManager.unregisterAllBroadcastReceiver()
    }
}