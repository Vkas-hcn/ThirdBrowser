package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blue.cat.fast.thirdbrowser.databinding.ActivityHistoryBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    val binding by lazy { ActivityHistoryBinding.inflate(layoutInflater) }
    private lateinit var allHistoryBeanData: MutableList<BrowserDataBean>
    private lateinit var adapter: BrowserDataAdapter

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, HistoryActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initHistoryAdapter()
        editSearchFun()
        binding.imgFinish.setOnClickListener {
            finish()
        }
    }

    private fun initHistoryAdapter() {
        val data = BVDataUtils.getWebPageHistory()?.asSequence()
            ?.sortedByDescending { it.timeDate }
            ?.toMutableList()
        binding.haveData = data == null
        allHistoryBeanData = mutableListOf()
        if (data != null) {
            allHistoryBeanData = data
            adapter = BrowserDataAdapter(allHistoryBeanData)
            binding.rvHistory.adapter = adapter
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            binding.rvHistory.layoutManager = layoutManager
            adapter.setOnItemClickListener(object : BrowserDataAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val data = allHistoryBeanData[position].urlData
                    killTargetActivity()
                    MainActivity.loadWeb(this@HistoryActivity, data, true)
                }
            })
            adapter.setOnItemDeleteListener(object : BrowserDataAdapter.OnItemDeleteListener {
                override fun onItemDelete(position: Int) {
                    adapter.deleteData(position, true)
                    binding.haveData = allHistoryBeanData.isEmpty()
                }
            })
        }
    }
    fun killTargetActivity() {
        val intent = Intent("ACTION_FINISH_ACTIVITY")
        sendBroadcast(intent)
    }

    private fun editSearchFun() {
        binding.edtSearchHistory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (allHistoryBeanData.isNotEmpty()) {
                    allHistoryBeanData.forEach { all ->
                        all.haveShow = !((all.urlTitle).lowercase(Locale.getDefault()).contains(
                            s.toString()
                                .lowercase(Locale.getDefault())
                        ))
                    }
                    showNoData()
                }
            }
        })
    }

    fun showNoData() {
        adapter.notifyDataSetChanged()
        var type = false
        allHistoryBeanData.forEach {
            if (!it.haveShow) {
                type = true
            }
        }
        binding.haveData = !type
    }
}
