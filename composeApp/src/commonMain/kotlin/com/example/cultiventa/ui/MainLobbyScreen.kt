package com.example.cultiventa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.example.cultiventa.crearData
import com.example.cultiventa.model.*
import com.example.cultiventa.programarNotificacionLocal
import com.example.cultiventa.cancelarNotificacionesPlanta
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.launch

object MainLobbyScreen : Screen {
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var datosUsuario by remember { mutableStateOf(UsuarioDatos()) }

        LaunchedEffect(Unit) {
            val cargados = cargarDatosUsuario()
            datosUsuario = cargados
        }

        TabNavigator(BocetosTab) { tabNavigator ->
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                        TabNavigationItem(BocetosTab)
                        TabNavigationItem(TiendaTab)
                        TabNavigationItem(PerfilTab)
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    when (val currentTab = tabNavigator.current) {
                        is BocetosTab -> currentTab.Content(
                            dinero = datosUsuario.monedas,
                            inventario = datosUsuario.inventario,
                            plantas = datosUsuario.plantas_activas,
                            desbloqueados = datosUsuario.bancales_desbloqueados,
                            onPlantar = { id, semilla ->
                                val cantidad = datosUsuario.inventario[semilla] ?: 0
                                if (cantidad > 0) {
                                    scope.launch {
                                        val ahora = GameData.obtenerTiempoActual()
                                        val tiempoCrecimiento = GameData.obtenerTiempoCrecimiento(semilla)
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv[semilla] = cantidad - 1
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = PlantaInstancia(semilla, ahora)
                                        val nuevosDatos = datosUsuario.copy(inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                        programarNotificacionLocal(
                                            titulo = "✨ ¡Cosecha lista!",
                                            mensaje = "Tu $semilla ya se puede recoger.",
                                            tiempoMilis = ahora + tiempoCrecimiento
                                        )
                                    }
                                }
                            },
                            onQuitarOCosechar = { id ->
                                cancelarNotificacionesPlanta(id)
                                val planta = datosUsuario.plantas_activas[id] ?: return@Content
                                scope.launch {
                                    val ahora = GameData.obtenerTiempoActual()
                                    val tiempoTotal = GameData.obtenerTiempoCrecimiento(planta.nombreSemilla)
                                    val nuevoInv = datosUsuario.inventario.toMutableMap()
                                    var monedas = datosUsuario.monedas
                                    var ganado = datosUsuario.dinero_ganado
                                    var logradas = datosUsuario.cosechas_logradas
                                    var perdidas = datosUsuario.cosechas_perdidas

                                    if (ahora - planta.tiempoPlante >= tiempoTotal && !planta.estaMuerta) {
                                        val reward = GameData.obtenerRecompensa(planta.nombreSemilla)
                                        monedas += reward
                                        ganado += reward
                                        logradas++
                                    } else if (planta.estaMuerta) {
                                        perdidas++
                                    } else {
                                        nuevoInv[planta.nombreSemilla] = (nuevoInv[planta.nombreSemilla] ?: 0) + 1
                                    }

                                    val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                    nuevasPlantas.remove(id)
                                    val nuevosDatos = datosUsuario.copy(
                                        monedas = monedas,
                                        dinero_ganado = ganado,
                                        cosechas_logradas = logradas,
                                        cosechas_perdidas = perdidas,
                                        inventario = nuevoInv,
                                        plantas_activas = nuevasPlantas
                                    )
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            },
                            onRegar = { id ->
                                cancelarNotificacionesPlanta(id)
                                val p = datosUsuario.plantas_activas[id]
                                if (p != null && (datosUsuario.inventario["Regadera PRO"] ?: 0) > 0) {
                                    scope.launch {
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv["Regadera PRO"] = (nuevoInv["Regadera PRO"] ?: 1) - 1
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = p.copy(tiempoSed = null)
                                        val nuevosDatos = datosUsuario.copy(inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            },
                            onCurar = { id ->
                                cancelarNotificacionesPlanta(id)
                                val p = datosUsuario.plantas_activas[id]
                                if (p != null && (datosUsuario.inventario["Antiplagas BIO"] ?: 0) > 0) {
                                    scope.launch {
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv["Antiplagas BIO"] = (nuevoInv["Antiplagas BIO"] ?: 1) - 1
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = p.copy(tiempoPlaga = null)
                                        val nuevosDatos = datosUsuario.copy(inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            },
                            onUpdateSalud = { id, nueva ->
                                if (datosUsuario.plantas_activas[id] != nueva) {
                                    val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                    nuevasPlantas[id] = nueva
                                    datosUsuario = datosUsuario.copy(plantas_activas = nuevasPlantas)
                                    scope.launch { actualizarDatosUsuario(datosUsuario) }
                                }
                            },
                            onDesbloquear = { i, cost ->
                                if (datosUsuario.monedas >= cost) {
                                    scope.launch {
                                        val nuevosBancales = datosUsuario.bancales_desbloqueados.toMutableList()
                                        nuevosBancales.add(i)
                                        val nuevosDatos = datosUsuario.copy(monedas = datosUsuario.monedas - cost, bancales_desbloqueados = nuevosBancales)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            },
                            onEliminarItem = { nombre ->
                                scope.launch {
                                    val nuevoInv = datosUsuario.inventario.toMutableMap()
                                    val cant = nuevoInv[nombre] ?: 0
                                    if (cant > 1) nuevoInv[nombre] = cant - 1 else nuevoInv.remove(nombre)
                                    val nuevosDatos = datosUsuario.copy(inventario = nuevoInv)
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            }
                        )

                        is TiendaTab -> currentTab.Content(
                            dinero = datosUsuario.monedas,
                            onCompra = { precio, nombre ->
                                if (datosUsuario.monedas >= precio) {
                                    scope.launch {
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv[nombre] = (nuevoInv[nombre] ?: 0) + 1
                                        val nuevosDatos = datosUsuario.copy(monedas = datosUsuario.monedas - precio, inventario = nuevoInv)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            }
                        )

                        is PerfilTab -> currentTab.Content(
                            statsReal = listOf(
                                "${datosUsuario.plantas_activas.size}",
                                "${datosUsuario.dinero_ganado}",
                                "${datosUsuario.cosechas_logradas}",
                                "${datosUsuario.cosechas_perdidas}"
                            ),
                            inventario = datosUsuario.inventario,
                            avatarUrl = datosUsuario.avatarUrl,
                            onModificarDinero = { extra ->
                                scope.launch {
                                    val nuevosDatos = datosUsuario.copy(monedas = datosUsuario.monedas + extra)
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            },
                            onAddInventario = { item ->
                                scope.launch {
                                    val nuevoInv = datosUsuario.inventario.toMutableMap()
                                    nuevoInv[item] = (nuevoInv[item] ?: 0) + 1
                                    val nuevosDatos = datosUsuario.copy(inventario = nuevoInv)
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            },
                            onAcelerarCultivos = { min ->
                                val ms = (min ?: 0L) * 60000L
                                val nuevas = datosUsuario.plantas_activas.mapValues { (_, p) ->
                                    p.copy(tiempoPlante = p.tiempoPlante - ms)
                                }
                                datosUsuario = datosUsuario.copy(plantas_activas = nuevas)
                                scope.launch { actualizarDatosUsuario(datosUsuario) }
                            },
                            onForzarSed = {
                                val ahora = GameData.obtenerTiempoActual()
                                val nuevas = datosUsuario.plantas_activas.mapValues { (_, p) -> p.copy(tiempoSed = ahora) }
                                datosUsuario = datosUsuario.copy(plantas_activas = nuevas)
                                scope.launch { actualizarDatosUsuario(datosUsuario) }
                            },
                            onForzarPlaga = {
                                val ahora = GameData.obtenerTiempoActual()
                                val nuevas = datosUsuario.plantas_activas.mapValues { (_, p) -> p.copy(tiempoPlaga = ahora) }
                                datosUsuario = datosUsuario.copy(plantas_activas = nuevas)
                                scope.launch { actualizarDatosUsuario(datosUsuario) }
                            },
                            onResetProgreso = {
                                scope.launch {
                                    datosUsuario.plantas_activas.keys.forEach { cancelarNotificacionesPlanta(it) }
                                    val reset = UsuarioDatos()
                                    datosUsuario = reset
                                    actualizarDatosUsuario(reset)
                                }
                            },
                            onSubirFoto = { file ->
                                scope.launch {
                                    val uid = Firebase.auth.currentUser?.uid ?: return@launch
                                    try {
                                        val bytes = file.readBytes()
                                        val storageRef = Firebase.storage.reference("Avatares/$uid.jpg")
                                        val dataParaSubir = crearData(bytes)
                                        storageRef.putData(dataParaSubir)
                                        val url = storageRef.getDownloadUrl()
                                        val nuevosDatos = datosUsuario.copy(avatarUrl = url)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            onLogout = { scope.launch { Firebase.auth.signOut() } }
                        )
                    }
                }
            }
        }
    }
}