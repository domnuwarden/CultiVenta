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
import com.example.cultiventa.programarNotificacionLocal
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

    val transition = rememberInfiniteTransition()
    val alphaAnim by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse)
    )

    LaunchedEffect(planta, id) {
        if (planta != null && !planta.estaMuerta) {
            while (true) {
                val ahora = GameData.obtenerTiempoActual()
                val trans = ahora - planta.tiempoPlante
                val total = GameData.obtenerTiempoCrecimiento(planta.nombreSemilla)
                val muertePlaga = planta.tiempoPlaga?.let { tPlaga -> (ahora - tPlaga) >= (15 * GameData.minuto) } ?: false
                val muerteSed = planta.tiempoSed?.let { tSed -> (ahora - tSed) >= GameData.hora } ?: false

                if (muertePlaga || muerteSed) {
                    onUpdateSalud(id, planta.copy(estaMuerta = true, tiempoPlaga = null, tiempoSed = null))
                    break
                }

                if (trans < total) {
                    val ratio = if(planta.nombreSemilla == "Semillas Lechuga") 5 else 2
                    if (Random.nextInt(1000) < ratio && planta.tiempoSed == null) {
                        onUpdateSalud(id, planta.copy(tiempoSed = ahora))
                        programarNotificacionLocal(
                            titulo = "💧 ¿Necesita agua?",
                            mensaje = "Pásate por el huerto, tu ${planta.nombreSemilla} tiene sed.",
                            tiempoMilis = ahora + 1000L // 1 segundo de margen para el sistema
                        )
                    }
                    if (Random.nextInt(2000) < ratio && planta.tiempoPlaga == null) {
                        onUpdateSalud(id, planta.copy(tiempoPlaga = ahora))
                        programarNotificacionLocal(
                            titulo = "🐛 ¡Ojo con los bichos!",
                            mensaje = "Han aparecido plagas en tu ${planta.nombreSemilla}, ¡revisa!",
                            tiempoMilis = ahora + 1000L
                        )
                    }
                }
                fase = when {
                    trans >= total -> "✨"
                    trans > total / 2 -> "🌿"
                    else -> "🌱"
                }
                alert = when {
                    planta.tiempoPlaga != null -> "🐛"
                    planta.tiempoSed != null -> "💧"
                    else -> ""
                }
                delay(1000)
            }
        } else if (planta?.estaMuerta == true) {
            fase = "🥀"
            alert = ""
        } else {
            fase = ""
            alert = ""
        }
    }

    val colorAlerta = when {
        planta?.tiempoPlaga != null && !planta.estaMuerta -> Color(0xFFFF9800)
        planta?.tiempoSed != null && !planta.estaMuerta -> Color(0xFF2196F3)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF3E2723).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .border(
                width = if (alert.isNotEmpty() && planta?.estaMuerta == false) 2.dp else 0.5.dp,
                color = if (alert.isNotEmpty() && planta?.estaMuerta == false) colorAlerta.copy(alpha = alphaAnim) else colorAlerta,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fase, fontSize = 24.sp)
            if (alert.isNotEmpty() && planta?.estaMuerta == false) {
                Text(alert, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BancalVisual(
    id: Int,
    esSeleccionado: Boolean,
    tieneSed: Boolean,
    tienePlaga: Boolean,
    estaBloqueado: Boolean = false,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition()
    val borderColor by transition.animateColor(
        initialValue = if (esSeleccionado) Color(0xFF4CAF50) else Color(0xFF8D6E63),
        targetValue = when {
            estaBloqueado -> Color.DarkGray
            tienePlaga -> Color(0xFFFF9800)
            tieneSed -> Color(0xFF2196F3)
            esSeleccionado -> Color(0xFF8BC34A)
            else -> Color(0xFF8D6E63)
        },
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .background(if (estaBloqueado) Color.Gray.copy(alpha = 0.6f) else Color(0xFF5D4037), RoundedCornerShape(10.dp))
            .border(4.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (estaBloqueado) Text("🔒", fontSize = 20.sp)
        else Text("#${id + 1}", color = Color.White.copy(alpha = 0.2f), fontSize = 22.sp)
    }
}