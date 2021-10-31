package org.berenguel.carheadunitconfigurer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*
import org.berenguel.carheadunitconfigurer.managers.EthernetWifiManager

class BootCompletedReceiver : BroadcastReceiver() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("BootCompletedReceiver", "Exception", throwable)
    }
    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + coroutineExceptionHandler)

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {

        coroutineScope.launch {
            //UsbResetCommand.execute()
            EthernetWifiManager.startWifiScan(context)

        }
    }
}