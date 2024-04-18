package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.blue.cat.fast.thirdbrowser.utils.NetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GuideActivity : AppCompatActivity() {
    val binding by lazy { ActivityGuideBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                NetUtils.getOnLineServiceData()
                NetUtils.getIpDataInfo()
                NetUtils.getRecordNetData(this@GuideActivity)
            }
            rotateImage(binding.imageLoad)
            delay(2000)
            MainActivity.start(this@GuideActivity)
            stopRotation(binding.imageLoad)
        }
        onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
            }
        })
    }


    private fun rotateImage(imageView: ImageView, duration: Long = 2000) {
        val animator = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f).apply {
            this.duration = duration
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }
        animator.start()
        imageView.tag = animator
    }

    private fun stopRotation(imageView: ImageView) {
        val animator = imageView.tag as? ObjectAnimator
        animator?.cancel()
    }

}
