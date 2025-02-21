package com.hengmei.hm_common.view.extendlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hengmei.hm_common.R

/**
 * @ClassName ExtendViewGroupAdapter
 * @Description TODO
 * @Author thy
 * @Date 2025/2/20
 */
class ExtendViewGroupAdapter(layoutResId : Int, data : MutableList<ExtendListData>, private val hashAllLevelData : HashMap<String, List<ExtendListData>>) : BaseQuickAdapter<ExtendListData, BaseViewHolder>(layoutResId, data) {
    private var removeSize = 0
    @SuppressLint("NotifyDataSetChanged")
    override fun convert(holder: BaseViewHolder, item: ExtendListData) {

        val llItem = holder.getView<LinearLayout>(R.id.ll_item)
        val llContent = holder.getView<LinearLayout>(R.id.ll_content)
        val llDiyContent = holder.getView<LinearLayout>(R.id.ll_diy_content)
        val ivExpend = holder.getView<ImageView>(R.id.iv_expend)
        val tvName = holder.getView<TextView>(R.id.tv_name)

        val onBindExtendView = item.onBindExtendView
        var bingView : View? = null

        llDiyContent.removeAllViews()

        if (onBindExtendView == null) {
            llContent.visibility = View.VISIBLE
            llDiyContent.visibility = View.GONE
            tvName.text = item.name
            val params = tvName.layoutParams as LinearLayout.LayoutParams
            params.leftMargin = item.level * 30
            tvName.layoutParams = params
            if (item.isExtend) {
                ivExpend.setImageResource(R.mipmap.icon_expend)
            } else {
                ivExpend.setImageResource(R.mipmap.icon_un_expend)
            }
        } else {
            llContent.visibility = View.GONE
            llDiyContent.visibility = View.VISIBLE
            bingView = LayoutInflater.from(context)
                .inflate(onBindExtendView.getLayoutResId(), llContent, false)
            onBindExtendView.onViewBind(bingView, item)
            llDiyContent.addView(bingView)
        }

        llItem.setOnClickListener {

            if (!item.isExtend) {//展开  只需展开依赖于当前级别的低一级别数据
                item.isExtend = true
                onBindExtendView?.onViewClick(bingView!!, item)
                if (hashAllLevelData[item.idStr] != null) {
                    data.addAll(holder.layoutPosition + 1, hashAllLevelData[item.idStr]!!)
                    notifyItemRangeInserted(holder.layoutPosition + 1, hashAllLevelData[item.idStr]!!.size)
                }
                notifyItemChanged(holder.layoutPosition)
            } else {//收起 需收起所有依赖于当前级别的低级别数据
                removeSize = 0
                collapseView(item)
//                data.removeAll(removeData)
                onBindExtendView?.onViewClick(bingView!!, item)
                data.subList(holder.layoutPosition + 1, holder.layoutPosition + 1 + removeSize).clear()
                notifyItemRangeRemoved(holder.layoutPosition + 1, removeSize)
                notifyItemChanged(holder.layoutPosition)
            }
        }
    }

    /**
     * 递归收起依赖于当前级别的低级别数据
     */
    private fun collapseView(item: ExtendListData) {
        if (item.isExtend) {
            if (hashAllLevelData[item.idStr] != null) {//有更低一级的列表且处于展开中
                for (j in hashAllLevelData[item.idStr]!!) {
                    collapseView(j)
                }
                removeSize += hashAllLevelData[item.idStr]!!.size
            }
            item.isExtend = false
        }
    }

}