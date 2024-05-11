package com.blue.cat.fast.thirdbrowser.utils.data

import androidx.annotation.Keep

@Keep
data class FieryAdBean(
    val addInt: List<DetailAdBean>,
    val backInt: List<DetailAdBean>,
    val ccc: Int,
    val connectInt: List<DetailAdBean>,
    val `open`: List<DetailAdBean>,
    val sss: Int
)
@Keep
data class DetailAdBean(
    val lv_fiery: String,
    val type_fiery: String,
    val unit_fiery: String,
    val where_fiery: String
)