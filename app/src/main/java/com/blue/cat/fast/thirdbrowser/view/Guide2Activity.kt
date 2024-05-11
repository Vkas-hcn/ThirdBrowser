package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuide2Binding
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.NetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Guide2Activity : AppCompatActivity() {
    val binding by lazy { ActivityGuide2Binding.inflate(layoutInflater) }

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, Guide2Activity::class.java))
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        binding.tvConnect.setOnClickListener {
            BrowserKey.vpn_guide_state = 1
            VpnActivity.startAndConnect(this, BrowserKey.vpn_guide_state)
        }
        binding.tvSubsequent.setOnClickListener {
            BrowserKey.vpn_guide_state = 2
            VpnActivity.startAndConnect(this, BrowserKey.vpn_guide_state)
        }
        binding.tvNoAcceleration.setOnClickListener {
            MainActivity.start(this)
        }
    }

}
