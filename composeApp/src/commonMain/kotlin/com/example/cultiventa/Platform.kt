package com.example.cultiventa

import dev.gitlive.firebase.storage.Data

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun matarAplicacion()
expect fun crearData(bytes: ByteArray): Data

expect fun programarNotificacionLocal(titulo: String, mensaje: String, tiempoMilis: Long)

expect fun cancelarNotificacionesPlanta(idBancal: String)