package org.berenguel.carheadunitconfigurer.managers

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object UsbModeSwitchManager {

    private const val USB_MODESWITCH_FILE_NAME = "usb_modeswitch"

    lateinit var usbModeSwitchFile: File

    fun init(context: Context) {
        usbModeSwitchFile = context.getFileStreamPath(USB_MODESWITCH_FILE_NAME)
    }

    private fun extractUsbModeSwitch(context: Context) {
        if (usbModeSwitchFile.exists()) {
            return
        }

        context.assets.open("${Build.SUPPORTED_ABIS[0]}/$USB_MODESWITCH_FILE_NAME")
            .use { assetStream ->
                context.openFileOutput(USB_MODESWITCH_FILE_NAME, Context.MODE_PRIVATE)
                    .use { fileStream ->
                        assetStream.copyTo(fileStream)
                    }
            }
        usbModeSwitchFile.setExecutable(true)
    }

    suspend fun execute(context: Context) {
        withContext(Dispatchers.IO) {
            extractUsbModeSwitch(context)
            val proc = Runtime.getRuntime()
                .exec(arrayOf("su", "-c", "${usbModeSwitchFile.absolutePath} -v 12d1 -p 157c -u 2"))
            proc.waitFor()
        }
    }


}