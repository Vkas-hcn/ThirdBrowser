package com.blue.cat.fast.thirdbrowser.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date

object BVDataUtils {

    fun getConnectBrowserServiceBean(): BrowserServiceBean {
        return BrowserServiceBean(
            "185.53.211.169",
            9644,
            "chacha20-ietf-poly1305",
            " j9xKDJd71urEPbgylc62BZH",
            "London",
            "UK",
            true
        )
    }


    fun closeKeyboard(view: View, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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
}