package org.berenguel.carheadunitconfigurer.managers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


object UsbResetManager {

    suspend fun execute() {
        withContext(Dispatchers.IO) {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "setprop sys.usb.config none"))
            proc.waitFor()
        }
    }
}