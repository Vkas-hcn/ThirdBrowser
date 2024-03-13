package com.blue.cat.fast.thirdbrowser.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityGuideBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import com.blue.cat.fast.thirdbrowser.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    val binding by lazy { ActivityResultBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        clickFun()
    }
    private fun clickFun(){
        binding.imgFinish.setOnClickListener {
            finish()
        }
    }
}
