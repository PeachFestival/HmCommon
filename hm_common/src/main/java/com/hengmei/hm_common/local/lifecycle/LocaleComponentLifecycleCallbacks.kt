package com.hengmei.hm_common.local.lifecycle

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import com.hengmei.hm_common.local.utils.LocaleHelper
/**
 * Created by reborn on 2019-12-11.
 */
class LocaleComponentLifecycleCallbacks(private val context: Context)  : ComponentCallbacks2 {

    override fun onLowMemory() { }

    override fun onTrimMemory(level: Int) { }

    override fun onConfigurationChanged(newConfig: Configuration) {
        LocaleHelper.getInstance().onConfigurationChanged(context)
    }
}