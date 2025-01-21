package com.hengmei.hm_common.utils

import android.view.View
import java.util.*


/*
*   科学防连点~
*   kevin
* */
class AntiShake {

/*    private val map: MutableMap<String, Long> = object :LinkedHashMap<String, Long>() {
        override fun removeEldestEntry(pEldest: Map.Entry<String, Long>?): Boolean {
            return size > 50
        }
    }*/

    companion object{//伴生对象是可以指定名字的，不过一般都省略掉。
    private val map: LinkedHashMap<String, Long> = object : LinkedHashMap<String, Long>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return false
        }

    }
    fun check(obj: View?): Boolean {

   /*     if(BaseApplication.isPlaySound()) {
            SoundUtil.newInstance().play(1)
        }*/
        return check(obj, 1000)
    }

    fun check(obj: View?, delayTime: Int): Boolean {
        val time = map[obj.toString()]
        return if (time == null) {
            map[obj.toString()] = System.currentTimeMillis()
            false
        } else {
            val b = System.currentTimeMillis() - time <= delayTime
            if (!b) map[obj.toString()] = System.currentTimeMillis()
            b
        }
    }}
}