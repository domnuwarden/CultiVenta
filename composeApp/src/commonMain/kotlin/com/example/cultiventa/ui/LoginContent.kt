package com.example.cultiventa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import cultiventa.composeapp.generated.resources.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun LoginContent(
    onGoogleSignIn: () -> Unit
) {
    val auth = Firebase.auth
    val scope = rememberCoroutineScope()

    var mostrarFormularioEmail by remember { mutableStateOf(false) }
    var esModoRegistro by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- PANTALLA PRINCIPAL ---
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, fontSize = 48.sp)) { append("Culti") }
                    withStyle(style = SpanStyle(color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 48.sp)) { append("Venta") }
                }
            )

            Text("Tu mercado agrícola digital", style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = { onGoogleSignIn() },
                modifier = Modifier.fillMaxWidth(0.85f).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(Res.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Iniciar sesión con Google", color = Color(0xFF1F1F1F), fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    esModoRegistro = false
                    mensajeError = ""
                    mostrarFormularioEmail = true
                },
                modifier = Modifier.fillMaxWidth(0.85f).height(54.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2E7D32))
            ) {
                Text("Iniciar sesión con Email", color = Color(0xFF2E7D32), fontSize = 16.sp)
            }
        }

        // --- FORMULARIO SOBREPUESTO ---
        if (mostrarFormularioEmail) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                    .clickable { if(!cargando) mostrarFormularioEmail = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable(enabled = false) {},
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.width(40.dp).height(4.dp).background(Color.LightGray, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(if (esModoRegistro) "Crea tu cuenta" else "Bienvenido de nuevo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                        if (mensajeError.isNotEmpty()) {
                            Text(text = mensajeError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = email, onValueChange = { email = it }, label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32))
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        if (cargando) {
                            CircularProgressIndicator(color = Color(0xFF2E7D32))
                        } else {
                            Button(
                                onClick = {
                                    mensajeError = ""

                                    // 1. VALIDACIÓN MANUAL ANTES DE LLAMAR A FIREBASE
                                    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+$".toRegex()

                                    when {
                                        email.isEmpty() || password.isEmpty() -> {
                                            mensajeError = "Rellena todos los campos"
                                        }
                                        !email.matches(emailPattern) -> {
                                            mensajeError = "El formato del correo no es válido."
                                        }
                                        password.length < 6 -> {
                                            mensajeError = "La contraseña debe tener al menos 6 caracteres."
                                        }
                                        else -> {
                                            // 2. SI PASA LAS VALIDACIONES, INTENTAMOS FIREBASE
                                            cargando = true
                                            scope.launch {
                                                try {
                                                    if (esModoRegistro) {
                                                        auth.createUserWithEmailAndPassword(email, password)
                                                        mensajeError = "¡Registro con éxito! Inicia sesión ahora"
                                                        esModoRegistro = false
                                                    } else {
                                                        auth.signInWithEmailAndPassword(email, password)
                                                        onGoogleSignIn()
                                                    }
                                                } catch (e: Exception) {
                                                    val errorMsg = e.message?.lowercase() ?: ""
                                                    mensajeError = when {
                                                        errorMsg.contains("already-in-use") -> "Este email ya está registrado."
                                                        errorMsg.contains("network") -> "Error de red. Revisa tu conexión."
                                                        else -> "No existe la cuenta. Por favor, regístrate primeramente."
                                                    }
                                                } finally {
                                                    cargando = false
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text(if (esModoRegistro) "Crear cuenta" else "Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = {
                                esModoRegistro = !esModoRegistro
                                mensajeError = ""
                            }) {
                                Text(if (esModoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí", color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        }
    }
}