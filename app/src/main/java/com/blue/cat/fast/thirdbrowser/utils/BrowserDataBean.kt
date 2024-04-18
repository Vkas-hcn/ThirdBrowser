package com.blue.cat.fast.thirdbrowser.utils

import androidx.annotation.Keep

@Keep
data class BrowserDataBean(
    var urlData:String,
    var urlTitle:String,
    var timeDate:String,
    var haveShow:Boolean
)
