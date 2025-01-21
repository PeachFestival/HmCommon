package com.hengmei.hm_common.event


import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.hengmei.hm_common.entity.GlobalNotificationEntity

class LiveDataEvent : LiveData<GlobalNotificationEntity>() {

    /*
    * GlobalNotification
    * */
    private val handler: Handler = Handler(Looper.getMainLooper())

    /*    private val timerRunnable = object : Runnable {
            override fun run() {
                postValue(count++)
                handler.postDelayed(this, 1000)
            }
        }*/

    fun postModbus(value : GlobalNotificationEntity) {
//        count = 0
//        handler.postDelayed(timerRunnable, 1000)
        postValue(value)
    }

    fun cancelTimer() {
//        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        private lateinit var sInstance: LiveDataEvent

        private var count = 0

        @MainThread
        fun get(): LiveDataEvent {
            sInstance = if (::sInstance.isInitialized) sInstance else LiveDataEvent()
            return sInstance
        }
    }

}
