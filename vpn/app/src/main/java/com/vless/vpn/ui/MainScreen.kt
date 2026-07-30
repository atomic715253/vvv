package com.vless.vpn.ui

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vless.vpn.viewmodel.VpnViewModel
import com.vless.vpn.vpn.VlessConfig
import com.vless.vpn.vpn.VpnService as CustomVpnService

@Composable
fun MainScreen(
    viewModel: VpnViewModel,
    onRequestPermission: (Intent) -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val pingMs by viewModel.pingMs.collectAsState()
    val connectionDuration by viewModel.connectionDuration.collectAsState()

    val isConnected = connectionState == CustomVpnService.STATUS_CONNECTED
    val isConnecting = connectionState == CustomVpnService.STATUS_CONNECTING

    // Dynamic theme colors
    val buttonBgColor by animateColorAsState(
        targetValue = when {
            isConnected -> Color(0xFF10B981) // Green
            isConnecting -> Color(0xFFF59E0B) // Amber
            else -> Color(0xFFEF4444) // Red
        },
        animationSpec = tween(500), label = "buttonBgColor"
    )

    // Pulse animation during connecting
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnecting) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "VLESS VPN Client",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = VlessConfig.SERVER,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = { viewModel.measurePing() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Ping",
                    tint = Color(0xFF94A3B8)
                )
            }
        }

        // Server Status & Ping Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Server Protocol", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(text = "VLESS (TCP)", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Latency", color = Color(0xFF64748B), fontSize = 12.sp)
                    Text(
                        text = when (pingMs) {
                            null -> "Measuring..."
                            -1L -> "Timeout"
                            else -> "${pingMs} ms"
                        },
                        color = if (pingMs != null && pingMs!! > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Central Power Toggle Button & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status Text
            Text(
                text = connectionState,
                color = when {
                    isConnected -> Color(0xFF10B981)
                    isConnecting -> Color(0xFFF59E0B)
                    else -> Color(0xFF94A3B8)
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (isConnected) {
                val hours = connectionDuration / 3600
                val minutes = (connectionDuration % 3600) / 60
                val seconds = connectionDuration % 60
                Text(
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "Tap button to connect",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Large Circular Connect Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(buttonBgColor.copy(alpha = 0.2f))
                    .border(4.dp, buttonBgColor, CircleShape)
                    .clickable { viewModel.toggleVpn(onRequestPermission) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    buttonBgColor,
                                    buttonBgColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isConnected) "Disconnect" else if (isConnecting) "Connecting..." else "Connect",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Footer Config Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hardcoded VLESS Node",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Host: ${VlessConfig.SERVER}", color = Color.White, fontSize = 12.sp)
                    Text(text = "Port: ${VlessConfig.PORT}", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
