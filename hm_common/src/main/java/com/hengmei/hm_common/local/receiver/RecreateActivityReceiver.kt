package com.hengmei.hm_common.local.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.hengmei.hm_common.local.LocaleConstant

class RecreateActivityReceiver(private val cont: Context?) : BroadcastReceiver() {

    fun getDefaultIntentFilter() : IntentFilter {
        val defaultIntentFilter = IntentFilter()
        defaultIntentFilter.addAction(LocaleConstant.ACTION_RECREATE_ACTIVITY)
        return defaultIntentFilter
    }

    fun register() {
        cont?.registerReceiver(this, getDefaultIntentFilter())
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (cont is Activity) {
            cont.recreate()
        }
    }
}