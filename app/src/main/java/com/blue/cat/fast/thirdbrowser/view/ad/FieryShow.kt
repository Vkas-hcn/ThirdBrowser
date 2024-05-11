package com.blue.cat.fast.thirdbrowser.view.ad

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.blue.cat.fast.thirdbrowser.App


 class FieryShow private constructor(private val where: String) {
    companion object {
        private var isShowingFullScreen = false

        fun of(where: String): FieryShow {
            return FieryShow(where)
        }

    }

    fun showFullScreen(
        activity: AppCompatActivity,
        res: Any,
        callback: () -> Unit
    ) {
        if (isShowingFullScreen || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            callback()
            return
        }
        isShowingFullScreen = true
        Log.e(App.TAG, "showFullScreen: ")
        FieryGoogleAds(where)
            .showFullScreen(
                context = activity,
                res = res,
                callback = {
                    isShowingFullScreen = false
                    callback()
                }
            )
    }
}
