package com.blue.cat.fast.thirdbrowser

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.blue.cat.fast.thirdbrowser.model.TimerViewModel
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.NetUtils
import com.blue.cat.fast.thirdbrowser.view.GuideActivity
import com.blue.cat.fast.thirdbrowser.view.VpnActivity
import com.github.shadowsocks.Core
import com.google.android.gms.ads.AdActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class App : Application() {
    var currentTime :Long = 0

    companion object {
        val TAG = "Fiery"
        lateinit var instance: App
        var timerText = "00:00:00"
        var viewModel: TimerViewModel? = null
        var wentToBackgroundTime: Long = 0
        var isAppInBackground = false
        val mmkvFiery by lazy {
            MMKV.mmkvWithID("fiery", MMKV.MULTI_PROCESS_MODE)
        }
        private val activityReferences = mutableSetOf<String>()
        fun isActivityInStack(activityName: String): Boolean {
            return activityReferences.contains(activityName)
        }
    }
    var ad_activity_smart: Activity? = null
    var top_activity_smart: Activity? = null
    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this)
        Core.init(this, VpnActivity::class)
        Firebase.initialize(this)
        FirebaseApp.initializeApp(this)
        registerActivityLifecycleCallbacks(AppLifecycleTracker())
        BrowserKey.isAppGreenSameDayGreen()
        if(BrowserKey.uuid_browser.isEmpty()){
            BrowserKey.uuid_browser = UUID.randomUUID().toString()
        }
        Core.stopService()
        BrowserKey.vpnState = -1
        BrowserKey.vpnClickState = -1
    }
    private inner class AppLifecycleTracker : ActivityLifecycleCallbacks {
        private var runningActivities = 0

        override fun onActivityStarted(activity: Activity) {
            runningActivities++
            if (isAppInBackground && runningActivities > 0) {
                isAppInBackground = false
                 currentTime = System.currentTimeMillis()
                if (currentTime - wentToBackgroundTime > 3000) {
                    ad_activity_smart?.finish()
                    if (top_activity_smart is GuideActivity) {
                        top_activity_smart?.finish()
                    }
                    val intent = Intent(activity, GuideActivity::class.java)
                    intent.putExtra("haveHot", true)
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

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activityReferences.add(activity.javaClass.name)
            if (activity !is AdActivity) {
                top_activity_smart = activity
            } else {
                ad_activity_smart = activity
            }
        }
        override fun onActivityResumed(activity: Activity) {
            if (activity !is AdActivity) {
                top_activity_smart = activity
            }
        }
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            activityReferences.remove(activity.javaClass.name)
            ad_activity_smart = null
            top_activity_smart = null
        }
    }

}