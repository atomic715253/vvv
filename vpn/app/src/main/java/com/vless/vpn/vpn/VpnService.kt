package com.vless.vpn.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * Android VpnService implementation creating TUN interface and routing device traffic.
 */
class VpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var xrayManager: XrayManager? = null
    private var isRunning = false

    companion object {
        const val ACTION_CONNECT = "com.vless.vpn.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.vless.vpn.ACTION_DISCONNECT"
        const val ACTION_STATUS_CHANGE = "com.vless.vpn.ACTION_STATUS_CHANGE"
        const val EXTRA_STATUS = "extra_status"
        
        const val STATUS_DISCONNECTED = "Disconnected"
        const val STATUS_CONNECTING = "Connecting"
        const val STATUS_CONNECTED = "Connected"
        
        private const val TAG = "VpnService"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_CONNECT -> startVpnTunnel()
            ACTION_DISCONNECT -> stopVpnTunnel()
        }
        return START_STICKY
    }

    private fun startVpnTunnel() {
        if (isRunning) return
        
        broadcastStatus(STATUS_CONNECTING)
        Log.i(TAG, "Initializing VPN Service & Xray Core...")

        try {
            // 1. Start Xray-Core
            xrayManager = XrayManager(applicationContext)
            val xrayStarted = xrayManager?.startXray() ?: false
            
            if (!xrayStarted) {
                Log.e(TAG, "Xray-Core failed to start")
                broadcastStatus(STATUS_DISCONNECTED)
                return
            }

            // 2. Establish TUN Interface
            val builder = Builder()
                .setSession("VLESS VPN")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0)
                .setMtu(1500)

            // Bypass VLESS server IP from VPN routing loop
            builder.addDisallowedApplication(packageName)

            vpnInterface = builder.establish()

            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish TUN interface")
                xrayManager?.stopXray()
                broadcastStatus(STATUS_DISCONNECTED)
                return
            }

            isRunning = true
            broadcastStatus(STATUS_CONNECTED)
            Log.i(TAG, "VPN Tunnel successfully established!")

        } catch (e: Exception) {
            Log.e(TAG, "Fatal error establishing VPN service", e)
            stopVpnTunnel()
        }
    }

    private fun stopVpnTunnel() {
        try {
            xrayManager?.stopXray()
            vpnInterface?.close()
            vpnInterface = null
            isRunning = false
            broadcastStatus(STATUS_DISCONNECTED)
            Log.i(TAG, "VPN Service stopped")
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN service", e)
        }
    }

    private fun broadcastStatus(status: String) {
        val intent = Intent(ACTION_STATUS_CHANGE).apply {
            putExtra(EXTRA_STATUS, status)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        stopVpnTunnel()
        super.onDestroy()
    }
}
