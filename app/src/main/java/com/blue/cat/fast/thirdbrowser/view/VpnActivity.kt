package com.blue.cat.fast.thirdbrowser.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.blue.cat.fast.thirdbrowser.databinding.ActivityVpnBinding
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import androidx.preference.PreferenceDataStore
import com.blue.cat.fast.thirdbrowser.utils.BVDataUtils
import com.blue.cat.fast.thirdbrowser.utils.BrowserServiceBean
import com.github.shadowsocks.Core
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.StartService
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VpnActivity : AppCompatActivity(),
    ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener {
    val binding by lazy { ActivityVpnBinding.inflate(layoutInflater) }
    val connection = ShadowsocksConnection(true)

    companion object {
        fun start(activity: AppCompatActivity) {
            activity.startActivity(Intent(activity, VpnActivity::class.java))
            activity.finish()
        }

        var stateListener: ((BaseService.State) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        stateListener?.invoke(BaseService.State.Idle)
        connection.connect(this, this)
        DataStore.publicStore.registerChangeListener(this)
        initializeServerData()
        onCLickFun()
    }

    private fun onCLickFun() {
        binding.imgService.setOnClickListener {

        }
        binding.clVpn.setOnClickListener {
            connectVpn()
        }
        binding.imgHalo.setOnClickListener {
            connectVpn()
        }
        binding.lottie.setOnClickListener {
            connectVpn()
        }

    }

    private fun setSkServerData(profile: Profile, bestData: BrowserServiceBean): Profile {
        profile.name = bestData.country + "-" + bestData.city
        profile.host = bestData.ip
        profile.password = bestData.password
        profile.method = bestData.method
        profile.remotePort = bestData.proxyPort
        return profile
    }

    private fun initializeServerData() {
        binding.showGuide = true
        val bestData = BVDataUtils.getConnectBrowserServiceBean()
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

    }

    val connect = registerForActivityResult(StartService()) {
        if (it) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
        } else {
            Core.startService()
//            if (SecureUtils.isHaveNetWork(SecureApp.instance)) {
//                homeFragment.connectVpnFun()
//            } else {
//                Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    fun connectVpn(){
        connect.launch(null)
        binding.showGuide =false
        binding.vpnState =1
        lifecycleScope.launch {
            var proInt = 0
            while (isActive){
                proInt++
                binding.progressBar.setProgress(proInt.toFloat())
                if(proInt>=100){
                    cancel()
                    binding.vpnState =2
                }
                delay(20)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        connection.bandwidthTimeout = 0
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

    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]

    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                connection.disconnect(this)
                connection.connect(this, this)
            }
        }
    }
}