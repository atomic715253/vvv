package com.vless.vpn.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vless.vpn.vpn.VlessConfig
import com.vless.vpn.vpn.VpnService as CustomVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val _connectionState = MutableStateFlow(CustomVpnService.STATUS_DISCONNECTED)
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _pingMs = MutableStateFlow<Long?>(null)
    val pingMs: StateFlow<Long?> = _pingMs.asStateFlow()

    private val _connectionDuration = MutableStateFlow(0L)
    val connectionDuration: StateFlow<Long> = _connectionDuration.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(CustomVpnService.EXTRA_STATUS)
            if (status != null) {
                _connectionState.value = status
                if (status == CustomVpnService.STATUS_CONNECTED) {
                    startTimer()
                } else if (status == CustomVpnService.STATUS_DISCONNECTED) {
                    stopTimer()
                }
            }
        }
    }

    init {
        val filter = IntentFilter(CustomVpnService.ACTION_STATUS_CHANGE)
        getApplication<Application>().registerReceiver(statusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        measurePing()
    }

    fun toggleVpn(onRequestPermission: (Intent) -> Unit) {
        when (_connectionState.value) {
            CustomVpnService.STATUS_DISCONNECTED -> {
                val intent = VpnService.prepare(getApplication())
                if (intent != null) {
                    onRequestPermission(intent)
                } else {
                    startVpnService()
                }
            }
            CustomVpnService.STATUS_CONNECTED, CustomVpnService.STATUS_CONNECTING -> {
                stopVpnService()
            }
        }
    }

    fun startVpnService() {
        val context = getApplication<Application>()
        val intent = Intent(context, CustomVpnService::class.java).apply {
            action = CustomVpnService.ACTION_CONNECT
        }
        context.startService(intent)
    }

    fun stopVpnService() {
        val context = getApplication<Application>()
        val intent = Intent(context, CustomVpnService::class.java).apply {
            action = CustomVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun measurePing() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress(VlessConfig.SERVER, VlessConfig.PORT), 3000)
                val endTime = System.currentTimeMillis()
                socket.close()
                _pingMs.value = endTime - startTime
            } catch (e: Exception) {
                _pingMs.value = -1
            }
        }
    }

    private fun startTimer() {
        _connectionDuration.value = 0L
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_connectionState.value == CustomVpnService.STATUS_CONNECTED) {
                delay(1000)
                _connectionDuration.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _connectionDuration.value = 0L
    }

    override fun onCleared() {
        try {
            getApplication<Application>().unregisterReceiver(statusReceiver)
        } catch (_: Exception) {}
        super.onCleared()
    }
}
