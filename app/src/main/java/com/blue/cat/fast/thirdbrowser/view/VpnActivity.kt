package com.blue.cat.fast.thirdbrowser.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityVpnBinding
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import androidx.preference.PreferenceDataStore
import com.blue.cat.fast.thirdbrowser.App
import com.blue.cat.fast.thirdbrowser.model.TimerViewModel
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserKey
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean
import com.blue.cat.fast.thirdbrowser.utils.NetUtils
import com.blue.cat.fast.thirdbrowser.view.ad.FieryAdMob
import com.github.shadowsocks.Core
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.StartService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.system.exitProcess

class VpnActivity : AppCompatActivity(),
    ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener {
    val binding by lazy { ActivityVpnBinding.inflate(layoutInflater) }
    private val connection = ShadowsocksConnection(true)
    private var jumpType: Int = -1
    private var connectType: Int = 0
    private var connectAdJob: Job? = null
    private var showConnectAdLive = MutableLiveData<Any>()

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, VpnActivity::class.java))
        }

        fun startAndConnect(activity: AppCompatActivity, connectType: Int) {
            val intent = Intent(activity, VpnActivity::class.java)
            intent.putExtra("connectType", connectType)
            activity.startActivity(intent)
            if (activity is Guide2Activity || activity is GuideActivity) {
                activity.finish()
            }
        }

        var stateListener: ((BaseService.State) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        BVDataUtils.rLOr()
        connectType = intent.getIntExtra("connectType", 0)
        stateListener?.invoke(BaseService.State.Idle)
        connection.connect(this, this)
        DataStore.publicStore.registerChangeListener(this)
        if (App.viewModel == null) {
            App.viewModel = ViewModelProvider(this)[TimerViewModel::class.java]
        }
        jumpType = -1
        binding.showGuide = true
        initializeServerData()
        onCLickFun()
        observeFun()
        haveShowNetWork()
        onBackPressedDispatcher.addCallback(this) {
            backFun()
        }
        if (connectType == 1 || connectType == 2 || connectType == 3) {
            beforeClickVpn()
        }
        showConnectAdLive.observe(this) {
            showAddAd(it)
        }
    }

    private fun haveShowNetWork(): Boolean {
        return if (BVDataUtils.isNetworkAvailable(this)) {
            binding.clNetWork.visibility = View.GONE
            true
        } else {
            binding.clNetWork.visibility = View.VISIBLE
            false
        }
    }

    private fun observeFun() {
        lifecycleScope.launch {
            while (isActive) {
                binding.tvTime.text = App.timerText
                delay(1000)
            }
        }
    }

    private fun backFun() {
        if (binding.showGuide == true) {
            binding.showGuide = false
            return
        }

        if (isConnectionProcess()) {
            Toast.makeText(this, "VPN is connecting. Please wait...", Toast.LENGTH_SHORT).show()
            return
        }
        if (isDisconnectionProcess()) {
            Toast.makeText(this, "VPN is disconnecting. Please wait...", Toast.LENGTH_SHORT).show()
            return
        }
        if (connectType == 1 || connectType == 2) {
            MainActivity.start(this)
        }
        finish()
    }

    fun onCLickMark(v: View) {}
    private fun onCLickFun() {
        binding.imgFinish.setOnClickListener {
            backFun()
        }
        binding.imgService.setOnClickListener {
            jumToServicePage()
        }
        binding.clVpn.setOnClickListener {
            beforeClickVpn()
        }
        binding.imgHalo.setOnClickListener {
            beforeClickVpn()
        }
        binding.lottie.setOnClickListener {
            beforeClickVpn()
        }
        binding.viewDue.setOnClickListener { }
        binding.viewNetWork.setOnClickListener { }
        binding.tvDue.setOnClickListener {
            finish()
            exitProcess(0)
        }
        binding.tvOk.setOnClickListener {
            binding.clNetWork.visibility = View.GONE
        }
        binding.llService.setOnClickListener {
            jumToServicePage()
        }
    }

    private fun jumToServicePage() {
        if (isConnectionProcess()) {
            Toast.makeText(this, "VPN is connecting. Please wait...", Toast.LENGTH_SHORT).show()
            return
        }
        if (isDisconnectionProcess()) {
            Toast.makeText(this, "VPN is disconnecting. Please wait...", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            binding.proConnect.visibility = View.VISIBLE
            if (BVDataUtils.isHaveServeData()) {
                binding.proConnect.visibility = View.GONE
                MoreVpnActivity.start(this@VpnActivity)
            } else {
                delay(2000)
                binding.proConnect.visibility = View.GONE
                MoreVpnActivity.start(this@VpnActivity)
            }
        }
    }

    private fun beforeClickVpn() {
        if (binding.vpnState == 1) {
            return
        }
        lifecycleScope.launch {
            NetUtils.getIpDataInfo()
            binding.showGuide = false
            binding.proConnect.visibility = View.VISIBLE
            if (BVDataUtils.isHaveServeData()) {
                connectOrDisConnect()
            } else {
                delay(2000)
                binding.proConnect.visibility = View.GONE
            }
        }
    }

    private fun connectOrDisConnect() {
        initializeServerData()
        binding.proConnect.visibility = View.GONE
        connect.launch(null)
    }

    private fun setSkServerData(profile: Profile, bestData: BrowserServiceBean): Profile {
        profile.name = bestData.country + "-" + bestData.city
        profile.host = bestData.ip
        profile.password = bestData.password
        profile.method = bestData.method
        profile.remotePort = bestData.proxyPort
        BrowserKey.connectVpn = Gson().toJson(bestData)
        setConnectServiceUI(bestData)
        return profile
    }

    private fun initializeServerData() {
        val bestData = BVDataUtils.getConnectBrowserServiceBean()
        if (bestData != null) {
            ProfileManager.getProfile(DataStore.profileId).let {
                if (it != null) {
                    ProfileManager.updateProfile(setSkServerData(it, bestData))
                } else {
                    val profile = Profile()
                    ProfileManager.createProfile(setSkServerData(profile, bestData))
                }
            }
            bestData.bestService = true
            DataStore.profileId = 1L
            Log.e("TAG", "连接IP: ${bestData.ip}")
        }
    }

    private fun setConnectServiceUI(bean: BrowserServiceBean) {
        binding.imgFast.setImageResource(BVDataUtils.getImageFlag(bean.country))
        binding.tvServiceName.text = bean.country
    }

    private val connect = registerForActivityResult(StartService()) {
        if (it) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
        } else {
            if (haveShowNetWork()) {
                if (!showDueDialog()) {
                    connectVpn()
                }
            }
        }
    }

    private fun connectVpn() {
        binding.showGuide = false
        if (binding.vpnState != 1) {
            duringConnection()
            loadConnectAd()
        }
    }

    private fun disOrConnectFun() {
        if (BrowserKey.vpnState == 2) {
            Core.stopService()
        }
        if (BrowserKey.vpnState == 0) {
            Core.startService()
        }
    }

    private fun connectSuccess() {
        binding.showGuide = false
        binding.vpnState = 2
        binding.tvVpnState.text = "Connected"
        if (jumpType != -1) {
            BVDataUtils.executeWithDebounce {
                ResultActivity.start(this)
                App.viewModel?.startTimer()
                finish()
            }
        }
    }

    private fun disConnectSuccess() {
        binding.vpnState = 0
        binding.tvVpnState.text = "Disconnected"
        if (jumpType != -1) {
            BVDataUtils.executeWithDebounce {
                ResultActivity.start(this)
                App.viewModel?.stopTimer()
            }
        }
    }

    private fun duringConnection() {
        binding.vpnState = 1
        if (isConnectionProcess()) {
            binding.tvVpnState.text = "Disconnecting"
        }
        if (isDisconnectionProcess()) {
            binding.tvVpnState.text = "Connecting"
        }
    }

    private fun isConnectionProcess(): Boolean {
        return binding.vpnState == 1 && (BrowserKey.vpnClickState == 0)
    }

    private fun isDisconnectionProcess(): Boolean {
        return binding.vpnState == 1 && BrowserKey.vpnClickState == 2
    }

    override fun onStop() {
        super.onStop()
        connection.bandwidthTimeout = 0
        connectAdJob?.cancel()
        connectAdJob = null
        if (isConnectionProcess()) {
            jumpType = -1
            disConnectSuccess()
        }
        if (isDisconnectionProcess()) {
            jumpType = -1
            connectSuccess()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        DataStore.publicStore.unregisterChangeListener(this)
        connection.disconnect(this)
    }


    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        Log.e("TAG", "stateChanged: ${state.canStop}")
        when (state.canStop) {
            true -> {
                BrowserKey.vpnState = 2
                connectSuccess()
            }

            false -> {
                BrowserKey.vpnState = 0
                disConnectSuccess()
            }
        }
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        Log.e("TAG", "onServiceConnected: ${state.canStop}")

        when (state.canStop) {
            true -> {
                BrowserKey.vpnState = 2
                connectSuccess()
            }

            false -> {
                BrowserKey.vpnState = 0
                disConnectSuccess()
            }
        }
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                connection.disconnect(this)
                connection.connect(this, this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x556 && BrowserKey.clickVpn != "") {
            if (BrowserKey.vpnState != 2) {
                BrowserKey.connectVpn = BrowserKey.clickVpn
            }
            initializeServerData()
            beforeClickVpn()
        }
        if (requestCode == 0x4454 && BrowserKey.clickVpn != "" && BrowserKey.vpnState != 2) {
            BrowserKey.connectVpn = BrowserKey.clickVpn
            initializeServerData()
            BrowserKey.clickVpn = ""
        }
    }

    private fun showDueDialog(): Boolean {
        return if (NetUtils.isIllegalIp()) {
            binding.clDue.visibility = View.VISIBLE
            true
        } else {
            binding.clDue.visibility = View.GONE
            false
        }
    }

    private fun loadConnectAd() {
        var isJump = true
        BrowserKey.vpnClickState = BrowserKey.vpnState
        jumpType = BrowserKey.vpnState
        var proInt = if (BrowserKey.vpnState == 2) 100 else 0
        var isCanShow = false
        connectAdJob?.cancel()
        connectAdJob = null
        connectAdJob = lifecycleScope.launch {
            FieryAdMob.loadOf(BrowserKey.Fiery_CONNECT_INT)
            FieryAdMob.loadOf(BrowserKey.Fiery_BACK_INT)
            try {
                withTimeout(10000) {
                    while (isActive) {
                        if (BrowserKey.vpnState == 2) {
                            proInt--
                            if (proInt <= 90) {
                                isCanShow = true
                            }
                        } else {
                            if (proInt >= 10) {
                                isCanShow = true
                            }
                            proInt++
                        }
                        binding.progressBar.setProgress(proInt.toFloat())
                        if (BrowserKey.isThresholdReached() && FieryAdMob.resultOf(BrowserKey.Fiery_CONNECT_INT) == "") {
                            isJump = true
                            break
                        }
                        if (FieryAdMob.resultOf(BrowserKey.Fiery_CONNECT_INT) != null && isCanShow) {
                            FieryAdMob.resultOf(BrowserKey.Fiery_CONNECT_INT)
                                ?.let {
                                    isJump = false
                                    showConnectAdLive.postValue(it)
                                }
                            break
                        }
                        delay(100)
                    }
                }
            } finally {
                if (!isActive) {
                    return@launch
                }
                if (isJump) {
                    disOrConnectFun()
                }
            }
        }
    }


    private fun showAddAd(addAdData: Any) {
        FieryAdMob.showFullScreenOf(
            where = BrowserKey.Fiery_CONNECT_INT,
            context = this,
            res = addAdData,
            preload = true,
            onShowCompleted = {
                Log.e("TAG", "showAddAd-vpn: ${lifecycle.currentState}", )
                if (lifecycle.currentState == Lifecycle.State.RESUMED || lifecycle.currentState == Lifecycle.State.STARTED) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        disOrConnectFun()
                    }
                }
            }
        )
    }
}