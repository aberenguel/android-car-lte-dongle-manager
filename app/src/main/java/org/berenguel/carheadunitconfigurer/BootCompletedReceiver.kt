package org.berenguel.carheadunitconfigurer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*
import org.berenguel.carheadunitconfigurer.managers.EthernetWifiManager

class BootCompletedReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        // just to start the app...
    }
}