package com.hengmei.hm_common.event

class EventMessage {
    /**
     * 消息的key
     *
     * 100 刷新历史列表
     * 102 刷新首页布局
     * 103 荧光界面，界面基线值
     * 104 荧光界面，曲线更新
     *
     */
    var key: Int

    /**
     * 消息的主体message
     */
    var message: Any? = null
    private var messageMap: HashMap<String, Any?>? = null

    constructor(key: Int, message: Any?) {
        this.key = key
        this.message = message
    }

    constructor(key: Int) {
        this.key = key
    }

    fun put(key: String, message: Any?) {
        if (messageMap == null) {
            messageMap = HashMap<String, Any?>()
        }
        messageMap?.set(key, message)
    }

    operator fun <T> get(key: String?): Any? {
        if (messageMap != null) {
            try {
                return messageMap!![key] as Any?
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
        }
        return null
    }
}
