package com.blue.cat.fast.thirdbrowser.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.R
import com.blue.cat.fast.thirdbrowser.utils.data.CoffeBean
import com.blue.cat.fast.thirdbrowser.utils.data.FieryAdBean
import com.github.shadowsocks.MkUtils

object BVDataUtils {

    fun getConnectBrowserServiceBean(): BrowserServiceBean? {
        val bean = runCatching {
            Gson().fromJson(
                BrowserKey.connectVpn,
                BrowserServiceBean::class.java
            )
        }.getOrNull()
        if (bean != null) {
            bean.isCheckThis = true
            return bean
        }
        val bestBean = getBestData()
        bestBean?.isCheckThis = true
        return bestBean
    }


    fun closeKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getVpnServiceList(): DataX? {
        val listData = runCatching {
            Gson().fromJson(
                BrowserKey.online_service_data,
                OnlineServiceData::class.java
            )
        }.getOrNull()
        return listData?.data
    }

    fun getAllVpnServiceList(): MutableList<BrowserServiceBean>? {
        val list: DataX? = getVpnServiceList()
        val sm = getBestData() ?: return null
        list?.nMhct?.add(0, sm)
        return list?.nMhct
    }

    suspend fun isHaveServeData(): Boolean {
        val vpnBean = runCatching {
            Gson().fromJson(
                BrowserKey.online_service_data,
                OnlineServiceData::class.java
            )
        }.getOrNull()
        return if (vpnBean == null || vpnBean.data.bwsJ.size <= 0) {
            NetUtils.getOnLineServiceData()
            false
        }else{
            if(getConnectBrowserServiceBean()==null){
                BrowserKey.connectVpn = Gson().toJson(getBestData())
            }
            true
        }
    }

    private fun getBestData(): BrowserServiceBean? {
        val vpnBean = getVpnServiceList()?.bwsJ ?: return null
        if (vpnBean.isNotEmpty()) {
            val bean = vpnBean.random()
            bean.bestService = true
            bean.country = "Fast Server"
            return bean
        }
        return null
    }

    fun getWebPageHistory(): MutableList<BrowserDataBean>? {
        val historyData = BrowserKey.history_data_browser
        return if (historyData.isNotEmpty()) {
            Gson().fromJson(historyData, object : TypeToken<MutableList<BrowserDataBean>>() {}.type)
        } else {
            null
        }
    }

    fun saveWebPageHistory(bean: BrowserDataBean) {
        val data = getWebPageHistory()
        if (data != null) {
            data.add(bean)
            BrowserKey.history_data_browser = Gson().toJson(data)
        } else {
            val list = mutableListOf<BrowserDataBean>()
            list.add(bean)
            BrowserKey.history_data_browser = Gson().toJson(list)
        }
    }

    fun deleteWebPageHistory(bean: BrowserDataBean) {
        val data = getWebPageHistory()?.toMutableList()
        data?.removeIf { it.timeDate == bean.timeDate }
        BrowserKey.history_data_browser = Gson().toJson(data)
    }
    fun clearWebPageHistory() {
        BrowserKey.history_data_browser = ""
    }
    fun clearWebPageBookmark() {
        BrowserKey.bookmark_data_browser = ""
    }

    fun getBookmarkList(): MutableList<BrowserDataBean>? {
        val historyData = BrowserKey.bookmark_data_browser
        return if (historyData.isNotEmpty()) {
            Gson().fromJson(historyData, object : TypeToken<MutableList<BrowserDataBean>>() {}.type)
        } else {
            null
        }
    }

    fun saveWebPageBookmark(bean: BrowserDataBean) {
        val data = getBookmarkList()
        if (data != null) {
            data.add(bean)
            BrowserKey.bookmark_data_browser = Gson().toJson(data)
        } else {
            val list = mutableListOf<BrowserDataBean>()
            list.add(bean)
            BrowserKey.bookmark_data_browser = Gson().toJson(list)
        }
    }


    fun deleteWebPageBookmark(bean: BrowserDataBean) {
        val data = getBookmarkList()?.toMutableList()
        data?.removeIf { it.timeDate == bean.timeDate }
        BrowserKey.bookmark_data_browser = Gson().toJson(data)
    }

    fun getCurrentTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(Date())
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            // for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }


    val flagImageArray = listOf(
        R.drawable.icon_australia,
        R.drawable.icon_belgium,
        R.drawable.icon_brazil,
        R.drawable.icon_canada,
        R.drawable.icon_france,
        R.drawable.icon_germany,
        R.drawable.icon_hongkong,
        R.drawable.icon_india,
        R.drawable.icon_ireland,
        R.drawable.icon_italy,
        R.drawable.icon_japan,
        R.drawable.icon_koreasouth,
        R.drawable.icon_netherlands,
        R.drawable.icon_newzealand,
        R.drawable.icon_norway,
        R.drawable.icon_russianfederation,
        R.drawable.icon_singapore,
        R.drawable.icon_sweden,
        R.drawable.icon_switzerland,
        R.drawable.icon_taiwang,
        R.drawable.icon_unitedarabemirates,
        R.drawable.icon_unitedkingdom,
        R.drawable.icon_unitedstates,
    )

    val flagNameArray = listOf(
        "Australia",
        "Belgium",
        "Brazil",
        "Canada",
        "France",
        "Germany",
        "HongKong",
        "India",
        "Ireland",
        "Italy",
        "Japan",
        "KoreaSouth",
        "Netherlands",
        "NewZeaLand",
        "Norway",
        "Russianfederation",
        "Singapore",
        "Sweden",
        "Switzerland",
        "Taiwan",
        "Unitedarabemirates",
        "United Kingdom",
        "United States",
    )


    fun getImageFlag(name: String): Int {
        flagNameArray.forEachIndexed { index, s ->
            if (s.equals(name, true)) {
                return flagImageArray[index]
            }
        }
        return R.drawable.icon_fast
    }

    private var lastExecutionTime = 0L

    fun executeWithDebounce(debounceTime: Long = 1000L, action: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastExecutionTime > debounceTime) {
            action()
            lastExecutionTime = currentTime
        }
    }


    fun getAdJson(): FieryAdBean {
        val dataJson = BrowserKey.fileBase_ad_data.let {
            it.ifEmpty {
                BrowserKey.loadJSONFromAsset(App.instance,BrowserKey.fiery_ad_data)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<FieryAdBean>() {}.type)
        } catch (e: Exception) {
            Gson().fromJson(
                BrowserKey.loadJSONFromAsset(App.instance,BrowserKey.fiery_ad_data),
                object : TypeToken<FieryAdBean>() {}.type
            )
        }
    }
    private fun getCoffeJson(): CoffeBean {
        val dataJson = BrowserKey.fileBase_coffe_data.let {
            it.ifEmpty {
                BrowserKey.loadJSONFromAsset(App.instance,BrowserKey.coffe)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<CoffeBean>() {}.type)
        } catch (e: Exception) {
            Gson().fromJson(
                BrowserKey.loadJSONFromAsset(App.instance,BrowserKey.coffe),
                object : TypeToken<CoffeBean>() {}.type
            )
        }
    }
    //获取间隔次数(0:搜索x次、1:删除x条记录、2:添加x条书签、3:删除x条书签 )
    private fun getAdInterval(index:Int): Int {
        val numArray = getCoffeJson().act.split("#").map { it.toInt() }
        return numArray[index]
    }

    fun getIsCanShowAd(index:Int): Boolean {
        val numArray = if(getAdInterval(index)<=0) 0 else getAdInterval(index)
        return when (index) {
            0 -> {
                BrowserKey.serachNum%(numArray+1) == 0
            }
            1 -> {
                BrowserKey.deleteHistoryNum%(numArray+1) == 0
            }
            2 -> {
                BrowserKey.addMarkNum % (numArray+1) == 0
            }
            3 -> {
                BrowserKey.deleteMarkNum % (numArray+1) == 0
            }
            else -> {
                BrowserKey.addMarkNum % (numArray+1) == 0
            }
        }
    }
    fun showAdBlacklist(): Boolean {
        val blackData = BrowserKey.black_data_browser != "zorn"
        return when (getCoffeJson().sto) {
            "1" -> {
                blackData
            }

            "2" -> {
                false
            }

            else -> {
                false
            }
        }
    }
    fun rLOr() {
        BrowserKey.rl_data_fiery =  when (getCoffeJson().vis) {
            "1" -> {
                 true
            }

            "2" -> {
                 false
            }

            "3" -> {
                 showAdBlacklist()
            }

            else -> {
                 true
            }
        }
        Log.e("TAG", "getAroundFlowJsonData-rLOr: ${BrowserKey.rl_data_fiery}")
    }
}