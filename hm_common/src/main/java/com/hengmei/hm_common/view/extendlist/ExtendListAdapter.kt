package com.hengmei.hm_common.view.extendlist

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hengmei.hm_common.R

/**
 * @ClassName ExtendListAdapter
 * @Description TODO
 * @Author thy
 * @Date 2025/2/20
 */
class ExtendListAdapter(
    val context: Context,
){

    private val listAdapterData : MutableList<ExtendListData> = mutableListOf()
    private val hashAllLevelData : HashMap<String, List<ExtendListData>> = HashMap()

    fun addLevel0Data(dataList : List<ExtendListData>) : ExtendListAdapter {
        for (i in dataList) {
            i.idStr = i.name + i.hashCode()
        }
        listAdapterData.addAll(dataList)
        return this
    }

    /**
     * @param previousData 依赖的上一级列表数据
     */
    fun addLevelOtherData(previousData : ExtendListData, dataList: List<ExtendListData>, onBindExtendView: OnBindExtendView? = null) : ExtendListAdapter {
        for (i in dataList) {
            i.idStr = previousData.idStr + "_" + i.name + i.hashCode()
            if (onBindExtendView != null) {
                i.onBindExtendView = onBindExtendView
            }
        }
        hashAllLevelData[previousData.idStr] = dataList
        return this
    }

    fun build() : RecyclerView.Adapter<BaseViewHolder> {
        return ExtendViewGroupAdapter(R.layout.item_extend_group_view, listAdapterData, hashAllLevelData)
    }
}