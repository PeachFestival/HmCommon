package com.hengmei.hm_common.Bean

data class ApiResponse(
    val code: Int,
    val msg: String,
    val data: ApkData


) {
    override fun toString(): String {
        return "ApiResponse(code=$code, msg='$msg', data=$data)"
    }
}

data class ApkData(
    val apkOssUrl: String, // 下载地址
    val apkRevision: String, // apk 版本号
    val content:String
)