package com.capstone.safehito.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val context = LocalContext.current

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
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Raspberry Pi",
                        tint = piStatusManager.getStatusColor(),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Pi Connection Status",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Status details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusDetailRow("Status", piStatusManager.getStatusDescription())

                    // LAN IP clickable + copy
                    piStatus.ipAddress?.let { ip ->
                        val url = "http://$ip:5000"
                        StatusDetailRow(
                            label = "LAN IP",
                            value = url,
                            clickable = true,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("LAN IP", url)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "LAN IP copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Cloudflare clickable + copy
                    piStatus.cloudflareUrl?.let { url ->
                        StatusDetailRow(
                            label = "Cloudflare",
                            value = url,
                            clickable = true,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Cloudflare URL", url)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Cloudflare URL copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (piStatus.lastSeen > 0) {
                        StatusDetailRow("Last Seen", dateFormat.format(Date(piStatus.lastSeen)))
                    }

                    if (piStatus.isOnline) {
                        StatusDetailRow(
                            "Connection",
                            piStatus.connectionQuality.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                    }

                    piStatus.errorMessage?.let { error ->
                        StatusDetailRow("Error", error, isError = true)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close button only
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Close",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
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
    isError: Boolean = false,
    clickable: Boolean = false,
    onClick: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable && onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.widthIn(min = 90.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    clickable -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Copy icon (if copy is supported)
        if (onCopy != null) {
            IconButton(
                onClick = { onCopy() },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
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
