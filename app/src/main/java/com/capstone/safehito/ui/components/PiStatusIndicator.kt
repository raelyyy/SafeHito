package com.capstone.safehito.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capstone.safehito.util.PiStatus
import com.capstone.safehito.util.PiStatusManager
import com.capstone.safehito.util.ConnectionQuality
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PiStatusIndicator(
    piStatusManager: PiStatusManager,
    modifier: Modifier = Modifier,
    showDetailedInfo: Boolean = false,
    onStatusClick: (() -> Unit)? = null
) {
    val piStatus by piStatusManager.piStatus.collectAsState()
    
    val statusColor = piStatusManager.getStatusColor()
    val statusDescription = piStatusManager.getStatusDescription()
    
    // Pulsing animation for offline status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val alpha = if (!piStatus.isOnline && piStatus.ipAddress != null) pulseAlpha else 1.0f
    
    Box(
        modifier = modifier
            .then(
                if (onStatusClick != null) {
                    Modifier.clickable { onStatusClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.7f * alpha),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = statusColor,
                        shape = CircleShape
                    )
            )
            
            // Status text
            Text(
                text = statusDescription,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            // Connection quality indicator (if online)
            if (piStatus.isOnline && showDetailedInfo) {
                ConnectionQualityIndicator(piStatus.connectionQuality)
            }
        }
    }
}

@Composable
fun ConnectionQualityIndicator(quality: ConnectionQuality) {
    val iconAndColor = when (quality) {
        ConnectionQuality.EXCELLENT -> Pair(Icons.Default.SignalCellular4Bar, Color(0xFF4CAF50))
        ConnectionQuality.GOOD -> Pair(Icons.Default.SignalCellularAlt, Color(0xFF8BC34A))
        ConnectionQuality.POOR -> Pair(Icons.Default.SignalCellularConnectedNoInternet0Bar, Color(0xFFFF9800))
        ConnectionQuality.UNRELIABLE -> Pair(Icons.Default.SignalCellularConnectedNoInternet0Bar, Color(0xFFFF5722))
        ConnectionQuality.UNKNOWN -> Pair(Icons.Default.SignalCellular0Bar, Color(0xFF9E9E9E))
    }
    
    Icon(
        imageVector = iconAndColor.first,
        contentDescription = "Connection Quality: $quality",
        tint = iconAndColor.second,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun PiStatusDialog(
    piStatusManager: PiStatusManager,
    onDismiss: () -> Unit
) {
    val piStatus by piStatusManager.piStatus.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Raspberry Pi",
                        tint = piStatusManager.getStatusColor(),
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Text(
                        text = "Pi Connection Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Status details
                StatusDetailRow("Status", piStatusManager.getStatusDescription())
                
                piStatus.ipAddress?.let { ip ->
                    StatusDetailRow("IP Address", ip)
                }
                
                if (piStatus.lastSeen > 0) {
                    StatusDetailRow(
                        "Last Seen",
                        dateFormat.format(Date(piStatus.lastSeen))
                    )
                }
                
                if (piStatus.isOnline) {
                    StatusDetailRow(
                        "Connection Quality",
                        piStatus.connectionQuality.name.lowercase().capitalize()
                    )
                }
                
                piStatus.errorMessage?.let { error ->
                    StatusDetailRow("Error", error, isError = true)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            piStatusManager.forceConnectionCheck()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh")
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDetailRow(
    label: String,
    value: String,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) Color.Red else Color.Black,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun PiStatusBanner(
    piStatusManager: PiStatusManager,
    modifier: Modifier = Modifier
) {
    val piStatus by piStatusManager.piStatus.collectAsState()
    
    if (!piStatus.isOnline && piStatus.ipAddress != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            ),
            border = BorderStroke(1.dp, Color(0xFFFFCDD2))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pi Connection Lost",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    
                    Text(
                        text = "Your Raspberry Pi appears to be offline. Some features may not work properly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}
