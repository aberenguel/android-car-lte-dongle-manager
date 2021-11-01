package org.berenguel.carheadunitconfigurer

import android.app.Application
import android.content.Intent
import android.os.Build
import org.berenguel.carheadunitconfigurer.managers.EthernetWifiManager
import org.berenguel.carheadunitconfigurer.managers.UsbModeSwitchManager

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        UsbModeSwitchManager.init(this)
        EthernetWifiManager.init(this)

        startForegroundService(Intent(this, MainService::class.java))
    }
}