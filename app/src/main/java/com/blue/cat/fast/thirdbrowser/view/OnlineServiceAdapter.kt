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
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean

class OnlineServiceAdapter(private var dataList: MutableList<BrowserServiceBean>) :
    RecyclerView.Adapter<OnlineServiceAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFlag: ImageView = itemView.findViewById(R.id.img_flag)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val imgCheck: ImageView = itemView.findViewById(R.id.img_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view =
            inflater.inflate(R.layout.item_vpn, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        setVisibility(data.haveShow, holder.itemView)
        holder.imgFlag.setImageResource(BVDataUtils.getImageFlag(data.country))
        holder.tvName.text = if (data.bestService) {
            data.country
        } else {
            String.format("${data.country}-${data.city}")
        }
        if (data.isCheckThis && BrowserKey.vpnState == 2) {
            holder.imgCheck.setImageResource(R.drawable.icon_check)
        } else {
            holder.imgCheck.setImageResource(R.drawable.icon_no_check)
        }
        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
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