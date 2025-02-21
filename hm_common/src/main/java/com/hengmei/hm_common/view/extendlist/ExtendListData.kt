package com.hengmei.hm_common.view.extendlist

/**
 * @ClassName ExtendListData
 * @Description TODO
 * @Author thy
 * @Date 2025/2/20
 */
data class ExtendListData(
    var level : Int = 0,//列表级别
    var name : String,//标题名称 仅展示
    var data : Any? = null,//存储其他逻辑所需数据
    var onBindExtendView: OnBindExtendView? = null,//自定义UI
    var idStr : String = "",//整个列表唯一名称 用于展开收起时查找自己管理的低级列表
    var isExtend : Boolean = false//是否展开
)
