package com.example.cultiventa.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

data class LoginScreen(val onGoogleSignIn: (onSuccess: () -> Unit) -> Unit) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LoginContent(
            onGoogleSignIn = {
                onGoogleSignIn {
                    navigator.replaceAll(MainLobbyScreen)
                }
            }
        )
    }
}

object MainLobbyScreen : Screen {
    @Composable
    override fun Content() {
        LobbyContainer()
    }
}