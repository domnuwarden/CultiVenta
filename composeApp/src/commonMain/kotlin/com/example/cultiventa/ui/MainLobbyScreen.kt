package com.example.cultiventa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.example.cultiventa.model.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

object MainLobbyScreen : Screen {
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var datosUsuario by remember { mutableStateOf(UsuarioDatos()) }

        LaunchedEffect(Unit) {
            val cargados = cargarDatosUsuario()
            datosUsuario = cargados
            actualizarDatosUsuario(cargados)
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
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv[semilla] = cantidad - 1
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = PlantaInstancia(semilla, GameData.obtenerTiempoActual())
                                        val nuevosDatos = datosUsuario.copy(inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            },
                            onQuitarOCosechar = { id ->
                                val planta = datosUsuario.plantas_activas[id] ?: return@Content
                                val total = GameData.obtenerTiempoCrecimiento(planta.nombreSemilla)
                                val ahora = GameData.obtenerTiempoActual()
                                scope.launch {
                                    val nuevoInv = datosUsuario.inventario.toMutableMap()
                                    var monedas = datosUsuario.monedas
                                    var ganado = datosUsuario.dinero_ganado
                                    var logradas = datosUsuario.cosechas_logradas
                                    var perdidas = datosUsuario.cosechas_perdidas
                                    if (ahora - planta.tiempoPlante >= total && !planta.estaMuerta) {
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
                                    val nuevosDatos = datosUsuario.copy(monedas = monedas, dinero_ganado = ganado, cosechas_logradas = logradas, cosechas_perdidas = perdidas, inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            },
                            onRegar = { id ->
                                val p = datosUsuario.plantas_activas[id]
                                val regaderas = datosUsuario.inventario["Regadera PRO"] ?: 0
                                if (p != null && regaderas > 0) {
                                    scope.launch {
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv["Regadera PRO"] = regaderas - 1
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = p.copy(tiempoSed = null)
                                        val nuevosDatos = datosUsuario.copy(inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            },
                            onCurar = { id ->
                                val p = datosUsuario.plantas_activas[id]
                                val medicinas = datosUsuario.inventario["Antiplagas BIO"] ?: 0
                                if (p != null && medicinas > 0) {
                                    scope.launch {
                                        val nuevoInv = datosUsuario.inventario.toMutableMap()
                                        nuevoInv["Antiplagas BIO"] = medicinas - 1
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = p.copy(tiempoPlaga = null)
                                        val nuevosDatos = datosUsuario.copy(inventario = nuevoInv, plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
                                }
                            },
                            onUpdateSalud = { id, nueva ->
                                if (nueva.estaMuerta && !datosUsuario.plantas_activas[id]!!.estaMuerta) {
                                    scope.launch {
                                        val nuevasPlantas = datosUsuario.plantas_activas.toMutableMap()
                                        nuevasPlantas[id] = nueva
                                        val nuevosDatos = datosUsuario.copy(plantas_activas = nuevasPlantas)
                                        datosUsuario = nuevosDatos
                                        actualizarDatosUsuario(nuevosDatos)
                                    }
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
                                    val cantidad = nuevoInv[nombre] ?: 0
                                    if (cantidad > 1) nuevoInv[nombre] = cantidad - 1 else nuevoInv.remove(nombre)
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
                                "${datosUsuario.plantas_activas.keys.map { it.split("-")[0].toInt() }.distinct().size}",
                                "${datosUsuario.dinero_ganado}",
                                "${datosUsuario.cosechas_logradas}",
                                "${datosUsuario.cosechas_perdidas}"
                            ),
                            inventario = datosUsuario.inventario,
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
                            onAcelerarCultivos = { minutos ->
                                scope.launch {
                                    val milisegundosReducir = (minutos ?: 0) * 60 * 1000L
                                    val nuevasPlantas = datosUsuario.plantas_activas.mapValues { (_, p) ->
                                        p.copy(tiempoPlante = p.tiempoPlante - milisegundosReducir)
                                    }
                                    val nuevosDatos = datosUsuario.copy(plantas_activas = nuevasPlantas)
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            },
                            onForzarPeligro = {
                                scope.launch {
                                    val ahora = GameData.obtenerTiempoActual()
                                    val nuevasPlantas = datosUsuario.plantas_activas.mapValues { (_, p) ->
                                        p.copy(tiempoSed = ahora, tiempoPlaga = ahora)
                                    }
                                    val nuevosDatos = datosUsuario.copy(plantas_activas = nuevasPlantas)
                                    datosUsuario = nuevosDatos
                                    actualizarDatosUsuario(nuevosDatos)
                                }
                            },
                            onResetProgreso = {
                                scope.launch {
                                    val reset = UsuarioDatos()
                                    datosUsuario = reset
                                    actualizarDatosUsuario(reset)
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