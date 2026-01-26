package com.example.cultiventa

import android.os.Build
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess
import dev.gitlive.firebase.storage.Data
import android.annotation.SuppressLint

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun matarAplicacion() {
    exitProcess(0)
}

actual fun crearData(bytes: ByteArray): Data = Data(bytes)

@SuppressLint("ScheduleExactAlarm")
actual fun programarNotificacionLocal(titulo: String, mensaje: String, tiempoMilis: Long) {
    try {
        val context = AppContext.get()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Verificamos si realmente tenemos permiso para alarmas exactas (Android 12+)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("titulo", titulo)
            putExtra("mensaje", mensaje)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tiempoMilis.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMilis, pendingIntent)
        } else {
            // Si no hay permiso, usamos una alarma normal (puede retrasarse unos minutos)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMilis, pendingIntent)
        }
    } catch (e: Exception) {
        println("ERROR NOTIFICACIÓN ANDROID: ${e.message}")
    }
}

actual fun cancelarNotificacionesPlanta(idBancal: String) {
    val context = AppContext.get()
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java)
    val ids = listOf(idBancal.hashCode(), idBancal.hashCode() + 1, idBancal.hashCode() + 2)

    ids.forEach { id ->
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}