package com.example.cultiventa

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.example.cultiventa.ui.LoginScreen
import com.example.cultiventa.ui.MainLobbyScreen
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@Composable
fun App(onGoogleSignIn: (onSuccess: () -> Unit, onFinished: () -> Unit) -> Unit) {
    val auth = Firebase.auth

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    val user by produceState(initialValue = auth.currentUser) {
        auth.authStateChanged.collect { value = it }
    }

    MaterialTheme {
        key(user?.uid) {
            Navigator(
                if (user == null) LoginScreen(onGoogleSignIn = onGoogleSignIn)
                else MainLobbyScreen
            )
        }
    }
}