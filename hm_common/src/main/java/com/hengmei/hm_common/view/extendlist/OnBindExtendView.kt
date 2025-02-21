package com.hengmei.hm_common.view.extendlist

import android.view.View

/**
 * @ClassName OnBindExtendView
 * @Description TODO
 * @Author thy
 * @Date 2025/2/20
 */
abstract class OnBindExtendView(layoutResId: Int) {

    private var layoutResId = 0
    init {
        this.layoutResId = layoutResId
    }

    fun getLayoutResId() : Int {
        return layoutResId
    }
    abstract fun onViewBind(view : View, extendListData: ExtendListData)

    abstract fun onViewClick(view: View, extendListData: ExtendListData)

}