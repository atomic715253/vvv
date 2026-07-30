package com.vless.vpn.vpn

/**
 * Hardcoded VLESS Configuration for the Android VPN Client.
 * No user input required - pre-configured VLESS connection details.
 */
object VlessConfig {
    const val VLESS_URL =
        "vless://69af5525-175e-4f19-b213-2c8ab84e7dbe@sakura.proxy.rlwy.net:10322?encryption=none&security=none&type=tcp&headerType=none#TCP"

    const val PROTOCOL = "VLESS"
    const val UUID = "69af5525-175e-4f19-b213-2c8ab84e7dbe"
    const val SERVER = "sakura.proxy.rlwy.net"
    const val PORT = 10322
    const val ENCRYPTION = "none"
    const val NETWORK = "tcp"
    const val SECURITY = "none"
    const val HEADER_TYPE = "none"
    const val REMARK = "TCP"
    
    // Local TUN / Proxy ports
    const val LOCAL_SOCKS_PORT = 10808
    const val LOCAL_HTTP_PORT = 10809
}
