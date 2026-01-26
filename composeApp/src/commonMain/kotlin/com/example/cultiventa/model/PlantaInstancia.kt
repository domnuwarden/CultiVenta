package com.example.cultiventa.model

import kotlinx.serialization.Serializable

@Serializable
data class PlantaInstancia(
    val nombreSemilla: String,
    val tiempoPlante: Long,
    val tiempoSed: Long? = null,
    val tiempoPlaga: Long? = null,
    val estaMuerta: Boolean = false
)