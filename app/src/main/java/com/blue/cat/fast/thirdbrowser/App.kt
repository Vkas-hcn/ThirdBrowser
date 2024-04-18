package com.blue.cat.fast.thirdbrowser

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.blue.cat.fast.thirdbrowser.model.TimerViewModel
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.NetUtils
import com.blue.cat.fast.thirdbrowser.view.GuideActivity
import com.blue.cat.fast.thirdbrowser.view.VpnActivity
import com.github.shadowsocks.Core
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class App : Application() {
    companion object {
        lateinit var instance: App
        var timerText = "00:00:00"
        var viewModel: TimerViewModel? = null
        var wentToBackgroundTime: Long = 0
        var isAppInBackground = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Core.init(this, VpnActivity::class)
        registerActivityLifecycleCallbacks(AppLifecycleTracker())
        if(BrowserKey.uuid_browser.isEmpty()){
            BrowserKey.uuid_browser = UUID.randomUUID().toString()
        }
    }
    private inner class AppLifecycleTracker : ActivityLifecycleCallbacks {
        private var runningActivities = 0

        override fun onActivityStarted(activity: Activity) {
            runningActivities++
            if (isAppInBackground && runningActivities > 0) {
                isAppInBackground = false
                val currentTime = System.currentTimeMillis()
                if (currentTime - wentToBackgroundTime > 3000) {
                    val intent = Intent(activity, GuideActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }

        override fun onActivityStopped(activity: Activity) {
            runningActivities--
            if (runningActivities == 0) {
                isAppInBackground = true
                wentToBackgroundTime = System.currentTimeMillis()
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}