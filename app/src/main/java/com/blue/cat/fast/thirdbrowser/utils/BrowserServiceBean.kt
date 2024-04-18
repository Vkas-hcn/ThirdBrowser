package com.blue.cat.fast.thirdbrowser.utils

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
data class BrowserServiceBean(
    @SerializedName("DMMaWWQiN")
    var ip:String,
    @SerializedName("ifeht")
    var proxyPort:Int,
    @SerializedName("gEorZ")
    var method:String,
    @SerializedName("Svc")
    var password:String,
    @SerializedName("OQvlTcld")
    var city:String,
    @SerializedName("bQbRKSfxy")
    var country:String,
    var bestService:Boolean,
    var isCheckThis:Boolean,
    var haveShow:Boolean,
)
@Keep
data class OnlineServiceData(
    val code: Int,
    val `data`: DataX,
    val msg: String
)

@Keep
data class DataX(
    val bwsJ: MutableList<BrowserServiceBean>,
    val nMhct: MutableList<BrowserServiceBean>
)

