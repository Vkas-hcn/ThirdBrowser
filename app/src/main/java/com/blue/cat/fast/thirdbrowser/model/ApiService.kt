package com.blue.cat.fast.thirdbrowser.model

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url
interface ApiService {
    @GET
    fun getIPOneData(@Url url: String): Call<Any>


    @GET("awash/church")
    suspend fun getRecordNetData(@QueryMap(encoded = true) params: Map<String, String>): Response<String>
    @POST
    fun postDynamicUrl(@Url url: String, @Body body: RequestBody): Call<ResponseBody>


}