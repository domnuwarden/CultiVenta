package com.example.cultiventa

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    App(onGoogleSignIn = { _ -> /* Lógica futura para iOS */ })
}