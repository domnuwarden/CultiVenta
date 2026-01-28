package com.example.cultiventa.ui

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import cafe.adriel.voyager.navigator.tab.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import com.example.cultiventa.model.*
import com.example.cultiventa.ui.components.*
import coil3.compose.AsyncImage
import com.example.cultiventa.matarAplicacion
import cultiventa.composeapp.generated.resources.Res
import cultiventa.composeapp.generated.resources.*
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.intl.Locale

@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF2E7D32),
            selectedTextColor = Color(0xFF2E7D32),
            indicatorColor = Color(0xFFE8F5E9)
        )
    )
}
@Composable
fun obtenerNombreSegunIdioma(nombres: Map<String, String>): String {
    val idiomaSistema = Locale.current.language
    return nombres[idiomaSistema] ?: nombres["es"] ?: "Unknown Item"
}

object BocetosTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(0u, stringResource(Res.string.tab_bancales), painterResource(Res.drawable.ic_home))

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
                title = { Text(stringResource(Res.string.dialog_expandir_titulo)) },
                text = { Text(stringResource(Res.string.dialog_expandir_cuerpo, costeActual)) },
                confirmButton = {
                    Button(onClick = { onDesbloquear(bancalParaComprar!!, costeActual); bancalParaComprar = null }, enabled = dinero >= costeActual) {
                        Text(stringResource(Res.string.btn_desbloquear))
                    }
                },
                dismissButton = { TextButton(onClick = { bancalParaComprar = null }) { Text(stringResource(Res.string.btn_cancelar)) } }
            )
        }

        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F8E9)).padding(12.dp, 8.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.header_mi_huerto), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text(stringResource(Res.string.header_terrenos, desbloqueados.size), fontSize = 12.sp, color = Color.Gray)
                    }
                    Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFFFC107))) {
                        Text("💰 $dinero", modifier = Modifier.padding(10.dp, 4.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                    }
                }
            }
            Text(stringResource(Res.string.label_mis_bancales), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Box(modifier = Modifier.weight(0.4f)) {
                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(6) { index ->
                        val estaDesbloqueado = desbloqueados.contains(index)
                        val bancalTieneSed = plantas.keys.filter { it.startsWith("$index-") }.any { id -> plantas[id]?.tiempoSed != null }
                        val bancalTienePlaga = plantas.keys.filter { it.startsWith("$index-") }.any { id -> plantas[id]?.tiempoPlaga != null }

                        BancalVisual(
                            id = index,
                            esSeleccionado = bancalSeleccionado == index,
                            tieneSed = bancalTieneSed,
                            tienePlaga = bancalTienePlaga,
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
                        Text(stringResource(Res.string.label_gestion_bancal, bancalSeleccionado!! + 1), fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZonaCultivo(bancalSeleccionado!!, 0, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                            ZonaCultivo(bancalSeleccionado!!, 1, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZonaCultivo(bancalSeleccionado!!, 2, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                            ZonaCultivo(bancalSeleccionado!!, 3, inventario, plantas, onPlantar, onQuitarOCosechar, onRegar, onCurar, onUpdateSalud, onEliminarItem, Modifier.weight(1f))
                        }
                    }
                } else { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(Res.string.hint_toca_terreno), color = Color(0xFF795548)) } }
            }
        }
    }
}

object TiendaTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(1u, stringResource(Res.string.tab_tienda), painterResource(Res.drawable.ic_shop))

    @Composable override fun Content() {}

    @Composable
    fun Content(dinero: Int, listaProductos: List<ProductoItem>, onCompra: (Int, String) -> Unit) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFDF5E6)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.header_tienda), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFFFC107)), shadowElevation = 2.dp) {
                    Text("💰 $dinero", modifier = Modifier.padding(10.dp, 4.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listaProductos.size) { index ->
                    val producto = listaProductos[index]
                    val nombreInterno = producto.nombre["es"] ?: ""
                    CardProducto(producto, onCompra = { onCompra(producto.precio, nombreInterno) })
                }
            }
        }
    }
}

data class ProductoItem(val nombre: Map<String, String>, val precio: Int)

@Composable
fun CardProducto(producto: ProductoItem, onCompra: () -> Unit) {
    val bucket = "proyectohuerto25-26.firebasestorage.app"
    val nombreTraducido = obtenerNombreSegunIdioma(producto.nombre)
    val nombreBaseImagen = producto.nombre["es"] ?: ""
    val (carpeta, prefijo) = when {
        nombreBaseImagen.contains("Semillas") -> "ImgSemillas" to "Bolsa"
        else -> "ImgUtilidades" to ""
    }
    val nombreLimpio = nombreBaseImagen.replace(" ", "").replace("PRO", "Pro").replace("BIO", "Bio")
    val nombreArchivo = "${prefijo}${nombreLimpio}.jpg"
    val imageUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$carpeta%2F$nombreArchivo?alt=media"

    Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEFEBE9)), contentAlignment = Alignment.Center) {
                AsyncImage(model = imageUrl, contentDescription = nombreTraducido, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = nombreTraducido, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723), maxLines = 1)
            Row(modifier = Modifier.background(Color(0xFFFFF8E1), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("💰 ${producto.precio}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFBF360C))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = onCompra, modifier = Modifier.fillMaxWidth().height(36.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(Res.string.btn_comprar))
            }
        }
    }
}

object PerfilTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(2u, stringResource(Res.string.tab_perfil), painterResource(Res.drawable.ic_profile))

    @Composable override fun Content() {}

    @Composable
    fun Content(
        statsReal: List<String>,
        inventario: Map<String, Int>,
        avatarUrl: String?,
        onModificarDinero: (Int) -> Unit,
        onAddInventario: (String) -> Unit,
        onAcelerarCultivos: (Long?) -> Unit,
        onForzarSed: () -> Unit,
        onForzarPlaga: () -> Unit,
        onResetProgreso: () -> Unit,
        onSubirFoto: (PlatformFile) -> Unit,
        onLogout: () -> Unit
    ) {
        val auth = Firebase.auth
        val scope = rememberCoroutineScope()
        val usuarioActual = auth.currentUser
        val developersAutorizados = setOf("L2qDDOMGjrZ4CBjuF1khlZyzGzt1")
        val esMiUsuario = developersAutorizados.contains(usuarioActual?.uid)

        val email = usuarioActual?.email ?: stringResource(Res.string.label_usuario)
        var modoDeveloper by remember { mutableStateOf(false) }
        var minutosAcelerar by remember { mutableStateOf("") }
        var mostrarConfirmReset by remember { mutableStateOf(false) }

        val picker = rememberFilePickerLauncher(type = PickerType.Image) { file ->
            file?.let { onSubirFoto(it) }
        }

        if (mostrarConfirmReset) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmReset = false },
                title = { Text(stringResource(Res.string.dialog_reset_titulo)) },
                text = { Text(stringResource(Res.string.dialog_reset_cuerpo)) },
                confirmButton = { Button(onClick = { onResetProgreso(); mostrarConfirmReset = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("RESET") } },
                dismissButton = { TextButton(onClick = { mostrarConfirmReset = false }) { Text(stringResource(Res.string.btn_cancelar)) } }
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDF5E6))) {
            if (esMiUsuario) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp).size(30.dp).clip(CircleShape).background(if(modoDeveloper) Color(0xFFD4AF37) else Color.White).border(1.dp, Color.LightGray, CircleShape).clickable { modoDeveloper = !modoDeveloper }, contentAlignment = Alignment.Center) { Text("D") }
            }

            Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.header_perfil), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Spacer(modifier = Modifier.height(40.dp))
                Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(100.dp).shadow(4.dp, CircleShape).background(Color.White, CircleShape).border(3.dp, Color(0xFF2E7D32), CircleShape).clip(CircleShape).clickable { picker.launch() }, contentAlignment = Alignment.Center) {
                        if (avatarUrl != null) {
                            AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Text(email.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(28.dp).background(Color(0xFF2E7D32), CircleShape).border(2.dp, Color.White, CircleShape).clickable { picker.launch() }, contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(email, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(32.dp))

                if (modoDeveloper && esMiUsuario) {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFD4AF37))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Panel Developer", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onModificarDinero(1000) }, modifier = Modifier.weight(1f)) { Text("+1K") }
                                Button(onClick = onForzarSed, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) { Text("💧") }
                                Button(onClick = onForzarPlaga, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("🐛") }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = minutosAcelerar, onValueChange = { if (it.all { char -> char.isDigit() }) minutosAcelerar = it }, label = { Text("Mins") }, modifier = Modifier.weight(1f), singleLine = true)
                                Button(onClick = { onAcelerarCultivos(minutosAcelerar.toLongOrNull()); minutosAcelerar = "" }) { Text("⚡") }
                            }
                            Button(onClick = { mostrarConfirmReset = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("RESET TOTAL", color = Color.White) }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFD7CCC8))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(Res.string.stats_titulo), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                        StatRow(stringResource(Res.string.stat_bancales), statsReal[0], Color(0xFF2E7D32))
                        StatRow(stringResource(Res.string.stat_dinero), statsReal[1], Color(0xFFBF360C))
                        StatRow(stringResource(Res.string.stat_cosechas_ok), statsReal[2], Color(0xFF4CAF50))
                        StatRow(stringResource(Res.string.stat_cosechas_ko), statsReal[3], Color(0xFFD32F2F))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFD7CCC8))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(Res.string.mochila_titulo), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                        if (inventario.isEmpty()) { Text(stringResource(Res.string.mochila_vacia), color = Color.Gray) } else {
                            inventario.forEach { (nombre, cantidad) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
                                    Text(nombre); Text("x$cantidad", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = { scope.launch { auth.signOut(); onLogout() } }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(Res.string.btn_logout), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { matarAplicacion() }, modifier = Modifier.fillMaxWidth().height(54.dp), border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(Res.string.btn_close_game), color = Color.Gray)
                }
            }
        }
    }
}

object InfoTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(3u, stringResource(Res.string.tab_info), painterResource(Res.drawable.ic_info))

    @Composable override fun Content() { InfoScreenContent() }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = Color.Gray); Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun InfoScreenContent() {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F8E9)).verticalScroll(scrollState).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(Res.string.info_game_name), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text(stringResource(Res.string.info_subtitle), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))
        InfoCardItem(stringResource(Res.string.info_que_es_titulo), stringResource(Res.string.info_que_es_cuerpo))
        Spacer(Modifier.height(16.dp))
        InfoCardItem(stringResource(Res.string.info_tech_titulo), stringResource(Res.string.info_tech_cuerpo))
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(Res.string.info_detalles_titulo), fontWeight = FontWeight.Bold, color = Color(0xFF388E3C), fontSize = 16.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                DetailRowInfo(stringResource(Res.string.info_version), "1.0.0")
                DetailRowInfo(stringResource(Res.string.info_dev), "R. Liviu Dumitru")
                DetailRowInfo(stringResource(Res.string.info_update), "Enero 2026")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Soporte: ldrotariu01@gmail.com", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(stringResource(Res.string.info_footer), fontSize = 12.sp, color = Color(0xFF81C784), textAlign = TextAlign.Center)
    }
}

@Composable
fun InfoCardItem(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}

@Composable
fun DetailRowInfo(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
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
            title = { Text(stringResource(Res.string.label_inventario)) },
            text = {
                Column {
                    inventario.forEach { (nombre, cant) ->
                        if (cant > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                TextButton(modifier = Modifier.weight(1f), onClick = { if(nombre.contains("Semillas")) onPlantar(cuadritoSel, nombre); mostrarMenuSiembra = false }) { Text("$nombre (x$cant)") }
                                Text(stringResource(Res.string.btn_borrar), color = Color.Red, fontSize = 12.sp, modifier = Modifier.clickable { onEliminarItem(nombre) })
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarMenuSiembra = false }) { Text(stringResource(Res.string.btn_cerrar)) } }
        )
    }

    if (mostrarMenuGestion) {
        val p = plantas[cuadritoSel]
        if (p != null) {
            val ahora = GameData.obtenerTiempoActual()
            val total = GameData.obtenerTiempoCrecimiento(p.nombreSemilla)
            val rest = total - (ahora - p.tiempoPlante)
            val textoTiempo = when {
                p.estaMuerta -> stringResource(Res.string.estado_marchita)
                rest <= 0 -> stringResource(Res.string.estado_cosechar)
                else -> {
                    val h = rest / GameData.hora
                    val m = (rest / 60000) % 60
                    stringResource(Res.string.estado_tiempo_restante, if (h > 0) "${h}h " else "", m)
                }
            }
            AlertDialog(
                onDismissRequest = { mostrarMenuGestion = false },
                title = { Text(p.nombreSemilla, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(textoTiempo, fontSize = 18.sp, color = if (rest <= 0) Color(0xFF2E7D32) else Color.DarkGray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        if (!p.estaMuerta) {
                            if (p.tiempoSed != null) Text(stringResource(Res.string.aviso_agua), color = Color(0xFF2196F3))
                            if (p.tiempoPlaga != null) Text(stringResource(Res.string.aviso_plaga), color = Color(0xFFFF9800))
                        }
                    }
                },
                confirmButton = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val esMadura = (ahora - p.tiempoPlante) >= total
                        if (p.estaMuerta || esMadura) {
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { onAccion(cuadritoSel); mostrarMenuGestion = false }) {
                                Text(if (p.estaMuerta) stringResource(Res.string.btn_limpiar) else stringResource(Res.string.btn_cosechar))
                            }
                        }
                        if (p.tiempoSed != null && !p.estaMuerta) {
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { onRegar(cuadritoSel); mostrarMenuGestion = false }, enabled = (inventario["Regadera PRO"] ?: 0) > 0) { Text(stringResource(Res.string.btn_regar)) }
                        }
                        if (p.tiempoPlaga != null && !p.estaMuerta) {
                            Button(modifier = Modifier.fillMaxWidth(), onClick = { onCurar(cuadritoSel); mostrarMenuGestion = false }, enabled = (inventario["Antiplagas BIO"] ?: 0) > 0) { Text(stringResource(Res.string.btn_curar)) }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAccion(cuadritoSel); mostrarMenuGestion = false }) {
                        Text(stringResource(Res.string.btn_quitar), color = Color.Red)
                    }
                }
            )
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF3E2723).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(4.dp)) {
        val ids = listOf("A", "B", "C", "D")
        for (i in 0..1) { Row(Modifier.weight(1f)) { for (j in 0..1) {
            val id = "$bancalIdx-$idZona-${ids[i * 2 + j]}"
            CuadritoTierra(id, plantas[id], onUpdateSalud, Modifier.weight(1f)) { cuadritoSel = id; if (plantas[id] == null) mostrarMenuSiembra = true else mostrarMenuGestion = true }
        } } }
    }
}