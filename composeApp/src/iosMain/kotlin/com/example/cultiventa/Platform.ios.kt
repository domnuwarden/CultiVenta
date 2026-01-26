package com.example.cultiventa

import platform.UIKit.UIDevice
import platform.posix.exit
import dev.gitlive.firebase.storage.Data
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UserNotifications.*
import platform.Foundation.*

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun matarAplicacion() {
    exit(0)
}

@OptIn(ExperimentalForeignApi::class)
actual fun crearData(bytes: ByteArray): Data {
    val nsData = bytes.usePinned {
        NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong())
    }
    return Data(nsData)
}

actual fun programarNotificacionLocal(titulo: String, mensaje: String, tiempoMilis: Long) {
    val center = UNUserNotificationCenter.currentNotificationCenter()

    center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { granted, _ ->
        if (granted) {
            val content = UNMutableNotificationContent().apply {
                setTitle(titulo)
                setBody(mensaje)
                setSound(UNNotificationSound.defaultSound)
            }

            // Calculamos cuánto tiempo falta desde ahora
            val ahoraMilis = NSDate().timeIntervalSince1970 * 1000
            val segundosDeEspera = (tiempoMilis - ahoraMilis.toLong()) / 1000.0

            if (segundosDeEspera > 0) {
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                    segundosDeEspera,
                    repeats = false
                )

                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = NSUUID().UUIDString(),
                    content = content,
                    trigger = trigger
                )

                center.addNotificationRequest(request) { error ->
                    if (error != null) println("Error en notificación iOS: ${error.localizedDescription}")
                }
            }
        }
    }
}

actual fun cancelarNotificacionesPlanta(idBancal: String) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.getPendingNotificationRequestsWithCompletionHandler { requests ->
        val identifiers = requests?.filter {
            (it as UNNotificationRequest).identifier.contains(idBancal)
        }?.map { (it as UNNotificationRequest).identifier } ?: emptyList<String>()

        if (identifiers.isNotEmpty()) {
            center.removePendingNotificationRequestsWithIdentifiers(identifiers as List<String>)
        }
    }
}