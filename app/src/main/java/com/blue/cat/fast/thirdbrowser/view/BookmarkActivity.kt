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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.blue.cat.fast.thirdbrowser.databinding.ActivityBookmarkBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.view.ad.FieryAdMob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import java.util.Locale

class BookmarkActivity : AppCompatActivity() {
    val binding by lazy { ActivityBookmarkBinding.inflate(layoutInflater) }
    private lateinit var allBookmarkBeanData: MutableList<BrowserDataBean>
    private lateinit var adapter: BrowserDataAdapter
    private var deleteMarkJob: Job? = null
    private var showDeleteMarkAdLive = MutableLiveData<Any>()
    private var posNum = 0


    private var showBackMarkAdLive = MutableLiveData<Any>()

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
        FieryAdMob.loadOf(BrowserKey.Fiery_ADD_INT)
        FieryAdMob.loadOf(BrowserKey.Fiery_BACK_INT)
        binding.imgFinish.setOnClickListener {
            loadBackAd()
        }
        showDeleteMarkAdLive.observe(this) {
            showAddAd(it)
        }
        showBackMarkAdLive.observe(this) {
            showBackAd(it)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                loadBackAd()
            }
        })
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
                    posNum = position
                    loadDeleteAd()
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


    private fun loadDeleteAd() {
        BrowserKey.deleteMarkNum = BrowserKey.deleteMarkNum + 1
        deleteMarkJob?.cancel()
        deleteMarkJob = null
        var isJump = true
        deleteMarkJob = lifecycleScope.launch {
            FieryAdMob.loadOf(BrowserKey.Fiery_ADD_INT)
            binding.haveLoading = true
            delay(1000)
            try {
                withTimeout(4000) {
                    while (isActive) {
                        if(BVDataUtils.showAdBlacklist()){
                            isJump = true
                            break
                        }
                        if (!BVDataUtils.getIsCanShowAd(3)) {
                            isJump = true
                            break
                        }
                        if (BrowserKey.isThresholdReached() && FieryAdMob.resultOf(BrowserKey.Fiery_ADD_INT) == "") {
                            isJump = true
                            break
                        }
                        if (FieryAdMob.resultOf(BrowserKey.Fiery_ADD_INT) != null) {
                            FieryAdMob.resultOf(BrowserKey.Fiery_ADD_INT)
                                ?.let {
                                    isJump = false
                                    showDeleteMarkAdLive.postValue(it)
                                }
                            break
                        }
                        delay(500)
                    }
                }
            } finally {
                if (isJump) {
                    binding.haveLoading = false
                    deleteMarkData()
                }
            }
        }
    }

    private fun showAddAd(addAdData: Any) {
        FieryAdMob.showFullScreenOf(
            where = BrowserKey.Fiery_ADD_INT,
            context = this,
            res = addAdData,
            preload = true,
            onShowCompleted = {
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.haveLoading = false
                    deleteMarkData()
                }
            }
        )
    }

    private fun deleteMarkData() {
        adapter.deleteData(posNum, false)
        binding.haveData = allBookmarkBeanData.isEmpty()
    }


    private fun loadBackAd() {
        if (BrowserKey.isThresholdReached() && FieryAdMob.resultOf(BrowserKey.Fiery_BACK_INT) == "") {
            finish()
            return
        }
        if (FieryAdMob.resultOf(BrowserKey.Fiery_BACK_INT) != null) {
            FieryAdMob.resultOf(BrowserKey.Fiery_BACK_INT)
                ?.let {
                    showBackMarkAdLive.postValue(it)
                }
        } else {
            finish()
        }
    }

    private fun showBackAd(addAdData: Any) {
        FieryAdMob.showFullScreenOf(
            where = BrowserKey.Fiery_BACK_INT,
            context = this,
            res = addAdData,
            preload = true,
            onShowCompleted = {
                lifecycleScope.launch(Dispatchers.Main) {
                    finish()
                }
            }
        )
    }
}
