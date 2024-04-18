package com.blue.cat.fast.thirdbrowser.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.addCallback
import com.blue.cat.fast.thirdbrowser.R
import com.blue.cat.fast.thirdbrowser.databinding.ActivityMainBinding
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserDataBean
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var loadUrl = ""
    private var webTitle: String = ""
    private var webUrl: String = ""
    private lateinit var webView: WebView
    private lateinit var finishReceiver: BroadcastReceiver

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
        Log.e("TAG", "onCreate: ${loadUrl}")
        clickFun()
        initWeb()
        initEditText()
        onBackFun()
        // 注册接收器
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // 结束当前Activity
                finish()
            }
        }
        registerReceiver(finishReceiver, IntentFilter("ACTION_FINISH_ACTIVITY"))

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(finishReceiver)
    }

    private fun onBackFun() {
        onBackPressedDispatcher.addCallback(this) {
            if (canGoBack()) {
                webView.goBack()
                return@addCallback
            }
            if (binding.showWeb == true) {
                loadUrl = ""
                initWeb()
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
            webView.loadUrl("https://www.instagram.com")
        }
        binding.tvVimor.setOnClickListener {
            binding.showWeb = true
            webView.loadUrl("https://vimeo.com/")
        }
        binding.tvFb.setOnClickListener {
            binding.showWeb = true
            webView.loadUrl("https://www.facebook.com")
        }
        binding.tvTiktok.setOnClickListener {
            binding.showWeb = true
            webView.loadUrl("https://www.tiktok.com")
        }
        binding.imgLeft.setOnClickListener {
            goBack()
        }
        binding.imgRight.setOnClickListener {
            goForward()
        }
        binding.imgHome.setOnClickListener {
            loadUrl = ""
            initWeb()
        }
        binding.imgMenu.setOnClickListener {
            binding.showMenu = true
        }
        binding.viewMenuBg.setOnClickListener {
            binding.showMenu = false
        }
        binding.tvReload.setOnClickListener {
            webView.reload()
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
        binding.imgVpn.setOnClickListener {
            VpnActivity.start(this)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWeb() {
        binding.imgLeft.setImageResource(R.drawable.icon_left_2)
        binding.imgRight.setImageResource(R.drawable.icon_right_2)
        binding.homeWeb.removeAllViews()
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
        }

        binding.homeWeb.addView(webView)
        binding.showWeb = false
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
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
                setGoOrBackIcon(binding)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                setGoOrBackIcon(binding)
                binding.progressBarLoading.visibility = View.GONE
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
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
            webView.loadUrl(loadUrl)
        }
    }

    fun setGoOrBackIcon(binding: ActivityMainBinding) {
        if (canGoBack() && binding.showWeb == true) {
            binding.imgLeft.setImageResource(R.drawable.icon_left_1)
        } else {
            binding.imgLeft.setImageResource(R.drawable.icon_left_2)
        }
        if (canGoForward() && binding.showWeb == true) {
            binding.imgRight.setImageResource(R.drawable.icon_right_1)
        } else {
            binding.imgRight.setImageResource(R.drawable.icon_right_2)
        }
    }

    private fun searchGoogle(data: String): String {
        val urlPattern =
            "^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$".toRegex()

        return if (urlPattern.matches(data)) {
            if (data.startsWith("http://") || data.startsWith("https://")) {
                data
            } else {
                "https://$data"
            }
        } else {
            "https://www.google.com/search?q=${data.replace(" ", "+")}"
        }
    }


    private fun initEditText() {
        binding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                if (binding.edtSearch.text.toString().trim().isNotEmpty()) {
                    binding.showWeb = true
                    webView.loadUrl(searchGoogle(binding.edtSearch.text.toString()))
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
                    webView.loadUrl(searchGoogle(binding.edtSearchWeb.text.toString()))
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
        return webView.canGoBack()
    }

    private fun canGoForward(): Boolean {
        return webView.canGoForward()
    }

    private fun goBack() {
        if (canGoBack()) {
            webView.goBack()
        }
    }

    private fun goForward() {
        if (canGoForward()) {
            webView.goForward()
        }
    }

    override fun onResume() {
        super.onResume()
        val bean = runCatching {
            Gson().fromJson(
                BrowserKey.connectVpn,
                BrowserServiceBean::class.java
            )
        }.getOrNull()
        binding.imgVpn.setImageResource(BVDataUtils.getImageFlag(bean?.country ?: ""))
        binding.imgFlag.setImageResource(BVDataUtils.getImageFlag(bean?.country ?: ""))
        binding.tvCountry.text = bean?.country ?: "Smart Sever"
    }
}