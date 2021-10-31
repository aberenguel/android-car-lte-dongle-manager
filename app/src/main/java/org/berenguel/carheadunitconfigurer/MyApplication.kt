package org.berenguel.carheadunitconfigurer

import android.app.Application
import org.berenguel.carheadunitconfigurer.managers.EthernetWifiManager
import org.berenguel.carheadunitconfigurer.managers.UsbModeSwitchManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        UsbModeSwitchManager.init(this)
        EthernetWifiManager.init(this)
    }
}