package com.blue.cat.fast.thirdbrowser.view.ad

import android.content.Context
import android.util.Log
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.data.DetailAdBean
import com.blue.cat.fast.thirdbrowser.utils.data.FieryAdBean

 class FieryLoad private constructor(private val where: String){
    companion object {
        private val open by lazy { FieryLoad(BrowserKey.Fiery_OPEN) }
        private val addInt by lazy { FieryLoad(BrowserKey.Fiery_ADD_INT) }
        private val connectInt by lazy { FieryLoad(BrowserKey.Fiery_CONNECT_INT) }
        private val backInt by lazy { FieryLoad(BrowserKey.Fiery_BACK_INT) }
        fun of(where: String): FieryLoad? {
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
        Log.d(App.TAG, "${where} ---${content}: ")
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
        FieryGoogleAds(where).load(context, unit) {
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
