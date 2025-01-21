package com.hengmei.hm_common.event

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

object ApplicationScopeViewModelProvider : ViewModelStoreOwner {

    private val eventViewModelStore: ViewModelStore = ViewModelStore()

    private val mApplicationProvider: ViewModelProvider by lazy {
        ViewModelProvider(
            ApplicationScopeViewModelProvider,
            ViewModelProvider.AndroidViewModelFactory.getInstance(FlowBusInitializer.application)
        )
    }

    fun <T : ViewModel> getApplicationScopeViewModel(modelClass: Class<T>): T {
        return mApplicationProvider[modelClass]
    }

    override val viewModelStore: ViewModelStore
        get() = eventViewModelStore

}

object FlowBusInitializer {
    lateinit var application: Application
    //在Application中初始化
    fun init(application: Application) {
        FlowBusInitializer.application = application
    }
}
