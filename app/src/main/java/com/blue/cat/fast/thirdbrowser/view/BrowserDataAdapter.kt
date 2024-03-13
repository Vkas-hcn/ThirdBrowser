package com.blue.cat.fast.thirdbrowser.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blue.cat.fast.thirdbrowser.R
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean

class BrowserDataAdapter (private var dataList: MutableList<BrowserDataBean>) :
    RecyclerView.Adapter<BrowserDataAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    interface OnItemDeleteListener {
        fun onItemDelete(position: Int)
    }
    private var listener: OnItemClickListener? = null
    private var listenerDelete: OnItemDeleteListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    fun setOnItemDeleteListener(listener: OnItemDeleteListener) {
        this.listenerDelete = listener
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle:TextView = itemView.findViewById(R.id.tv_title)
        val tvUrl:TextView = itemView.findViewById(R.id.tv_url)
        val imgDelete: ImageView = itemView.findViewById(R.id.img_delete)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view =
            inflater.inflate(R.layout.item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        setVisibility(data.haveShow, holder.itemView)
        holder.tvTitle.text = data.urlTitle
        holder.tvUrl.text = data.urlData
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
        holder.imgDelete.setOnClickListener {
            listenerDelete?.onItemDelete(position)
        }
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateData(newData: MutableList<BrowserDataBean>) {
        dataList = newData
        notifyDataSetChanged()
    }
    fun deleteData(position: Int,isHistory: Boolean) {
        if(isHistory){
            BVDataUtils.deleteWebPageHistory(dataList[position])
        }else{
            BVDataUtils.deleteWebPageBookmark(dataList[position])
        }
        dataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataList.size)
    }

    private fun setVisibility(isVisible: Boolean, itemView: View) {
        val param = itemView.layoutParams as RecyclerView.LayoutParams
        if (!isVisible) {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT
            param.width = LinearLayout.LayoutParams.MATCH_PARENT
            itemView.visibility = View.VISIBLE
        } else {
            itemView.visibility = View.GONE
            param.height = 0
            param.width = 0
        }
        itemView.layoutParams = param
    }
}