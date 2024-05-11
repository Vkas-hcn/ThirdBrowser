package com.blue.cat.fast.thirdbrowser.utils.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FieryAdBean(
    @SerializedName("add_int")
    val addInt: List<DetailAdBean>,
    @SerializedName("back_int")
    val backInt: List<DetailAdBean>,
    @SerializedName("connect_int")
    val connectInt: List<DetailAdBean>,
    @SerializedName("open_open")
    val `open`: List<DetailAdBean>,
    val ccc: Int,
    val sss: Int
)

@Keep
data class DetailAdBean(
    val lv_fiery: String,
    val type_fiery: String,
    val unit_fiery: String,
    val where_fiery: String
)