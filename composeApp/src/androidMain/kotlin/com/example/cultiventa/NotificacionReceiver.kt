package com.example.cultiventa

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlin.time.Clock

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "¡Tu cosecha está lista!"
        val mensaje = intent.getStringExtra("mensaje") ?: "Entra a recolectar tus productos."
        val channelId = "cultiventa_alerts"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Alertas de Cultivo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de crecimiento de plantas"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(Clock.System.now().toEpochMilliseconds().toInt(), notification)
    }
}