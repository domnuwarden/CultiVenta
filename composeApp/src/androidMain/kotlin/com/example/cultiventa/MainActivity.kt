package com.example.cultiventa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cultiventa.App
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    private var onLoginSuccessAction: (() -> Unit)? = null

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
                        if (taskAuth.isSuccessful) {
                            Log.d("GoogleLogin", "Usuario registrado en Firebase")
                            onLoginSuccessAction?.invoke()
                        } else {
                            Log.e("GoogleLogin", "Fallo al registrar en Firebase", taskAuth.exception)
                        }
                    }
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Error de Google: ${e.statusCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                onGoogleSignIn = { onSuccess ->
                    onLoginSuccessAction = onSuccess
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

        val intent = GoogleSignIn.getClient(this, gso).signInIntent
        googleSignInLauncher.launch(intent)
    }
}