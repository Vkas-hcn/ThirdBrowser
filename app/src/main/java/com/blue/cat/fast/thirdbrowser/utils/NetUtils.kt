package com.blue.cat.fast.thirdbrowser.utils

import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import android.webkit.WebSettings
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.model.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object NetUtils {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("User-Agent", WebSettings.getDefaultUserAgent(App.instance))
                .header("QKEJ", "ZZ")
                .header("WXTP", App.instance.packageName)
                .build()
            chain.proceed(newRequest)
        }
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
    val lenientGson = GsonBuilder().setLenient().create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://example.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(lenientGson))
        .build()
    val retrofitClock = Retrofit.Builder()
        .baseUrl(BrowserKey.online_cloak_url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
     @OptIn(DelicateCoroutinesApi::class)
     fun getRecordNetData(context: Context) {
        if (BrowserKey.black_data_browser.isNotEmpty()) {
            return
        }
        val map = BrowserKey.getCloakData(context)
        val apiService = retrofitClock.create(ApiService::class.java)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getRecordNetData(map.mapValues { URLEncoder.encode(it.value, StandardCharsets.UTF_8.toString()) })
                if (response.isSuccessful && response.body() != null) {
                    BrowserKey.black_data_browser = response.body()!!
                    Timber.tag("TAG").e("blacklist results=" + BrowserKey.black_data_browser + " ")
                } else {
                    delay(10004)
                    getRecordNetData(context)
                }
            } catch (e: IOException) {
                delay(10004)
                getRecordNetData(context)
            }
        }
    }

    private fun getServiceData(url: String, onSuccess: (Any) -> Unit, onError: (String) -> Unit) {
        val apiService = retrofit.create(ApiService::class.java)
        apiService.getIPOneData(url).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("No response from server")
                } else {
                    onError("Error from server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                onError("Network error or other exception: ${t.message}")
            }
        })
    }


    private fun getVpnServiceData(
        url: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {

                val urlObj = URL(url)
                val conn = urlObj.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val customHeaders = mapOf(
                    "ITSLU" to "ZZ",
                    "IFROV" to App.instance.packageName,
                )
                for ((key, value) in customHeaders) {
                    conn.setRequestProperty(key, value)
                }
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = conn.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = reader.readText()
                    reader.close()
                    onSuccess(decodeModifiedBase64(response) ?: "")
                } else {
                    onError("Error from server: $responseCode")
                }
            } catch (e: Exception) {
                onError("Network error or other exception: ${e.message}")
            }
        }.start()
    }


    private fun postTbaData(url: String, body: String) {
        val jsonMediaType = "application/json; charset=UTF-8".toMediaTypeOrNull()
        val requestBody = body.toRequestBody(jsonMediaType)
        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.postDynamicUrl(url, requestBody)
        Timber.tag("TAG").e("postTbaData:${body}")
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.e("TAG", "da-dian-:isSuccessful")
                    } ?: { Log.e("TAG", "da-dian-: (No response from server)") }
                } else {
                    Log.e("TAG", "da-dian-:onFailure ${response.body()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TAG", "da-dian-:onFailure ${t.message ?: "Network error"}")
            }
        })
    }
    suspend fun getIpDataInfo() {
        withContext(Dispatchers.IO) {
            getServiceData("https://api.infoip.io/", onSuccess = {
                BrowserKey.ip_one = extractCountryShort(it.toString()) ?: ""
                Log.e("TAG", "getServiceData1-onSuccess: ${BrowserKey.ip_one}")
            }, onError = {
                Log.e("TAG", "getServiceData1-onError: $it")

            })
            getServiceData("https://ipinfo.io/json", onSuccess = {
                BrowserKey.ip_first = extractCountryShort2(it.toString()) ?: ""
                Log.e("TAG", "getServiceData2-onSuccess: ${BrowserKey.ip_first}")
            }, onError = {
                Log.e("TAG", "getServiceData2-onError: $it")
            })
        }
    }

    suspend fun getOnLineServiceData() {
        val time = System.currentTimeMillis()
        postTbaData(BrowserKey.online_tba_url,BrowserKey.getBuryingPointShu(App.instance,"fier1"))
        withContext(Dispatchers.IO) {
            getVpnServiceData(BrowserKey.online_service_url, onSuccess = {
                postTbaData(BrowserKey.online_tba_url,BrowserKey.getBuryingPointShu(App.instance,"fier2"))
                val finishTime =  (System.currentTimeMillis()-time)/1000
                postTbaData(BrowserKey.online_tba_url,BrowserKey.getDataBuryingPointShu(App.instance,finishTime,"fier3","time"))
                BrowserKey.online_service_data = it
                Timber.tag("TAG")
                    .e("getOnLineServiceData1-onSuccess: ${BrowserKey.online_service_data}")
            }, onError = {
                Timber.tag("TAG").e("getOnLineServiceData3-onError: $it")
            })
        }
    }

    private fun decodeModifiedBase64(input: String): String? {
        val modifiedString = input.drop(16)
        val swappedCaseString = modifiedString.map { char ->
            when {
                char.isLowerCase() -> char.toUpperCase()
                char.isUpperCase() -> char.toLowerCase()
                else -> char
            }
        }.joinToString("")
        return try {
            String(Base64.decode(swappedCaseString, Base64.DEFAULT))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun extractCountryShort(input: String): String? {
        val regex = "country_short=([A-Z]+)".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1)
    }

    private fun extractCountryShort2(input: String): String? {
        val regex = "country=([A-Z]+)".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groupValues?.get(1)
    }

    fun isIllegalIp(): Boolean {
        val one = BrowserKey.ip_one
        val first = BrowserKey.ip_first
        val locale = Locale.getDefault().language
        if (BrowserKey.ip_first.isNotEmpty()) {
            return first == "IR" || first == "CN" ||
                    first == "HK" || first == "MO"
        }
        if (BrowserKey.ip_one.isNotEmpty()) {
            return one == "IR" || one == "CN" ||
                    one == "HK" || one == "MO"
        }
        return locale == "zh" || locale == "fa"
    }


}