package org.berenguel.carheadunitconfigurer.managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.preference.PreferenceManager


object EthernetWifiManager {

    private const val TAG = "EthernetWifiManager"

    private var countTimer = 0

    fun init(context: Context) {

        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        // listen SCAN_RESULTS_AVAILABLE_ACTION
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                Log.i(TAG, "SCAN_RESULTS_AVAILABLE_ACTION -> Wi-Fi scan done!")
                if (intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) == true) {
                    performEthernetWifiSwitch(context)
                }
            }
        }, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        // listen ACTION_SCREEN_ON
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {

                val handler = Handler(Looper.getMainLooper())
                for (delay in listOf(5000L, 10000L, 15000L, 30000L)) {
                    handler.postDelayed({
                        Log.i(TAG, "ACTION_SCREEN_ON (delay=$delay) -> startWifiScan")
                        startWifiScan(context)
                    }, delay)
                }
            }
        }, IntentFilter(Intent.ACTION_SCREEN_ON))

        // listen ACTION_TIME_TICK
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (countTimer++ % 5 == 0) { // each 5 minutes
                    Log.i(TAG, "ACTION_TIME_TICK -> startWifiScan")
                    startWifiScan(context)
                }
            }
        }, IntentFilter(Intent.ACTION_TIME_TICK))

        // listen WIFI_STATE_CHANGED_ACTION
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {

                // handle Wifi interface enabled
                if (WifiManager.WIFI_STATE_ENABLED == intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)) {
                    Log.i(TAG, "WIFI_STATE_CHANGED_ACTION: Wifi interface enabled -> startWifiScan")
                    startWifiScan(context)
                }
            }
        }, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

        // listen TRANSPORT_ETHERNET available
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.i(TAG, "TRANSPORT_ETHERNET available -> startWifiScan")
                    startWifiScan(context)
                }
            }
        )

        // listen TRANSPORT_WIFI lost
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    Log.i(TAG, "TRANSPORT_WIFI lost -> startWifiScan")
                    startWifiScan(context)
                }
            }
        )
    }

    fun startWifiScan(context: Context) {
        val wifiManager = context.getSystemService(WifiManager::class.java)

        if (!wifiManager.isWifiEnabled) {
            Log.i(TAG, "startWifiScan: Wifi disabled. Aborting")
            performEthernetWifiSwitch(context)
            return
        }

        Log.i(TAG, "startWifiScan: starting")
        val scanStarted = wifiManager.startScan()

        if (!scanStarted) {
            performEthernetWifiSwitch(context)
        }
    }

    private fun performEthernetWifiSwitch(context: Context) {
        val ethernetConnected = isEthernetNetworkConnected(context)
        val wifiAvailable = isWifiConnectionAvailable(context)

        Log.v(TAG, "ethernetConnected=$ethernetConnected / wifiAvailable=$wifiAvailable")

        val interfaceName = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("ethernet_wifi_interface_name", "eth0")!!

        if (ethernetConnected && wifiAvailable) {
            setEthernetState(interfaceName, false)
        } else if (!ethernetConnected && !wifiAvailable) {
            setEthernetState(interfaceName, true)
        }
    }

    private fun setEthernetState(interfaceName: String, enable: Boolean) {
        Log.v(TAG, "setEthernetState($interfaceName, $enable)")

        val action = if (enable) "up" else "down"
        val command = "ifconfig $interfaceName $action"
        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            proc.waitFor()
        } catch (e: Exception) {
            Log.w(TAG, "Error executing command \"$command\": ${e.message}")
        }
    }

    private fun isWifiConnectionAvailable(context: Context?): Boolean {
        val wifiManager = context?.getSystemService(WifiManager::class.java) ?: return false

        if (!wifiManager.isWifiEnabled) return false

        val currentAps = PreferenceManager.getDefaultSharedPreferences(context)
            .getStringSet("ethernet_wifi_aps", emptySet())!!
        if (currentAps.isEmpty()) return false

        return wifiManager.scanResults.any { currentAps.contains(it.SSID) }
    }


    private fun isEthernetNetworkConnected(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(ConnectivityManager::class.java) ?: return false
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}