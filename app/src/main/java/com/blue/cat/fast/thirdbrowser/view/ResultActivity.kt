package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.blue.cat.fast.thirdbrowser.databinding.ActivityResultBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean
import com.google.gson.Gson

class ResultActivity : AppCompatActivity() {
    val binding by lazy { ActivityResultBinding.inflate(layoutInflater) }
    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivityForResult(Intent(activity, ResultActivity::class.java),0x4454)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        clickFun()
        binding.haveConnect = BrowserKey.vpnState==2
        val connectBean = BVDataUtils.getConnectBrowserServiceBean()
        if(connectBean?.ip?.isNotEmpty() == true){
            binding.imgFast.setImageResource(BVDataUtils.getImageFlag(connectBean.country))
            binding.tvServiceName.text = connectBean.country
        }
    }
    private fun clickFun(){
        binding.imgFinish.setOnClickListener {
            finish()
        }
    }
}
