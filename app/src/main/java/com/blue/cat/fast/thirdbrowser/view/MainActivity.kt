package com.blue.cat.fast.thirdbrowser.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import com.blue.cat.fast.thirdbrowser.R
import com.blue.cat.fast.thirdbrowser.databinding.ActivityMainBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var loadUrl = ""
    private var webTitle: String = ""
    private var webUrl: String = ""

    companion object {
        private var isHistory = false
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }

        fun loadWeb(activity: AppCompatActivity, url: String, isHistoryType: Boolean) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra("url", url)
            activity.startActivity(intent)
            activity.finish()
            isHistory = isHistoryType
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadUrl = intent.getStringExtra("url") ?: ""
        clickFun()
        initWeb()
        initEditText()
        onBackFun()
    }

    private fun onBackFun() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.showWeb == true) {
                binding.showWeb = false
            } else {
                finish()
            }
        }
    }

    private fun clickFun() {
        binding.constraintLayout.setOnClickListener {
            VpnActivity.start(this)
        }
        binding.tvInstagram.setOnClickListener {
            binding.showWeb = true
            binding.homeWeb.loadUrl("https://www.instagram.com")
        }
        binding.tvVimor.setOnClickListener {
            binding.showWeb = true
            binding.homeWeb.loadUrl("https://www.vimor.com")
        }
        binding.tvFb.setOnClickListener {
            binding.showWeb = true
            binding.homeWeb.loadUrl("https://www.facebook.com")
        }
        binding.tvTiktok.setOnClickListener {
            binding.showWeb = true
            binding.homeWeb.loadUrl("https://www.tiktok.com")
        }
        binding.imgLeft.setOnClickListener {
            goBack()
        }
        binding.imgRight.setOnClickListener {
            goForward()
        }
        binding.imgHome.setOnClickListener {
            binding.showWeb = false
        }
        binding.imgMenu.setOnClickListener {
            binding.showMenu = true
        }
        binding.viewMenuBg.setOnClickListener {
            binding.showMenu = false
        }
        binding.tvReload.setOnClickListener {
            binding.homeWeb.reload()
            binding.showMenu = false
        }
        binding.tvHistory.setOnClickListener {
            HistoryActivity.start(this)
            binding.showMenu = false
        }
        binding.tvBookmark.setOnClickListener {
            BookmarkActivity.start(this)
            binding.showMenu = false
        }
        binding.tvAdd.setOnClickListener {
            val bean = BrowserDataBean(webUrl, webTitle, BVDataUtils.getCurrentTime(), false)
            BVDataUtils.saveWebPageBookmark(bean)
            binding.showMenu = false
            Toast.makeText(this, "Bookmark added successfully", Toast.LENGTH_SHORT).show()
        }
        binding.tvPrivate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(BrowserKey.URL_PRIVATE)
            intent.resolveActivity(packageManager)?.let {
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Browser not found", Toast.LENGTH_SHORT).show()
            }
            binding.showMenu = false
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWeb() {
        binding.showWeb = false
        binding.homeWeb.settings.javaScriptEnabled = true
        binding.homeWeb.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (loadUrl == url) {
                    view.loadUrl(url)
                } else {
                    return super.shouldOverrideUrlLoading(view, url)
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                webUrl = url.toString()
                binding.progressBarLoading.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                setGoOrBackIcon(binding)
                binding.progressBarLoading.visibility = View.GONE
            }
        }
        binding.homeWeb.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                webTitle = title.toString()
                val bean = BrowserDataBean(webUrl, webTitle, BVDataUtils.getCurrentTime(), false)
                if (!isHistory) {
                    BVDataUtils.saveWebPageHistory(bean)
                }
                isHistory = false
            }
        }
        if (loadUrl.isNotEmpty()) {
            binding.showWeb = true
            binding.homeWeb.loadUrl(loadUrl)
        }
    }

    fun setGoOrBackIcon(binding: ActivityMainBinding) {
        if (canGoBack()) {
            binding.imgLeft.setImageResource(R.drawable.icon_left_1)
        } else {
            binding.imgLeft.setImageResource(R.drawable.icon_left_2)
        }
        if (canGoForward()) {
            binding.imgRight.setImageResource(R.drawable.icon_right_1)
        } else {
            binding.imgRight.setImageResource(R.drawable.icon_right_2)
        }
    }

    private fun searchGoogle(data: String): String {
        return "https://www.baidu.com/s?wd=${data}"
    }

    private fun initEditText() {
        binding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                if (binding.edtSearch.text.toString().trim().isNotEmpty()) {
                    binding.showWeb = true
                    binding.homeWeb.loadUrl(searchGoogle(binding.edtSearch.text.toString()))
                    binding.edtSearch.text?.clear()
                    BVDataUtils.closeKeyboard(binding.edtSearch, this)
                }
                true
            } else {
                false
            }
        }

        binding.edtSearchWeb.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                if (binding.edtSearchWeb.text.toString().trim().isNotEmpty()) {
                    binding.showWeb = true
                    binding.homeWeb.loadUrl(searchGoogle(binding.edtSearchWeb.text.toString()))
                    binding.edtSearchWeb.text?.clear()
                    BVDataUtils.closeKeyboard(binding.edtSearchWeb, this)
                }
                true
            } else {
                false
            }
        }
    }

    private fun canGoBack(): Boolean {
        return binding.homeWeb.canGoBack()
    }

    private fun canGoForward(): Boolean {
        return binding.homeWeb.canGoForward()
    }

    private fun goBack() {
        if (canGoBack()) {
            binding.homeWeb.goBack()
        }
    }

    private fun goForward() {
        if (canGoForward()) {
            binding.homeWeb.goForward()
        }
    }
}