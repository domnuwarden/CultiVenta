package com.example.cultiventa.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import cultiventa.composeapp.generated.resources.*
import cafe.adriel.voyager.navigator.tab.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import com.example.cultiventa.model.*
import com.example.cultiventa.ui.components.*
import coil3.compose.AsyncImage

@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF2E7D32),
            selectedTextColor = Color(0xFF2E7D32),
            indicatorColor = Color(0xFFE8F5E9)
        )
    )
}

object BocetosTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(0u, "Bancales", painterResource(Res.drawable.ic_home))

    @Composable override fun Content() {}

    @Composable
    fun Content(
        dinero: Int,
        inventario: Map<String, Int>,
        plantas: Map<String, PlantaInstancia>,
        desbloqueados: List<Int>,
        onPlantar: (String, String) -> Unit,
        onQuitarOCosechar: (String) -> Unit,
        onRegar: (String) -> Unit,
        onCurar: (String) -> Unit,
        onUpdateSalud: (String, PlantaInstancia) -> Unit,
        onDesbloquear: (Int, Int) -> Unit,
        onEliminarItem: (String) -> Unit
    ) {
        var bancalSeleccionado by remember { mutableStateOf<Int?>(null) }
        var bancalParaComprar by remember { mutableStateOf<Int?>(null) }
        val costeActual = 500 + ((desbloqueados.size - 2) * 500)

        if (bancalParaComprar != null) {
            AlertDialog(
                onDismissRequest = { bancalParaComprar = null },
                title = { Text("Expandir Huerto") },
                text = { Text("Limpiar este terreno cuesta 💰 $costeActual.") },
                confirmButton = {
                    Button(onClick = { onDesbloquear(bancalParaComprar!!, costeActual); bancalParaComprar = null }, enabled = dinero >= costeActual) { Text("Desbloquear") }
                },
                dismissButton = { TextButton(onClick = { bancalParaComprar = null }) { Text("Cancelar") } }
            )
        }

        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F8E9)).padding(12.dp, 8.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mi Huerto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("🌿 Terrenos: ${desbloqueados.size} / 6", fontSize = 12.sp, color = Color.Gray)
                    }
                    Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFFFC107))) {
                        Text("💰 $dinero", modifier = Modifier.padding(10.dp, 4.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                    }
                }
            }
            Text("Mis Bancales", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Box(modifier = Modifier.weight(0.4f)) {
                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(6) { index ->
                        val estaDesbloqueado = desbloqueados.contains(index)
                        val bancalTieneSed = plantas.keys.filter { it.startsWith("$index-") }.any { id -> plantas[id]?.tiempoSed != null }

                        BancalVisual(
                            id = index,
                            esSeleccionado = bancalSeleccionado == index,
                            tieneSed = bancalTieneSed,
                            estaBloqueado = !estaDesbloqueado,
                            onClick = { if (estaDesbloqueado) bancalSeleccionado = index else bancalParaComprar = index }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().weight(0.6f).background(Color(0xFFD7CCC8), RoundedCornerShape(12.dp)).border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp)).padding(10.dp)) {
                if (bancalSeleccionado != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Gestión Bancal #${bancalSeleccionado!! + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZonaCultivo(bancalSeleccionado!!, 0, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                            ZonaCultivo(bancalSeleccionado!!, 1, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZonaCultivo(bancalSeleccionado!!, 2, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                            ZonaCultivo(bancalSeleccionado!!, 3, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                        }
                    }
                } else { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Toca un terreno desbloqueado", color = Color(0xFF795548)) } }
            }
        }
    }
}

object TiendaTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(1u, "Tienda", painterResource(Res.drawable.ic_shop))

    @Composable override fun Content() {}

    @Composable
    fun Content(dinero: Int, onCompra: (Int, String) -> Unit) {
        val listaProductos = listOf(
            ProductoItem("Semillas Tomate", 50), ProductoItem("Semillas Lechuga", 30),
            ProductoItem("Semillas Berenjena", 120), ProductoItem("Semillas Zanahoria", 450),
            ProductoItem("Semillas Pimiento", 90), ProductoItem("Semillas Calabacin", 40),
            ProductoItem("Regadera PRO", 200), ProductoItem("Antiplagas BIO", 180)
        )
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFDF5E6)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mercado Ambulante", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFFFC107)), shadowElevation = 2.dp) {
                    Text("💰 $dinero", modifier = Modifier.padding(10.dp, 4.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listaProductos.size) { index ->
                    CardProducto(listaProductos[index], onCompra = { onCompra(listaProductos[index].precio, listaProductos[index].nombre) })
                }
            }
        }
    }
}

data class ProductoItem(val nombre: String, val precio: Int)

@Composable
fun CardProducto(producto: ProductoItem, onCompra: () -> Unit) {
    val bucket = "proyectohuerto25-26.firebasestorage.app"
    val (carpeta, prefijo) = when {
        producto.nombre.contains("Semillas") -> "ImgSemillas" to "Bolsa"
        else -> "ImgUtilidades" to ""
    }
    val nombreLimpio = producto.nombre.replace(" ", "").replace("PRO", "Pro").replace("BIO", "Bio")
    val nombreArchivo = "${prefijo}${nombreLimpio}.jpg"
    val imageUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$carpeta%2F$nombreArchivo?alt=media"

    Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEFEBE9)), contentAlignment = Alignment.Center) {
                AsyncImage(model = imageUrl, contentDescription = producto.nombre, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = producto.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723), maxLines = 1)
            Row(modifier = Modifier.background(Color(0xFFFFF8E1), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("💰 ${producto.precio}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFBF360C))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onCompra, modifier = Modifier.fillMaxWidth().height(36.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(8.dp)) {
                Text("Comprar")
            }
        }
    }
}

object PerfilTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(2u, "Perfil", painterResource(Res.drawable.ic_profile))

    @Composable override fun Content() {}

    @Composable
    fun Content(
        statsReal: List<String>,
        inventario: Map<String, Int>,
        onModificarDinero: (Int) -> Unit,
        onAddInventario: (String) -> Unit,
        onAcelerarCultivos: (Long?) -> Unit, // Cambiado para aceptar minutos
        onForzarPeligro: () -> Unit,
        onResetProgreso: () -> Unit, // Nueva función
        onLogout: () -> Unit
    ) {
        val auth = Firebase.auth
        val scope = rememberCoroutineScope()
        val usuarioActual = auth.currentUser
        val developersAutorizados = setOf("L2qDDOMGjrZ4CBjuF1khlZyzGzt1")
        val esMiUsuario = developersAutorizados.contains(usuarioActual?.uid)

        val email = usuarioActual?.email ?: "Usuario"
        var mostrarInfo by remember { mutableStateOf(false) }
        var modoDeveloper by remember { mutableStateOf(false) }
        var minutosAcelerar by remember { mutableStateOf("") } // Estado para el campo de texto

        if (mostrarInfo) {
            AlertDialog(
                onDismissRequest = { mostrarInfo = false },
                title = { Text("Información de CultiVenta", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Tu mercado agrícola digital recreativo.", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                        Spacer(Modifier.height(8.dp))
                        Text("CultiVenta es un juego de simulación estratégica.")
                        Spacer(Modifier.height(8.dp))
                        Text("Soporte: ldrotariu01@gmail.com", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                confirmButton = { TextButton(onClick = { mostrarInfo = false }) { Text("Cerrar") } }
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDF5E6))) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(30.dp).clip(CircleShape).background(Color.White).border(2.dp, Color(0xFF5D4037), CircleShape).clickable { mostrarInfo = true }, contentAlignment = Alignment.Center) { Text("?", color = Color(0xFF5D4037), fontWeight = FontWeight.ExtraBold) }

            if (esMiUsuario) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).size(30.dp).clip(CircleShape).background(if(modoDeveloper) Color(0xFFD4AF37) else Color.White).border(1.dp, Color.LightGray, CircleShape).clickable { modoDeveloper = !modoDeveloper }, contentAlignment = Alignment.Center) { Text("D") }
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Diario de Campo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(40.dp))
                Box(modifier = Modifier.size(100.dp).shadow(4.dp, CircleShape).background(Color.White, CircleShape).border(3.dp, Color(0xFF2E7D32), CircleShape), contentAlignment = Alignment.Center) { Text(email.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) }
                Text(email, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(32.dp))

                if (modoDeveloper && esMiUsuario) {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFD4AF37))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Panel Developer", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onModificarDinero(1000) }, modifier = Modifier.weight(1f)) { Text("+1K") }
                                Button(onClick = onForzarPeligro, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("💀") }
                            }

                            // NUEVA FILA: Aceleración personalizada
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = minutosAcelerar,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) minutosAcelerar = it },
                                    label = { Text("Mins") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(onClick = {
                                    onAcelerarCultivos(minutosAcelerar.toLongOrNull())
                                    minutosAcelerar = ""
                                }) { Text("⚡") }
                            }

                            // NUEVO BOTÓN: Reset de progreso
                            Button(
                                onClick = onResetProgreso,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("RESET TOTAL", color = Color.White)
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFD7CCC8))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Estadísticas del Huerto", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        StatRow("Bancales en uso", statsReal[0], Color(0xFF2E7D32))
                        StatRow("Dinero ganado", statsReal[1], Color(0xFFBF360C))
                        StatRow("Cosechas logradas", statsReal[2], Color(0xFF4CAF50))
                        StatRow("Cosechas perdidas", statsReal[3], Color(0xFFD32F2F))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFD7CCC8))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Mi Mochila", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        if (inventario.isEmpty()) {
                            Text("No tienes objetos.", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            inventario.forEach { (nombre, cantidad) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
                                    Text(nombre, fontSize = 14.sp)
                                    Text("x$cantidad", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { scope.launch { auth.signOut(); onLogout() } }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = Color.Gray); Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun ZonaCultivo(bancalIdx: Int, idZona: Int, inventario: Map<String, Int>, plantas: Map<String, PlantaInstancia>, onPlantar: (String, String) -> Unit, onAccion: (String) -> Unit, onRegar: (String) -> Unit, onCurar: (String) -> Unit, onUpdateSalud: (String, PlantaInstancia) -> Unit, onEliminarItem: (String) -> Unit, modifier: Modifier = Modifier) {
    var mostrarMenuSiembra by remember { mutableStateOf(false) }
    var mostrarMenuGestion by remember { mutableStateOf(false) }
    var cuadritoSel by remember { mutableStateOf("") }

    if (mostrarMenuSiembra) {
        AlertDialog(
            onDismissRequest = { mostrarMenuSiembra = false },
            title = { Text("Inventario") },
            text = {
                Column {
                    inventario.forEach { (nombre, cant) ->
                        if (cant > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                TextButton(modifier = Modifier.weight(1f), onClick = { if(nombre.contains("Semillas")) onPlantar(cuadritoSel, nombre); mostrarMenuSiembra = false }) { Text("$nombre (x$cant)") }
                                Text(text = " [Borrar] ", color = Color.Red, fontSize = 12.sp, modifier = Modifier.clickable { onEliminarItem(nombre) })
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarMenuSiembra = false }) { Text("Cerrar") } }
        )
    }

    if (mostrarMenuGestion) {
        val p = plantas[cuadritoSel]
        AlertDialog(
            onDismissRequest = { mostrarMenuGestion = false },
            title = { Text(if (p?.estaMuerta == true) "Planta Muerta" else "Gestionar") },
            text = { Column { if (p?.estaMuerta == true) Text("Se ha marchitado.") else { if (p?.tiempoSed != null) Text("⚠️ Necesita agua"); if (p?.tiempoPlaga != null) Text("⚠️ Tiene bichos") } } },
            confirmButton = {
                Column {
                    val ahora = GameData.obtenerTiempoActual()
                    val total = GameData.obtenerTiempoCrecimiento(p?.nombreSemilla ?: "")
                    val esMadura = (ahora - (p?.tiempoPlante ?: 0)) >= total
                    if (p?.estaMuerta == true || esMadura) Button(onClick = { onAccion(cuadritoSel); mostrarMenuGestion = false }) { Text(if(p?.estaMuerta == true) "Limpiar" else "Cosechar") }
                    if (p?.tiempoSed != null && !p.estaMuerta) Button(onClick = { onRegar(cuadritoSel); mostrarMenuGestion = false }, enabled = (inventario["Regadera PRO"] ?: 0) > 0) { Text("Regar") }
                    if (p?.tiempoPlaga != null && !p.estaMuerta) Button(onClick = { onCurar(cuadritoSel); mostrarMenuGestion = false }, enabled = (inventario["Antiplagas BIO"] ?: 0) > 0) { Text("Curar") }
                }
            },
            dismissButton = { TextButton(onClick = { onAccion(cuadritoSel); mostrarMenuGestion = false }) { Text("Quitar") } }
        )
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF3E2723).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
        val ids = listOf("A", "B", "C", "D")
        for (i in 0..1) { Row(Modifier.weight(1f)) { for (j in 0..1) {
            val id = "$bancalIdx-$idZona-${ids[i * 2 + j]}"
            CuadritoTierra(id, plantas[id], onUpdateSalud, Modifier.weight(1f)) { cuadritoSel = id; if (plantas[id] == null) mostrarMenuSiembra = true else mostrarMenuGestion = true }
        } } }
    }
}