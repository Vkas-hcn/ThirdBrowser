package com.blue.cat.fast.thirdbrowser.view.ad

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.App.Companion.TAG
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.data.DetailAdBean
import com.blue.cat.fast.thirdbrowser.utils.data.FieryAdBean
import com.google.android.gms.ads.*
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object FieryAdMob {
    fun init(context: Context) {
        FieryGoogleAds.init(context) {
            preloadAds()
        }
    }

    fun loadOf(where: String) {
        FieryLoad.of(where)?.load()
    }

    fun resultOf(where: String): Any? {
        return FieryLoad.of(where)?.res
    }

    fun showFullScreenOf(
        where: String,
        context: AppCompatActivity,
        res: Any,
        preload: Boolean = false,
        onShowCompleted: () -> Unit
    ) {
        FieryShow.of(where)
            .showFullScreen(
                activity = context,
                res = res,
                callback = {
                    FieryLoad.of(where)?.let { load ->
                        load.clearCache()
                        if (preload) {
                            load.load()
                        }
                    }
                    onShowCompleted()
                }
            )
    }

    private fun preloadAds() {
        runCatching {
            FieryLoad.of(BrowserKey.Fiery_OPEN)?.load()
            FieryLoad.of(BrowserKey.Fiery_CONNECT_INT)?.load()
        }
    }
}