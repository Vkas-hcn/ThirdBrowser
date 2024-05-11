package com.blue.cat.fast.thirdbrowser.utils.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FieryAdBean(
    @SerializedName("koper")
    val `open`: List<DetailAdBean>,
    @SerializedName("tana")
    val addInt: List<DetailAdBean>,
    @SerializedName("zuibbt")
    val connectInt: List<DetailAdBean>,
    @SerializedName("ruten")
    val backInt: List<DetailAdBean>,
    val ccc: Int,
    val sss: Int
)

@Keep
data class DetailAdBean(
    val lv_fiery: String,
    val te_fiery: String,
    val u_fiery: String,
    val w_fiery: String
)