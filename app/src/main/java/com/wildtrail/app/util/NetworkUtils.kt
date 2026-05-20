package com.wildtrail.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

object NetworkUtils {
    /**
     * Returns true if the device currently has an active, validated internet
     * connection. "Validated" means Android confirmed it can actually reach
     * the internet (not just that a Wi-Fi network is joined but captive-portal
     * blocked).
     */
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

/**
 * Composable helper that returns `true` when the device has internet and
 * recomposes automatically whenever connectivity changes.
 *
 * How it works:
 *  1. Reads the current state immediately with [NetworkUtils.isOnline].
 *  2. Registers a [ConnectivityManager.NetworkCallback] while the composable
 *     is on screen, which fires on every network change.
 *  3. [DisposableEffect] unregisters the callback when the composable leaves
 *     the screen, so we never leak a listener.
 */
@Composable
fun rememberIsOnline(): Boolean {
    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(ConnectivityManager::class.java)
    }
    var isOnline by remember { mutableStateOf(NetworkUtils.isOnline(context)) }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                isOnline = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            override fun onLost(network: Network) {
                isOnline = false
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    return isOnline
}
