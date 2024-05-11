package com.blue.cat.fast.thirdbrowser.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils.getAdJson
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BrowserKey {

    const val URL_PRIVATE = "https://developer.android.com/studio"
    const val online_service_url = "https://test.securefierybrowser.com/ZrzX/qOE/"
    const val online_tba_url = "https://test-grace.securefierybrowser.com/goethe/polytope"
    const val online_cloak_url = "https://level.securefierybrowser.com/"
    const val Fiery_OPEN = "open"
    const val Fiery_ADD_INT = "addInt"
    const val Fiery_CONNECT_INT = "connectInt"
    const val Fiery_BACK_INT = "backInt"
    const val online_ad_key = "online_ad_key"
    const val online_coffe_key = "coffe"

    var addMarkNum = 0
    var deleteMarkNum = 0
    var deleteHistoryNum = 0
    var serachNum = 0
    private val sharedPreferences: SharedPreferences =
        App.instance.getSharedPreferences("browser_dog", Context.MODE_PRIVATE)
    var vpnState = -1
        set(value) {
            sharedPreferences.edit().run {
                putInt("vpnState", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("vpnState", -1)

    var vpnClickState = -1
        set(value) {
            sharedPreferences.edit().run {
                putInt("vpnClickState", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("vpnClickState", -1)
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

    var connectVpn = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("connectVpn", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("connectVpn", "").toString()

    var clickVpn = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("clickVpn", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("clickVpn", "").toString()
    var ip_one = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_one", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_one", "").toString()
    var ip_first = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_first", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_first", "").toString()

    var online_service_data = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("online_service_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("online_service_data", "").toString()

    var vpn_guide_state = 0
        set(value) {
            sharedPreferences.edit().run {
                putInt("vpn_guide_state", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("vpn_guide_state", 0)

    var fileBase_ad_data = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("fileBase_ad_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("fileBase_ad_data", "").toString()
    var fileBase_coffe_data = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("fileBase_coffe_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("fileBase_coffe_data", "").toString()

    var local_click_data = 0
        set(value) {
            sharedPreferences.edit().run {
                putInt("local_click_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("local_click_data", 0)

    var local_show_data = 0
        set(value) {
            sharedPreferences.edit().run {
                putInt("local_show_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("local_show_data", 0)

    var current_date_fiery = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("current_date_fiery", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("current_date_fiery", "").toString()


    var rl_data_fiery = false
        set(value) {
            sharedPreferences.edit().run {
                putBoolean("rl_data_fiery", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getBoolean("rl_data_fiery", false)
    fun getCloakData(context: Context): Map<String, String> {
        return mapOf(
            "ir" to uuid_browser, // distinct_id
            "mall" to System.currentTimeMillis().toString(), // client_ts
            "aphasia" to Build.MODEL,//device_model
            "casey" to "com.secure.fierybrowser.unlimited",// bundle_id
            "pleasant" to Build.VERSION.RELEASE, // os_version
            "glycogen" to "", // gaid
            "whether" to Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ), // android_id
            "grill" to "cache", // os
            "campus" to context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName, // app_version
        )
    }

    private fun getTopLevelJsonData(context: Context): JSONObject {
        val jsonData = JSONObject()
        val spoon = JSONObject()
        spoon.apply {
            //bundle_id
            put("casey", "com.secure.fierybrowser.unlimited")
            //distinct_id
            put("ir", uuid_browser)
            //manufacturer
            put("laudanum", Build.MANUFACTURER.toLowerCase())
            //system_language
            put(
                "teenage",
                "${Locale.getDefault().language}_${Locale.getDefault().country}"
            )
            //client_ts
            put("mall", System.currentTimeMillis())
        }

        val bangkok = JSONObject()
        bangkok.apply {
            //os_version
            put("pleasant", Build.VERSION.RELEASE)
            //device_model
            put("aphasia", Build.MODEL)
        }
        val highball = JSONObject()
        highball.apply {
            //operator
            put(
                "hither",
                ""
            )
        }

        val galena = JSONObject()
        galena.apply {
            //app_version
            put("campus", context.packageManager.getPackageInfo(context.packageName, 0).versionName)
            //log_id
            put("saute", UUID.randomUUID().toString())
            //os
            put("grill", "cache")

            //android_id
            put(
                "whether",
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            )
        }
        jsonData.apply {
            put("spoon", spoon)
            put("bangkok", bangkok)
            put("highball", highball)
            put("galena", galena)
        }
        return jsonData
    }

    fun getBuryingPointShu(context: Context, name: String): String {
        return getTopLevelJsonData(context).apply {
            put("dragoon", name)
        }.toString()
    }

    fun getDataBuryingPointShu(
        context: Context,
        time: Any,
        name: String,
        parameterName: String
    ): String {
        val data = JSONObject()
        data.put(parameterName, time)
        return getTopLevelJsonData(context).apply {
            put("dragoon", name)
            put("dilemma", data)
        }.toString()
    }

    fun isThresholdReached(): Boolean {
        return overrunType() != ""
    }

    private fun overrunType(): String {
        if (local_click_data >= getAdJson().ccc) {
            return "click"
        }
        if (local_show_data >= getAdJson().sss) {
            return "show"
        }
        if (local_click_data >= getAdJson().ccc && local_show_data >= getAdJson().sss) {
            return "click&show"
        }
        return ""
    }

    fun recordNumberOfAdDisplaysGreen() {
        var showCount = local_show_data
        showCount++
        local_show_data = showCount
    }

    fun recordNumberOfAdClickGreen() {
        var clicksCount = local_click_data
        clicksCount++
        local_click_data = clicksCount
    }

    fun isAppGreenSameDayGreen() {
        if (current_date_fiery == "") {
            current_date_fiery = formatDateNow()
        } else {
            if (dateAfterDate(current_date_fiery, formatDateNow())) {
                current_date_fiery = formatDateNow()
                local_click_data = 0
                local_show_data = 0
            }
        }
    }

    private fun formatDateNow(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return simpleDateFormat.format(date)
    }

    private fun dateAfterDate(startTime: String?, endTime: String?): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd")
        try {
            val startDate: Date = format.parse(startTime)
            val endDate: Date = format.parse(endTime)
            val start: Long = startDate.getTime()
            val end: Long = endDate.getTime()
            if (end > start) {
                return true
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            return false
        }
        return false
    }

    const val fiery_ad_data = "fiery_ad_data.json"
    const val coffe = "coffe.json"



    fun loadJSONFromAsset(context: Context, fileName: String): String? {
        var json: String? = null
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}