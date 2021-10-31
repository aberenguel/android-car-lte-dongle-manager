package org.berenguel.carheadunitconfigurer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateWifiApsValues()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateWifiApsValues()

        val wifiManager = requireContext().getSystemService(WifiManager::class.java)
        wifiManager.startScan()

        requireContext().registerReceiver(
            receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(receiver)
    }

    private fun updateWifiApsValues() {
        val wifiManager = requireContext().getSystemService(WifiManager::class.java)
        val entries = wifiManager.scanResults.map { it.SSID }

        findPreference<MultiSelectListPreference>("ethernet_wifi_aps")?.apply {
            val allEntries = (this.values + entries).distinct().toTypedArray()
            setEntries(allEntries)
            entryValues = allEntries
        }
    }
}