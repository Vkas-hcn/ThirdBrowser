package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.BuildConfig
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.NetUtils
import com.blue.cat.fast.thirdbrowser.view.ad.FieryAdMob
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

class GuideActivity : AppCompatActivity() {
    val binding by lazy { ActivityGuideBinding.inflate(layoutInflater) }
    var haveHotType = false
    var onlineJob: Job? = null
    var openJob: Job? = null
    var skipToTheNextPage = MutableLiveData<Boolean>()
    var showOpenAdLive = MutableLiveData<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        haveHotType = intent.getBooleanExtra("haveHot", false)
        BrowserKey.isAppGreenSameDayGreen()
        getOnlineData()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                NetUtils.getOnLineServiceData()
                NetUtils.getIpDataInfo()
                NetUtils.getRecordNetData(this@GuideActivity)
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        skipToTheNextPage.observe(this) {
            if (it) {
                jumpToNextPage()
            }
        }
        showOpenAdLive.observe(this) {
            showOpenAd(it)
        }
    }

    private fun jumpToNextPage() {
        openJob?.cancel()
        openJob = null
        stopRotation(binding.imageLoad)
        if (!haveHotType && BrowserKey.vpnState != 2) {
            if (BrowserKey.vpn_guide_state == 2) {
                VpnActivity.startAndConnect(this@GuideActivity, BrowserKey.vpn_guide_state)
            } else {
                Guide2Activity.start(this@GuideActivity)
            }
        } else {
            finish()
        }
    }

    private fun rotateImage(imageView: ImageView, duration: Long = 2000) {
        val animator = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f).apply {
            this.duration = duration
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }
        animator.start()
        imageView.tag = animator
    }

    private fun stopRotation(imageView: ImageView) {
        val animator = imageView.tag as? ObjectAnimator
        animator?.cancel()
    }


    private fun getOnlineData() {
        onlineJob?.cancel()
        onlineJob = null
        var isP = false
        onlineJob = lifecycleScope.launch {
            rotateImage(binding.imageLoad)
            withTimeoutOrNull(4000) {
                if (!BuildConfig.DEBUG) {
                    val auth = Firebase.remoteConfig
                    auth.fetchAndActivate().addOnSuccessListener {
                        BrowserKey.fileBase_ad_data = auth.getString(BrowserKey.online_ad_key)
                        BrowserKey.fileBase_coffe_data = auth.getString(BrowserKey.online_coffe_key)
                        isP = true
                    }
                }
                while (isP.not()) delay(400)
            }
            FieryAdMob.init(this@GuideActivity)
            loadOpenAd()
        }
    }

    private fun loadOpenAd() {
        openJob?.cancel()
        openJob = null
        var isJump = true
        openJob = lifecycleScope.launch {
            try {
                withTimeout(10000) {
                    while (isActive) {
                        if (BrowserKey.isThresholdReached() && FieryAdMob.resultOf(BrowserKey.Fiery_OPEN) == "") {
                            isJump = true
                            break
                        }
                        if (FieryAdMob.resultOf(BrowserKey.Fiery_OPEN) != null) {
                            Log.e("TAG", "loadOpenAd: show")
                            FieryAdMob.resultOf(BrowserKey.Fiery_OPEN)
                                ?.let {
                                    isJump = false
                                    showOpenAdLive.postValue(it)
                                }
                            break
                        }
                        delay(500)
                    }
                }
            } finally {
                openJob?.cancel()
                openJob = null
                Log.e("TAG", "loadOpenAd: finally1")
                if (isJump) {
                    Log.e("TAG", "loadOpenAd: finally2")
                    skipToTheNextPage.postValue(true)
                }
            }
        }
    }

    private fun showOpenAd(openData: Any) {
        FieryAdMob.showFullScreenOf(
            where = BrowserKey.Fiery_OPEN,
            context = this,
            res = openData,
            onShowCompleted = {
                if (!App.isAppInBackground) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        skipToTheNextPage.postValue(true)
                    }
                }
            }
        )
    }
}
