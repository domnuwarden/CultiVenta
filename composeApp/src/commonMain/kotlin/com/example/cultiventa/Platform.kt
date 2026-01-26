package com.example.cultiventa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun matarAplicacion()