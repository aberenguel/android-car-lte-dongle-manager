package org.berenguel.carheadunitconfigurer

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.berenguel.carheadunitconfigurer.managers.EthernetWifiManager

class MainService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 10
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val channel =
            NotificationChannelCompat.Builder("default", NotificationManager.IMPORTANCE_LOW)
                .setName(getString(R.string.service_notification_group_name))
                .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channel.id)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setNumber(0)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.service_notification_title))
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // tasks when the service is started
        EthernetWifiManager.startWifiScan(this)

        return START_STICKY
    }
}