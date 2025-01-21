package com.hengmei.hm_common.mmkv

object DataRepository : MMKVOwner {
  var isFirstLaunch by mmkvBool(default = true)
  var language by mmkvString(default = "zh")
  var isBan by mmkvInt(default = 0)

    var loginName by mmkvString(default = "")
    var loginPassword by mmkvString(default = "")
    var openLogin by mmkvBool(default = false)
    var openSound by mmkvBool(default = false)
    var homeTitle by mmkvString(default = "")
    var footTitle by mmkvString(default = "")
    var upUserName by mmkvString(default = "")

    var upPwd by mmkvString(default = "")
    var upPath by mmkvString(default = "http://39.98.237.174:80/control/upload/data/uploadData")
  /*
  * print TAG
  * (IntentKey.CHECKITEM)) //检测项目
   (IntentKey.COMMON_USER)) //检测人员
   (IntentKey.COMMON_UNIT)) //检测单位
   (IntentKey.COMMON_BEUNIT)) //被检单位
   (IntentKey.COMMON_QRCODE)) //二维码
   (IntentKey.COMMON_PHONE)) //联系电话
   (IntentKey.COMMON_TIME)) //检测时间
  * */
    var printTestItem by mmkvBool(default = true)
    var printTestUser by mmkvBool(default = true)
    var printTestUnit by mmkvBool(default = true)
    var printTestBeUnit by mmkvBool(default = true)
    var printTestQrCode by mmkvBool(default = true)
    var printTestPhone by mmkvBool(default = true)
    var printTestTime by mmkvBool(default = true)

    var testUser by mmkvString(default = "")
    var testUnit by mmkvString(default = "")
    var testBeUnit by mmkvString(default = "")
    var tel by mmkvString(default = "")



}