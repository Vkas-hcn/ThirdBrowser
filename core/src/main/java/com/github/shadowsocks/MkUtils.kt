package com.github.shadowsocks

import android.content.Context
import android.util.Log
import android.net.VpnService
import android.text.format.Formatter
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.tencent.mmkv.MMKV

object MkUtils {
    private val mmkv by lazy {
        MMKV.mmkvWithID("fiery", MMKV.MULTI_PROCESS_MODE)
    }

    private fun getFlowData(): Boolean {
        val data = mmkv.decodeBool("rl_data_fiery", true)
        Log.e("TAG", "getAroundFlowJsonData-ss: ${data}")
        return data
    }

    fun brand(builder: VpnService.Builder, myPackageName: String) {
        if(getFlowData()){
            (listOf(myPackageName) + listGmsPackages())
                .iterator()
                .forEachRemaining {
                    runCatching { builder.addDisallowedApplication(it) }
                }
        }
    }

    private fun listGmsPackages(): List<String> {
        return listOf(
            "com.google.android.gms",
            "com.google.android.ext.services",
            "com.google.process.gservices",
            "com.android.vending",
            "com.google.android.gms.persistent",
            "com.google.android.cellbroadcastservice",
            "com.google.android.packageinstaller",
            "com.google.android.gms.location.history",
            "com.android.chrome",
        )
    }
}