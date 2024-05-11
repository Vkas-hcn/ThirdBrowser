package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blue.cat.fast.thirdbrowser.databinding.ActivityHistoryBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.view.ad.FieryAdMob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    val binding by lazy { ActivityHistoryBinding.inflate(layoutInflater) }
    private lateinit var allHistoryBeanData: MutableList<BrowserDataBean>
    private lateinit var adapter: BrowserDataAdapter
    private var deleteHistoryJob: Job? = null
    private var showDeleteHistoryAdLive = MutableLiveData<Any>()
    private var posNum = -1
    private var showBackMarkAdLive = MutableLiveData<Any>()
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
        FieryAdMob.loadOf(BrowserKey.Fiery_ADD_INT)
        FieryAdMob.loadOf(BrowserKey.Fiery_BACK_INT)
        binding.imgFinish.setOnClickListener {
            loadBackAd()
        }
        binding.tvDeleteAll.setOnClickListener {
            deleteAllHistory()
        }
        showDeleteHistoryAdLive.observe(this) {
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

    fun onCLickHistory(v: View) {}
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
                    posNum = position
                    loadAddAd()
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

    private fun deleteAllHistory() {
        posNum = -1
        AlertDialog.Builder(this)
            .setTitle("Delete all history")
            .setMessage("Are you sure you want to delete all history?")
            .setPositiveButton("Yes") { _, _ ->
                loadAddAd()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun loadAddAd() {

        deleteHistoryJob?.cancel()
        deleteHistoryJob = null
        var isJump = true
        deleteHistoryJob = lifecycleScope.launch {
            FieryAdMob.loadOf(BrowserKey.Fiery_ADD_INT)
            binding.haveLoading = true
            if (posNum == -1) {
                delay(2000)
            } else {
                delay(1000)
            }
            if(posNum != -1 && BVDataUtils.showAdBlacklist()){
                binding.haveLoading = false
                deleteHistoryData()
                deleteHistoryJob?.cancel()
                deleteHistoryJob = null
                return@launch
            }
            BrowserKey.deleteHistoryNum = BrowserKey.deleteHistoryNum+1

            try {
                withTimeout(4000) {
                    while (isActive) {
                        if (!BVDataUtils.getIsCanShowAd(1)) {
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
                                    showDeleteHistoryAdLive.postValue(it)
                                }
                            break
                        }
                        delay(500)
                    }
                }
            } finally {
                if (isJump) {
                    binding.haveLoading = false
                    deleteHistoryData()
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
                    deleteHistoryData()
                }
            }
        )
    }

    private fun deleteHistoryData() {
        if (posNum < 0) {
            deleteAllFun()
        } else {
            adapter.deleteData(posNum, true)
            binding.haveData = allHistoryBeanData.isEmpty()
        }
    }

    private fun deleteAllFun() {
        BVDataUtils.clearWebPageHistory()
        allHistoryBeanData.clear()
        adapter.notifyDataSetChanged()
        binding.haveData = true
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
