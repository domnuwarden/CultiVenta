package com.example.cultiventa.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cultiventa.model.PlantaInstancia
import com.example.cultiventa.model.GameData
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun CuadritoTierra(
    id: String,
    planta: PlantaInstancia?,
    onUpdateSalud: (String, PlantaInstancia) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var fase by remember { mutableStateOf("") }
    var alert by remember { mutableStateOf("") }
    var crono by remember { mutableStateOf("") }

    LaunchedEffect(planta, id) {
        if (planta != null) {
            while (true) {
                val ahora = GameData.obtenerTiempoActual()
                val trans = ahora - planta.tiempoPlante
                val total = GameData.obtenerTiempoCrecimiento(planta.nombreSemilla)
                val rest = total - trans

                if (!planta.estaMuerta) {
                    if (planta.tiempoSed != null && ahora - planta.tiempoSed > GameData.hora) {
                        onUpdateSalud(id, planta.copy(estaMuerta = true))
                    }
                    if (planta.tiempoPlaga != null && ahora - planta.tiempoPlaga > 15 * GameData.minuto) {
                        onUpdateSalud(id, planta.copy(estaMuerta = true))
                    }
                    if (trans < total) {
                        val ratio = if(planta.nombreSemilla == "Semillas Lechuga") 5 else 2
                        if (Random.nextInt(1000) < ratio && planta.tiempoSed == null) onUpdateSalud(id, planta.copy(tiempoSed = ahora))
                        if (Random.nextInt(2000) < ratio && planta.tiempoPlaga == null) onUpdateSalud(id, planta.copy(tiempoPlaga = ahora))
                    }
                }

                fase = when {
                    planta.estaMuerta -> "🥀"
                    trans >= total -> "✨"
                    trans > total / 2 -> "🌿"
                    else -> "🌱"
                }
                alert = when {
                    planta.tiempoPlaga != null -> "🐛"
                    planta.tiempoSed != null -> "💧"
                    else -> ""
                }
                crono = if (rest > 0 && !planta.estaMuerta) {
                    if(rest > GameData.hora) "${rest/GameData.hora}h" else "${(rest/60000)%60}m"
                } else ""
                delay(2000)
            }
        } else { fase = ""; alert = ""; crono = "" }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color(0xFF3E2723).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fase, fontSize = 16.sp)
            if (alert.isNotEmpty() && planta?.estaMuerta == false) Text(alert, fontSize = 12.sp)
            if (crono.isNotEmpty()) Text(crono, fontSize = 7.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun BancalVisual(
    id: Int,
    esSeleccionado: Boolean,
    tieneSed: Boolean,
    estaBloqueado: Boolean = false,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition()
    val borderColor by transition.animateColor(
        initialValue = if (esSeleccionado) Color(0xFF4CAF50) else Color(0xFF8D6E63),
        targetValue = when {
            estaBloqueado -> Color.DarkGray
            tieneSed -> Color(0xFF2196F3)
            esSeleccionado -> Color(0xFF8BC34A)
            else -> Color(0xFF8D6E63)
        },
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).shadow(2.dp, RoundedCornerShape(10.dp))
            .background(if (estaBloqueado) Color.Gray.copy(alpha = 0.6f) else Color(0xFF5D4037), RoundedCornerShape(10.dp))
            .border(4.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (estaBloqueado) Text("🔒", fontSize = 20.sp)
        else Text("#${id + 1}", color = Color.White.copy(alpha = 0.2f), fontSize = 22.sp)
    }
}