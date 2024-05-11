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
    var isLoadOpenFist = false

    fun init(context: Context) {
        GoogleAds.init(context) {
            preloadAds()
        }
    }

    fun loadOf(where: String) {
        Load.of(where)?.load()
    }

    fun resultOf(where: String): Any? {
        return Load.of(where)?.res
    }

    fun showFullScreenOf(
        where: String,
        context: AppCompatActivity,
        res: Any,
        preload: Boolean = false,
        onShowCompleted: () -> Unit
    ) {
        Show.of(where)
            .showFullScreen(
                activity = context,
                res = res,
                callback = {
                    Load.of(where)?.let { load ->
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
            Load.of(BrowserKey.Fiery_OPEN)?.load()
            Load.of(BrowserKey.Fiery_CONNECT_INT)?.load()
        }
    }

    private class Load private constructor(private val where: String) {
        companion object {
            private val open by lazy { Load(BrowserKey.Fiery_OPEN) }
            private val addInt by lazy { Load(BrowserKey.Fiery_ADD_INT) }
            private val connectInt by lazy { Load(BrowserKey.Fiery_CONNECT_INT) }
            private val backInt by lazy { Load(BrowserKey.Fiery_BACK_INT) }
            fun of(where: String): Load? {
                return when (where) {
                    BrowserKey.Fiery_OPEN -> open
                    BrowserKey.Fiery_ADD_INT -> addInt
                    BrowserKey.Fiery_CONNECT_INT -> connectInt
                    BrowserKey.Fiery_BACK_INT -> backInt
                    else -> null
                }
            }

        }


        private var createdTime = 0L
        var res: Any? = null
            set
        var isLoading = false
            set

        private fun printLog(content: String) {
            Log.d(TAG, "${where} ---${content}: ")
        }

        fun load(
            context: Context = App.instance,
            requestCount: Int = 1,
            inst: FieryAdBean = BVDataUtils.getAdJson(),
        ) {
            BrowserKey.isAppGreenSameDayGreen()
            if (isLoading) {
                printLog("is requesting")
                return
            }

            val cache = res
            val cacheTime = createdTime

            if (cache != null && cacheTime > 0L && (System.currentTimeMillis() - cacheTime) <= (1000L * 60L * 60L)) {
                printLog("Existing cache")
                return
            }

            if (cache == null || cache == "") {
                if (BrowserKey.isThresholdReached()) {
                    printLog("The ad reaches the go-live")
                    res = ""
                    return
                }

                if ((where == BrowserKey.Fiery_BACK_INT || where == BrowserKey.Fiery_CONNECT_INT) && BVDataUtils.showAdBlacklist()) {
                    res = ""
                    return
                }
            }

            isLoading = true
            val listData = when (where) {
                BrowserKey.Fiery_OPEN -> inst.open
                BrowserKey.Fiery_CONNECT_INT -> inst.connectInt
                BrowserKey.Fiery_ADD_INT -> inst.addInt
                BrowserKey.Fiery_BACK_INT -> inst.backInt
                else -> emptyList()
            }

            val redListData = listData.sortedBy { it.lv_fiery }
            printLog("load started-data=${redListData}")

            doRequest(context, redListData) { result ->
                val isSuccessful = result != null
                printLog("load complete, result=$isSuccessful")

                if (isSuccessful) {
                    res = result
                    createdTime = System.currentTimeMillis()
                }

                isLoading = false

                if (!isSuccessful && where == BrowserKey.Fiery_OPEN && requestCount < 2) {
                    load(context, requestCount + 1, inst)
                }
            }
        }


        private fun doRequest(
            context: Context,
            units: List<DetailAdBean>,
            startIndex: Int = 0,
            callback: ((result: Any?) -> Unit)
        ) {
            val unit = units.getOrNull(startIndex)
            if (unit == null) {
                callback(null)
                return
            }
            printLog("${where},on request: $unit")
            GoogleAds(where).load(context, unit) {
                if (it == null)
                    doRequest(context, units, startIndex + 1, callback)
                else
                    callback(it)
            }
        }

        fun clearCache() {
            res = null
            createdTime = 0L
        }
    }

    private class Show private constructor(private val where: String) {
        companion object {
            private var isShowingFullScreen = false

            fun of(where: String): Show {
                return Show(where)
            }

        }

        fun showFullScreen(
            activity: AppCompatActivity,
            res: Any,
            callback: () -> Unit
        ) {
            if (isShowingFullScreen || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                callback()
                return
            }
            isShowingFullScreen = true
            Log.e(TAG, "showFullScreen: ")
            GoogleAds(where)
                .showFullScreen(
                    context = activity,
                    res = res,
                    callback = {
                        isShowingFullScreen = false
                        callback()
                    }
                )
        }
    }

    private class GoogleAds(private val where: String) {
        private class GoogleFullScreenCallback(
            private val where: String,
            private val callback: () -> Unit
        ) : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "${where} ---dismissed")
                onAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.d(TAG, "${where} ---fail to show, message=${p0.message}")
                onAdComplete()
            }

            private fun onAdComplete() {
                callback()
            }

            override fun onAdShowedFullScreenContent() {
                BrowserKey.recordNumberOfAdDisplaysGreen()
                Log.d(TAG, "${where}--showed")

            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "${where}插屏广告点击")
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
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
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
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
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
}