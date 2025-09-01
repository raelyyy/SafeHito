package com.capstone.safehito.util

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Socket
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

data class PiStatus(
    val isOnline: Boolean = false,
    val cloudflareUrl: String? = null,
    val ipAddress: String? = null,
    val serverUrl: String? = null,   // <- always the chosen one
    val lastSeen: Long = 0L,
    val connectionQuality: ConnectionQuality = ConnectionQuality.UNKNOWN,
    val errorMessage: String? = null
)


enum class ConnectionQuality {
    EXCELLENT,    // < 100ms response time
    GOOD,         // 100-500ms response time
    POOR,         // 500ms-2s response time
    UNRELIABLE,   // > 2s response time or intermittent
    UNKNOWN       // No connection data
}

class PiStatusManager {
    private val database = FirebaseDatabase.getInstance()
    private val piIpRef = database.getReference("raspberry_pi/local_ip")
    private val piStatusRef = database.getReference("raspberry_pi/status")
    private val piCloudflareRef = database.getReference("raspberry_pi/cloudflare_url")

    private val _piStatus = MutableStateFlow(PiStatus())
    val piStatus: StateFlow<PiStatus> = _piStatus
    
    private var ipListener: ValueEventListener? = null
    private var cloudflareListener: ValueEventListener? = null
    private var statusCheckJob: Job? = null
    private var heartbeatJob: Job? = null
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    // in PiStatusManager
    fun startMonitoring() {
        stopMonitoring()

        // Listen for LAN IP first
        ipListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ip = snapshot.getValue(String::class.java)
                if (!ip.isNullOrEmpty()) {
                    val lanUrl = "http://$ip:5000"
                    Log.d("PiStatusManager", "✅ Using LAN IP: $lanUrl")
                    _piStatus.value = _piStatus.value.copy(
                        ipAddress = ip,
                        serverUrl = lanUrl
                    )
                    startConnectionMonitoring(lanUrl)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.getReference("raspberry_pi/local_ip")
            .addValueEventListener(ipListener!!)

        // Only fallback to Cloudflare if LAN missing
        cloudflareListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                if (!url.isNullOrEmpty() && _piStatus.value.ipAddress.isNullOrEmpty()) {
                    Log.d("PiStatusManager", "⚠️ Falling back to Cloudflare: $url")
                    _piStatus.value = _piStatus.value.copy(
                        cloudflareUrl = url,
                        serverUrl = url
                    )
                    startConnectionMonitoring(url)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.getReference("raspberry_pi/cloudflare_url")
            .addValueEventListener(cloudflareListener!!)

        startHeartbeat()
    }



    fun stopMonitoring() {
        ipListener?.let { piIpRef.removeEventListener(it) }
        ipListener = null

        cloudflareListener?.let { piCloudflareRef.removeEventListener(it) }
        cloudflareListener = null

        statusCheckJob?.cancel()
        statusCheckJob = null

        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun startConnectionMonitoring(ip: String) {
        statusCheckJob?.cancel()
        statusCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                checkConnection(ip)
                delay(10000) // Check every 10 seconds
            }
        }
    }

    private suspend fun checkConnection(ip: String) {
        try {
            val startTime = System.currentTimeMillis()
            var isOnline = false
            var responseTime = 0L
            var connectionQuality = ConnectionQuality.UNKNOWN
            var errorMessage: String? = null

            // ✅ Normalize to handle ngrok or LAN
            val baseUrl = if (ip.startsWith("http")) ip else "http://$ip:5000"
            val endpoints = listOf(
                "$baseUrl/health",
                "$baseUrl/",
                "$baseUrl/scan"
            )

            // ✅ Only do TCP check for LAN, skip for ngrok
            if (!ip.startsWith("http")) {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(ip, 5000), 3000)
                    socket.close()
                    Log.d("PiStatusManager", "TCP connection to $ip:5000 successful")
                } catch (e: Exception) {
                    errorMessage = "TCP connection failed: ${e.message}"
                }
            }

            // ✅ Try HTTP endpoints (works for ngrok + LAN)
            for (endpoint in endpoints) {
                try {
                    val request = Request.Builder().url(endpoint).get().build()
                    val response = client.newCall(request).execute()
                    responseTime = System.currentTimeMillis() - startTime

                    if (response.isSuccessful) {
                        isOnline = true
                        connectionQuality = when {
                            responseTime < 100 -> ConnectionQuality.EXCELLENT
                            responseTime < 500 -> ConnectionQuality.GOOD
                            responseTime < 2000 -> ConnectionQuality.POOR
                            else -> ConnectionQuality.UNRELIABLE
                        }
                        Log.d("PiStatusManager", "Connected to $endpoint in ${responseTime}ms")
                        break
                    }
                } catch (e: Exception) {
                    errorMessage = "HTTP failed: ${e.message}"
                }
            }

            withContext(Dispatchers.Main) {
                _piStatus.value = _piStatus.value.copy(
                    isOnline = isOnline,
                    lastSeen = if (isOnline) System.currentTimeMillis() else _piStatus.value.lastSeen,
                    connectionQuality = connectionQuality,
                    errorMessage = if (isOnline) null else errorMessage
                )
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _piStatus.value = _piStatus.value.copy(isOnline = false, errorMessage = e.message)
            }
        }
    }


    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val currentStatus = _piStatus.value
                    piStatusRef.setValue(mapOf(
                        "isOnline" to currentStatus.isOnline,
                        "lastSeen" to currentStatus.lastSeen,
                        "connectionQuality" to currentStatus.connectionQuality.name,
                        "lastUpdated" to System.currentTimeMillis()
                    ))
                    delay(30000) // Update Firebase every 30 seconds
                } catch (e: Exception) {
                    Log.e("PiStatusManager", "Failed to update Firebase status", e)
                    delay(60000) // Wait longer on error
                }
            }
        }
    }
    
    fun forceConnectionCheck() {
        val currentIp = _piStatus.value.ipAddress
        if (currentIp != null) {
            CoroutineScope(Dispatchers.IO).launch {
                checkConnection(currentIp)
            }
        }
    }
    
    fun getStatusDescription(): String {
        return when {
            _piStatus.value.isOnline -> {
                when (_piStatus.value.connectionQuality) {
                    ConnectionQuality.EXCELLENT -> "Pi Online (Excellent)"
                    ConnectionQuality.GOOD -> "Pi Online (Good)"
                    ConnectionQuality.POOR -> "Pi Online (Poor)"
                    ConnectionQuality.UNRELIABLE -> "Pi Online (Unreliable)"
                    ConnectionQuality.UNKNOWN -> "Pi Online"
                }
            }
            _piStatus.value.ipAddress == null -> "Pi Not Configured"
            else -> "Pi Offline"
        }
    }
    
    fun getStatusColor(): androidx.compose.ui.graphics.Color {
        return when {
            _piStatus.value.isOnline -> {
                when (_piStatus.value.connectionQuality) {
                    ConnectionQuality.EXCELLENT -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                    ConnectionQuality.GOOD -> androidx.compose.ui.graphics.Color(0xFF8BC34A) // Light Green
                    ConnectionQuality.POOR -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                    ConnectionQuality.UNRELIABLE -> androidx.compose.ui.graphics.Color(0xFFFF5722) // Deep Orange
                    ConnectionQuality.UNKNOWN -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
                }
            }
            _piStatus.value.ipAddress == null -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Grey
            else -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
        }
    }
}
