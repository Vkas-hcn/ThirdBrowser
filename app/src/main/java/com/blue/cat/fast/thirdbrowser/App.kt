package com.blue.cat.fast.thirdbrowser

import android.app.Application
import com.blue.cat.fast.thirdbrowser.view.VpnActivity
import com.github.shadowsocks.Core

class App:Application() {
    companion object{
        lateinit var instance:App

    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        Core.init(this,VpnActivity::class)
    }
}