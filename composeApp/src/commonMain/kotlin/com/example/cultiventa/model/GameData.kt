package com.example.cultiventa.model

import kotlin.time.Clock

object GameData {
    val minuto = 60_000L
    val hora = 60 * minuto

    fun obtenerTiempoActual(): Long = Clock.System.now().toEpochMilliseconds()

    fun obtenerTiempoCrecimiento(semilla: String): Long = when (semilla) {
        "Semillas Lechuga" -> 15 * minuto
        "Semillas Calabacin" -> 1 * hora
        "Semillas Tomate" -> 4 * hora
        "Semillas Pimiento" -> 8 * hora
        "Semillas Berenjena" -> 12 * hora
        "Semillas Zanahoria" -> 24 * hora
        else -> 5 * minuto
    }

    fun obtenerRecompensa(semilla: String): Int = when (semilla) {
        "Semillas Lechuga" -> 60
        "Semillas Calabacin" -> 100
        "Semillas Tomate" -> 150
        "Semillas Pimiento" -> 250
        "Semillas Berenjena" -> 400
        "Semillas Zanahoria" -> 1500
        else -> 0
    }
}