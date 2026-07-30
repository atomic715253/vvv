package com.vless.vpn.vpn

import org.json.JSONArray
import org.json.JSONObject

/**
 * Generates the Xray-core JSON configuration file dynamically from VlessConfig properties.
 */
object XrayConfigBuilder {

    fun buildConfig(): String {
        val root = JSONObject()

        // Log settings
        val log = JSONObject().apply {
            put("loglevel", "warning")
        }
        root.put("log", log)

        // Inbound rules (Local SOCKS & HTTP proxies)
        val inbounds = JSONArray()

        val socksInbound = JSONObject().apply {
            put("port", VlessConfig.LOCAL_SOCKS_PORT)
            put("listen", "127.0.0.1")
            put("protocol", "socks")
            put("sniffing", JSONObject().apply {
                put("enabled", true)
                put("destOverride", JSONArray().apply {
                    put("http")
                    put("tls")
                })
            })
            put("settings", JSONObject().apply {
                put("auth", "noauth")
                put("udp", true)
            })
        }
        inbounds.put(socksInbound)

        val httpInbound = JSONObject().apply {
            put("port", VlessConfig.LOCAL_HTTP_PORT)
            put("listen", "127.0.0.1")
            put("protocol", "http")
            put("settings", JSONObject())
        }
        inbounds.put(httpInbound)

        root.put("inbounds", inbounds)

        // Outbound rules (VLESS server)
        val outbounds = JSONArray()

        val user = JSONObject().apply {
            put("id", VlessConfig.UUID)
            put("encryption", VlessConfig.ENCRYPTION)
            put("level", 0)
        }

        val server = JSONObject().apply {
            put("address", VlessConfig.SERVER)
            put("port", VlessConfig.PORT)
            put("users", JSONArray().apply { put(user) })
        }

        val vlessSettings = JSONObject().apply {
            put("vnext", JSONArray().apply { put(server) })
        }

        val streamSettings = JSONObject().apply {
            put("network", VlessConfig.NETWORK)
            put("security", VlessConfig.SECURITY)
        }

        val proxyOutbound = JSONObject().apply {
            put("protocol", "vless")
            put("settings", vlessSettings)
            put("streamSettings", streamSettings)
            put("tag", "proxy")
        }
        outbounds.put(proxyOutbound)

        // Direct outbound
        val directOutbound = JSONObject().apply {
            put("protocol", "freedom")
            put("tag", "direct")
        }
        outbounds.put(directOutbound)

        // Block outbound
        val blockOutbound = JSONObject().apply {
            put("protocol", "blackhole")
            put("tag", "block")
        }
        outbounds.put(blockOutbound)

        root.put("outbounds", outbounds)

        return root.toString(2)
    }
}
