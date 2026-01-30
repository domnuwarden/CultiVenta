package com.example.cultiventa

import android.os.Bundle
import android.util.Log
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

object AppContext {
    private var instance: Context? = null
    fun set(context: Context) {
        instance = context.applicationContext
    }
    fun get(): Context = instance ?: error("Context no inicializado")
}

class MainActivity : ComponentActivity() {
    private var onLoginSuccessAction: (() -> Unit)? = null
    private var onLoginFinishedAction: (() -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this) { taskAuth ->
                        onLoginFinishedAction?.invoke()
                        if (taskAuth.isSuccessful) {
                            Log.d("GoogleLogin", "Usuario registrado en Firebase")
                            onLoginSuccessAction?.invoke()
                        } else {
                            Log.e("GoogleLogin", "Error Firebase: ", taskAuth.exception)
                        }
                    }
            } else {
                onLoginFinishedAction?.invoke()
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Error de Google: ${e.statusCode} - ${e.message}")
            onLoginFinishedAction?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }

        AppContext.set(applicationContext)

        setContent {
            App(
                onGoogleSignIn = { onSuccess, onFinished ->
                    onLoginSuccessAction = onSuccess
                    onLoginFinishedAction = onFinished
                    launchGoogleSignIn()
                }
            )
        }
    }

    private fun launchGoogleSignIn() {
        val webClientId = "1044564835494-f3eps4t8u9ff00pbe8ak3bso2nre50ha.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            try {
                val intent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e("GoogleLogin", "No se pudo lanzar el intent")
                onLoginFinishedAction?.invoke()
            }
        }
    }
}