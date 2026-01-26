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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cultiventa.matarAplicacion
import org.jetbrains.compose.resources.painterResource
import cultiventa.composeapp.generated.resources.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

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
                onClick = {
                    cargando = true
                    onGoogleSignIn()
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(0.85f).height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFF5F5F5)
                ),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cargando && !mostrarFormularioEmail) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF2E7D32), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(Res.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Iniciar sesión con Google", color = Color(0xFF1F1F1F), fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    esModoRegistro = false
                    mensajeError = ""
                    mostrarFormularioEmail = true
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(0.85f).height(54.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2E7D32))
            ) {
                Text("Iniciar sesión con Email", color = Color(0xFF2E7D32), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = { matarAplicacion() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Cerrar Juego", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }

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
                            enabled = !cargando
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            enabled = !cargando
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        if (cargando) {
                            CircularProgressIndicator(color = Color(0xFF2E7D32))
                        } else {
                            Button(
                                onClick = {
                                    cargando = true
                                    scope.launch {
                                        try {
                                            if (esModoRegistro) {
                                                auth.createUserWithEmailAndPassword(email, password)
                                                mensajeError = "¡Registro con éxito!"
                                                esModoRegistro = false
                                            } else {
                                                auth.signInWithEmailAndPassword(email, password)
                                            }
                                        } catch (e: Exception) {
                                            mensajeError = "Error en el acceso."
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text(if (esModoRegistro) "Crear cuenta" else "Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            TextButton(onClick = { esModoRegistro = !esModoRegistro; mensajeError = "" }) {
                                Text(if (esModoRegistro) "¿Ya tienes cuenta?" else "¿No tienes cuenta? Regístrate", color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        }
    }
}