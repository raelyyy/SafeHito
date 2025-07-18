package com.capstone.safehito.util // change this to your actual package

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/**
 * Observe real-time network status.
 */
@Composable
fun rememberNetworkStatus(): State<Boolean> {
    val context = LocalContext.current
    val isConnected = remember { mutableStateOf(checkInitialConnection(context)) }

    DisposableEffect(Unit) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected.value = true
            }

            override fun onLost(network: Network) {
                isConnected.value = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected
}


// ✅ Check network at launch
fun checkInitialConnection(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}


/**
 * Manages visibility of offline banner with delayed dismissal after reconnect.
 */
@Composable
fun rememberOfflineBannerState(): State<Boolean> {
    val isConnected by rememberNetworkStatus()
    var showBanner by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            showBanner = true
        } else {
            delay(1500) // Delay hiding after reconnect
            showBanner = false
        }
    }

    return derivedStateOf { showBanner }
}
