package com.blue.cat.fast.thirdbrowser.utils

import android.content.Context
import com.blue.cat.fast.thirdbrowser.App

object BrowserKey {

    const val URL_PRIVATE = "https://developer.android.com/studio"
    val sharedPreferences =
        App.instance.getSharedPreferences("browser_dog", Context.MODE_PRIVATE)
      var vpnState = -1
          set
    var uuid_browser = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("uuid_browser", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("uuid_browser", "").toString()
    var black_data_browser = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("black_data_browser", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("black_data_browser", "").toString()

    var bookmark_data_browser = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("bookmark_data_browser", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("bookmark_data_browser", "").toString()

    var history_data_browser = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("history_data_browser", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("history_data_browser", "").toString()
}