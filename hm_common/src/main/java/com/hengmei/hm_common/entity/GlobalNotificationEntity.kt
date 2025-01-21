package com.hengmei.hm_common.entity

class GlobalNotificationEntity {

    var tag = 0 // 1，标题更新 2，重启首页界面
    var value = ""

    constructor(tag: Int, value: String) {
        this.tag = tag
        this.value = value
    }
}