package com.vless.vpn.vpn

import android.content.Context
import android.util.Log
import java.io.File
import libv2ray.Libv2ray

/**
 * Manages the Xray-core daemon process embedded via libv2ray library.
 */
class XrayManager(private val context: Context) {

    private var isRunning = false

    companion object {
        private const val TAG = "XrayManager"
    }

    /**
     * Starts Xray-core with the generated JSON configuration.
     */
    fun startXray(): Boolean {
        if (isRunning) return true

        return try {
            val configContent = XrayConfigBuilder.buildConfig()
            val configFile = File(context.filesDir, "config.json")
            configFile.writeText(configContent)

            Log.d(TAG, "Writing Xray configuration to ${configFile.absolutePath}")

            // Initialize libv2ray core
            Libv2ray.initV2Env(context.filesDir.absolutePath)
            val result = Libv2ray.startV2Ray(configFile.absolutePath)
            
            if (result == 0L) {
                isRunning = true
                Log.i(TAG, "Xray-core successfully started!")
                true
            } else {
                Log.e(TAG, "Failed to start Xray-core. Error code: $result")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Xray-core", e)
            false
        }
    }

    /**
     * Stops the running Xray-core process.
     */
    fun stopXray() {
        if (!isRunning) return
        try {
            Libv2ray.stopV2Ray()
            isRunning = false
            Log.i(TAG, "Xray-core stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Xray-core", e)
        }
    }

    fun isCoreRunning(): Boolean = isRunning
}
