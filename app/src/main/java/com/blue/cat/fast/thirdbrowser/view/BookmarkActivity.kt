package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blue.cat.fast.thirdbrowser.databinding.ActivityBookmarkBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean
import java.util.Locale

class BookmarkActivity : AppCompatActivity() {
    val binding by lazy { ActivityBookmarkBinding.inflate(layoutInflater) }
    private lateinit var allBookmarkBeanData: MutableList<BrowserDataBean>
    private lateinit var adapter: BrowserDataAdapter

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, BookmarkActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initBookmarkAdapter()
        editSearchFun()
        binding.imgFinish.setOnClickListener {
            finish()
        }
    }

    private fun initBookmarkAdapter() {
        val data = BVDataUtils.getBookmarkList()?.asSequence()
            ?.sortedByDescending { it.timeDate }
            ?.toMutableList()
        binding.haveData = data == null
        allBookmarkBeanData = mutableListOf()
        if (data != null) {
            allBookmarkBeanData = data
            adapter = BrowserDataAdapter(allBookmarkBeanData)
            binding.rvBookmark.adapter = adapter
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            binding.rvBookmark.layoutManager = layoutManager
            adapter.setOnItemClickListener(object : BrowserDataAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val data = allBookmarkBeanData[position].urlData
                    killTargetActivity()
                    MainActivity.loadWeb(this@BookmarkActivity, data, false)
                }
            })
            adapter.setOnItemDeleteListener(object : BrowserDataAdapter.OnItemDeleteListener {
                override fun onItemDelete(position: Int) {
                    adapter.deleteData(position, false)
                    binding.haveData = allBookmarkBeanData.isEmpty()
                }
            })
        }
    }
    fun killTargetActivity() {
        val intent = Intent("ACTION_FINISH_ACTIVITY")
        sendBroadcast(intent)
    }
    private fun editSearchFun() {
        binding.edtSearchBookmark.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (allBookmarkBeanData.isNotEmpty()) {
                    allBookmarkBeanData.forEach { all ->
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
        allBookmarkBeanData.forEach {
            if (!it.haveShow) {
                type = true
            }
        }
        binding.haveData = !type
    }
}
