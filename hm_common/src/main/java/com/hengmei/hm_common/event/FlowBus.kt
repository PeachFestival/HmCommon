package com.hengmei.hm_common.event


import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import com.hengmei.hm_common.utils.log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlowBus : ViewModel() {
    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { FlowBus() }
    }

    //正常事件
    private val events = mutableMapOf<String, Event<*>>()

    //粘性事件
    private val stickyEvents = mutableMapOf<String, Event<*>>()

    fun with(key: String, isSticky: Boolean = false): Event<Any> {
        return with(key, Any::class.java, isSticky)
    }

    fun <T> with(eventType: Class<T>, isSticky: Boolean = false): Event<T> {
        return with(eventType.name, eventType, isSticky)
    }

    @Synchronized
    fun <T> with(key: String, type: Class<T>?, isSticky: Boolean): Event<T> {
        val flows = if (isSticky) stickyEvents else events
        if (!flows.containsKey(key)) {
            flows[key] = Event<T>(key, isSticky)
        }
        return flows[key] as Event<T>

    }


    class Event<T>(private val key: String, isSticky: Boolean) {

        // private mutable shared flow
        private val _events = MutableSharedFlow<T>(
            replay = if (isSticky) 1 else 0,
            extraBufferCapacity = Int.MAX_VALUE
        )

        // publicly exposed as read-only shared flow
        val events = _events.asSharedFlow()

        /**
         * need main thread execute
         */
        fun observeEvent(
            lifecycleOwner: LifecycleOwner,
            dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
            minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
            action: (t: T) -> Unit
        ) {
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    log("EventBus.onDestroy:remove key=$key")
                    val subscriptCount = _events.subscriptionCount.value
                    if (subscriptCount <= 0)
                        instance.events.remove(key)
                }
            })
            lifecycleOwner.lifecycleScope.launch(dispatcher) {
                lifecycleOwner.lifecycle.whenStateAtLeast(minActiveState) {
                    events.collect {
                        try {
                            action(it)
                        } catch (e: Exception) {
                            log("ker=$key , error=${e.message}")
                        }
                    }
                }
            }
        }

        /**
         * send value
         */
        suspend fun setValue(
            event: T,
            dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
        ) {
            withContext(dispatcher) {
                _events.emit(event)
            }

        }
    }
}
