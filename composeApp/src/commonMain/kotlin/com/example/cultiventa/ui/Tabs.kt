package com.example.cultiventa.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import cultiventa.composeapp.generated.resources.*
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun LobbyContainer() {
    TabNavigator(BocetosTab) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    TabNavigationItem(BocetosTab)
                    TabNavigationItem(TiendaTab)
                    TabNavigationItem(PerfilTab)
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                CurrentTab()
            }
        }
    }
}

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

// --- PESTAÑA BANCALES ---
object BocetosTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(0u, "Bancales", painterResource(Res.drawable.ic_home))

    @Composable override fun Content() {
        var bancalSeleccionado by remember { mutableStateOf<Int?>(null) }
        val dinero = 1200
        val necesitaRiego = true

        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF1F8E9)).padding(12.dp, 8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mi Huerto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("🌿 Estado: Activo", fontSize = 12.sp, color = Color.Gray)
                    }
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFC107))
                    ) {
                        Text("💰 $dinero", modifier = Modifier.padding(10.dp, 4.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                    }
                }
            }

            Text("Mis Bancales", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))

            Box(modifier = Modifier.weight(0.4f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(6) { index ->
                        BancalVisual(
                            id = index,
                            esSeleccionado = bancalSeleccionado == index,
                            tieneSed = necesitaRiego && index == 0,
                            onClick = { bancalSeleccionado = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth().weight(0.6f)
                    .background(Color(0xFFD7CCC8), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp)).padding(10.dp)
            ) {
                if (bancalSeleccionado != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Gestión Bancal #${bancalSeleccionado!! + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZonaCultivo(0, Modifier.weight(1f))
                            ZonaCultivo(1, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZonaCultivo(2, Modifier.weight(1f))
                            ZonaCultivo(3, Modifier.weight(1f))
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Toca un bancal para inspeccionar", color = Color(0xFF795548))
                    }
                }
            }
        }
    }
}

// --- PESTAÑA TIENDA ---
object TiendaTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(1u, "Tienda", painterResource(Res.drawable.ic_shop))

    @Composable override fun Content() {
        val dineroUsuario = 1200
        val listaProductos = listOf(
            ProductoItem("Semillas Tomate", 50),
            ProductoItem("Semillas Lechuga", 30),
            ProductoItem("Semillas Berenjena", 120),
            ProductoItem("Semillas Zanahoria", 450),
            ProductoItem("Semillas Pimiento", 90),
            ProductoItem("Semillas Calabacin", 40),
            ProductoItem("Regadera PRO", 200),
            ProductoItem("Antiplagas BIO", 180)
        )

        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFFDF5E6)).padding(16.dp)
        ) {
            // Header con Título y Caja de Dinero
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mercado Ambulante",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFC107)),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        "💰 $dineroUsuario",
                        modifier = Modifier.padding(10.dp, 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFBF360C)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listaProductos.size) { index -> CardProducto(listaProductos[index]) }
            }
        }
    }
}

data class ProductoItem(val nombre: String, val precio: Int)

@Composable
fun CardProducto(producto: ProductoItem) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFEFEBE9), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text("IMG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D6E63))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = producto.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723), maxLines = 1)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.background(Color(0xFFFFF8E1), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("💰 ${producto.precio}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFBF360C))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(36.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(8.dp)) {
                Text("Comprar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PESTAÑA PERFIL ---
object PerfilTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(2u, "Perfil", painterResource(Res.drawable.ic_profile))

    @Composable override fun Content() {
        val auth = Firebase.auth
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val email = auth.currentUser?.email ?: "Usuario"

        val bancalesPlantados = 2
        val dineroGanado = 4500
        val recoltasHechas = 24
        val recoltasPerdidas = 3

        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFFDF5E6)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Diario de Campo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(120.dp).shadow(4.dp, RoundedCornerShape(60.dp))
                    .background(Color.White, RoundedCornerShape(60.dp))
                    .border(3.dp, Color(0xFF2E7D32), RoundedCornerShape(60.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(email.take(1).uppercase(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }

            Spacer(Modifier.height(16.dp))
            Text(email, fontSize = 18.sp, color = Color(0xFF3E2723), fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFD7CCC8))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Estadísticas del Huerto", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037), fontSize = 16.sp)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEFEBE9))

                    StatRow("Bancales en uso", "$bancalesPlantados / 6", Color(0xFF2E7D32))
                    StatRow("Dinero ganado", "💰 $dineroGanado", Color(0xFFBF360C))
                    StatRow("Cosechas logradas", "📦 $recoltasHechas", Color(0xFF4CAF50))
                    StatRow("Cosechas perdidas", "🥀 $recoltasPerdidas", Color(0xFFD32F2F))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        auth.signOut()
                        navigator.replaceAll(LoginScreen(onGoogleSignIn = {}))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// --- COMPONENTES VISUALES BANCALES ---

@Composable
fun ZonaCultivo(idZona: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF3E2723).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF5D4037).copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(6.dp)
    ) {
        Text("Zona ${idZona + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
        Spacer(Modifier.height(4.dp))
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CuadritoTierra(Modifier.weight(1f))
                CuadritoTierra(Modifier.weight(1f))
            }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CuadritoTierra(Modifier.weight(1f))
                CuadritoTierra(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CuadritoTierra(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF3E2723).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).clickable { })
}

@Composable
fun BancalVisual(id: Int, esSeleccionado: Boolean, tieneSed: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val borderColor by infiniteTransition.animateColor(
        initialValue = if (esSeleccionado) Color(0xFF4CAF50) else Color(0xFF8D6E63),
        targetValue = if (tieneSed) Color(0xFF2196F3) else (if (esSeleccionado) Color(0xFF8BC34A) else Color(0xFF8D6E63)),
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).shadow(if (esSeleccionado) 6.dp else 2.dp, RoundedCornerShape(10.dp))
            .background(Color(0xFF5D4037), RoundedCornerShape(10.dp)).border(4.dp, borderColor, RoundedCornerShape(10.dp)).clickable { onClick() }
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp).background(Color(0xFF3E2723), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
            Text("#${id + 1}", color = Color.White.copy(alpha = 0.1f), fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
    }
}