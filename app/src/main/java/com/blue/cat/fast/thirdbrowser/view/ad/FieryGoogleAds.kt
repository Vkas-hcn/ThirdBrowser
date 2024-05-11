package com.blue.cat.fast.thirdbrowser.view.ad

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.data.DetailAdBean
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

 class FieryGoogleAds(private val where: String) {
    private class GoogleFullScreenCallback(
        private val where: String,
        private val callback: () -> Unit
    ) : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(App.TAG, "${where} ---dismissed")
            onAdComplete()
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            Log.d(App.TAG, "${where} ---fail to show, message=${p0.message}")
            onAdComplete()
        }

        private fun onAdComplete() {
            callback()
        }

        override fun onAdShowedFullScreenContent() {
            BrowserKey.recordNumberOfAdDisplaysGreen()
            Log.d(App.TAG, "${where}--showed")

        }

        override fun onAdClicked() {
            super.onAdClicked()
            Log.d(App.TAG, "${where}插屏广告点击")
            BrowserKey.recordNumberOfAdClickGreen()
        }
    }

    companion object {
        fun init(context: Context, onInitialized: () -> Unit) {
            MobileAds.initialize(context) {
                onInitialized()
            }
        }

    }

    fun load(
        context: Context,
        unit: DetailAdBean,
        callback: ((result: Any?) -> Unit)
    ) {

        val requestContext = context.applicationContext
        when (unit.where_fiery) {
            BrowserKey.Fiery_OPEN -> {

                AppOpenAd.load(
                    requestContext,
                    unit.unit_fiery,
                    AdRequest.Builder().build(),
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    object :
                        AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.d(App.TAG, "${where} ---request fail: ${loadAdError.message}")
                            callback(null)
                        }

                        override fun onAdLoaded(appOpenAd: AppOpenAd) {
                            callback(appOpenAd)
                        }
                    })
            }

            BrowserKey.Fiery_CONNECT_INT, BrowserKey.Fiery_ADD_INT, BrowserKey.Fiery_BACK_INT -> {

                InterstitialAd.load(
                    requestContext,
                    unit.unit_fiery,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.d(App.TAG, "${where} ---request fail: ${loadAdError.message}")
                            callback(null)
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            callback(interstitialAd)
                        }
                    }
                )
            }

            else -> {
                callback(null)
            }
        }
    }

    fun showFullScreen(
        context: AppCompatActivity,
        res: Any,
        callback: () -> Unit
    ) {
        when (res) {
            is AppOpenAd -> {
                res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                res.show(context)
            }

            is InterstitialAd -> {
                if ((where == BrowserKey.Fiery_BACK_INT || where == BrowserKey.Fiery_CONNECT_INT) && BVDataUtils.showAdBlacklist()) {
                    callback.invoke()
                    return
                }
                context.lifecycleScope.launch(Dispatchers.Main) {

                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res.show(context)
                }
            }

            else -> callback()
        }
    }
}
