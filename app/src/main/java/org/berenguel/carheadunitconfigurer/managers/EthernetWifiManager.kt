package org.berenguel.carheadunitconfigurer.managers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.preference.PreferenceManager


object EthernetWifiManager {

    private const val TAG = "EthernetWifiManager"

    fun init(context: Context) {

        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Wi-Fi scan done!")
                context?.apply {
                    performEthernetWifiSwitch(this)
                }
            }
        }, IntentFilter().apply {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        })

        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Bluetooth connected")
                context?.apply {
                    startWifiScan(this)
                    Handler(Looper.getMainLooper()).let { handler ->
                        handler.postDelayed({ startWifiScan(this) }, 1000L)
                        handler.postDelayed({ startWifiScan(this) }, 3000L)
                        handler.postDelayed({ startWifiScan(this) }, 5000L)
                    }
                }
            }

        }, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
    }

    fun startWifiScan(context: Context) {
        val wifiManager = context.getSystemService(WifiManager::class.java)

        if (!wifiManager.isWifiEnabled) {
            Log.i(TAG, "Wifi disabled. Aborting")
            return
        }

        Log.i(TAG, "Wi-Fi scan starting")
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