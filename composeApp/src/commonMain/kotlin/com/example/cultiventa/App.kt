package com.example.cultiventa

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.cultiventa.ui.LoginScreen

@Composable
fun App(onGoogleSignIn: (onSuccess: () -> Unit) -> Unit) {
    MaterialTheme {
        Navigator(LoginScreen(onGoogleSignIn = onGoogleSignIn))
    }
}