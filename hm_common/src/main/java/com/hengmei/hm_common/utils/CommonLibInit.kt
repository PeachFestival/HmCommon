package com.hengmei.hm_common.utils

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.hengmei.hm_common.event.FlowBusInitializer
import com.hengmei.hm_common.local.LocalePlugin
import com.hengmei.hm_common.mmkv.MMKVOwner
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

class CommonLibInit {
    //在Application中初始化
    fun init(application: Application) {

        FlowBusInitializer.init(application)

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .tag("hengmeiTag")
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        val imageLoader = ImageLoader.Builder(application)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()

        //设置全局唯一实例
        Coil.setImageLoader(imageLoader)

        LocalePlugin.init(application)

    }
}