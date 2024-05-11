package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.lifecycle.MutableLiveData
import com.blue.cat.fast.thirdbrowser.databinding.ActivityResultBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean
import com.blue.cat.fast.thirdbrowser.view.ad.FieryAdMob
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers

class ResultActivity : AppCompatActivity() {
    val binding by lazy { ActivityResultBinding.inflate(layoutInflater) }
    private var showBackMarkAdLive = MutableLiveData<Any>()

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivityForResult(Intent(activity, ResultActivity::class.java), 0x4454)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        clickFun()
        binding.haveConnect = BrowserKey.vpnState == 2
        val connectBean = BVDataUtils.getConnectBrowserServiceBean()
        if (connectBean?.ip?.isNotEmpty() == true) {
            binding.imgFast.setImageResource(BVDataUtils.getImageFlag(connectBean.country))
            binding.tvServiceName.text = connectBean.country
        }

        showBackMarkAdLive.observe(this) {
            showBackAd(it)
        }
    }

    private fun clickFun() {
        binding.imgFinish.setOnClickListener {
            loadBackAd()
        }
        onBackPressedDispatcher.addCallback(this) {
            loadBackAd()
        }
    }

    private fun backFun() {
        if (BrowserKey.vpnState == 2 && BrowserKey.vpn_guide_state != 3) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    private fun loadBackAd() {
        if (BrowserKey.isThresholdReached() && FieryAdMob.resultOf(BrowserKey.Fiery_BACK_INT) == "") {
            backFun()
            return
        }
        if (FieryAdMob.resultOf(BrowserKey.Fiery_BACK_INT) != null) {
            FieryAdMob.resultOf(BrowserKey.Fiery_BACK_INT)
                ?.let {
                    showBackMarkAdLive.postValue(it)
                }
        } else {
            backFun()
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
                    backFun()
                }
            }
        )
    }
}
