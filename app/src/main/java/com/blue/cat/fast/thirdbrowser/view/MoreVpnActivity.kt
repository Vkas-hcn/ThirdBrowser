package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blue.cat.fast.thirdbrowser.databinding.ActivityMoreVpnBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean
import com.blue.cat.fast.thirdbrowser.view.ad.FieryAdMob
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MoreVpnActivity : AppCompatActivity() {
    val binding by lazy { ActivityMoreVpnBinding.inflate(layoutInflater) }
    private lateinit var allServiceBeanData: MutableList<BrowserServiceBean>
    private lateinit var adapter: OnlineServiceAdapter
    private var clickDataString = ""
    private var showBackMarkAdLive = MutableLiveData<Any>()

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivityForResult(Intent(activity, MoreVpnActivity::class.java), 0x556)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        FieryAdMob.loadOf(BrowserKey.Fiery_BACK_INT)
        initVpnAdapter()
        editSearchFun()
        binding.imgFinish.setOnClickListener {
            loadBackAd()
        }
        binding.tvNo.setOnClickListener {
            binding.clDisconnect.visibility = View.GONE
        }
        binding.tvYes.setOnClickListener {
            binding.clDisconnect.visibility = View.GONE
            if (clickDataString.isNotEmpty()) {
                backToVpnConnect()
            }
        }
        BrowserKey.clickVpn = ""
        showBackMarkAdLive.observe(this) {
            showBackAd(it)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                loadBackAd()
            }
        })
    }

    private fun initVpnAdapter() {
        val data = BVDataUtils.getAllVpnServiceList()
        binding.haveData = data == null
        allServiceBeanData = mutableListOf()
        if (data != null) {
            val bean = BVDataUtils.getConnectBrowserServiceBean()
            allServiceBeanData = data
            allServiceBeanData.forEach {
                it.isCheckThis = bean?.ip == it.ip && bean.bestService == it.bestService
            }
            adapter = OnlineServiceAdapter(allServiceBeanData)
            binding.rvMoreVpn.adapter = adapter
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            binding.rvMoreVpn.layoutManager = layoutManager
            adapter.setOnItemClickListener(object : OnlineServiceAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    itemClick(allServiceBeanData[position])
                }
            })
        }
    }

    private fun editSearchFun() {
        binding.edtSearchMoreVpn.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (allServiceBeanData.isNotEmpty()) {
                    allServiceBeanData.forEach { all ->
                        all.haveShow = !((all.country+all.city).lowercase(Locale.getDefault()).contains(
                            s.toString()
                                .lowercase(Locale.getDefault())
                        ))
                    }
                    showNoData()
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showNoData() {
        adapter.notifyDataSetChanged()
        var type = false
        allServiceBeanData.forEach {
            if (!it.haveShow) {
                type = true
            }
        }
        binding.haveData = !type
    }

    fun itemClick(bean: BrowserServiceBean) {
        clickDataString = Gson().toJson(bean)
        val nowBean = Gson().fromJson(BrowserKey.connectVpn,BrowserServiceBean::class.java)
        if (BrowserKey.vpnState == 2) {
            if (nowBean.ip != bean.ip || nowBean.bestService != bean.bestService) {
                binding.clDisconnect.visibility = View.VISIBLE
            }
        } else {
            backToVpnConnect()
        }
    }

    private fun backToVpnConnect() {
        val bean = Gson().fromJson(clickDataString, BrowserServiceBean::class.java)
        allServiceBeanData.forEach {
            it.isCheckThis = bean.ip == it.ip && bean.bestService == it.bestService
        }
        BrowserKey.clickVpn = clickDataString
        finish()
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
