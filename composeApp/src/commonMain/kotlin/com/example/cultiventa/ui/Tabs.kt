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
import org.jetbrains.compose.resources.painterResource
import cultiventa.composeapp.generated.resources.*
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions

@Composable
fun LobbyContainer() {
    TabNavigator(ParcelasTab) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TabNavigationItem(ParcelasTab)
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
        label = { Text(tab.options.title) }
    )
}

object ParcelasTab : Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(0u, "Bancales", painterResource(Res.drawable.ic_home))

    @Composable override fun Content() {
        var bancalSeleccionado by remember { mutableStateOf<Int?>(null) }
        val dinero = 1200
        val hayPlaga = false
        val necesitaRiego = true

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F8E9))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // --- 1. HUD ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mi Huerto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        val (texto, color) = when {
                            hayPlaga -> "⚠️ Plaga" to Color.Red
                            necesitaRiego -> "💧 Sediento" to Color(0xFF1976D2)
                            else -> "🌿 Sano" to Color(0xFF4CAF50)
                        }
                        Text(texto, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
                    }
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFC107))
                    ) {
                        Text("💰 $dinero", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBF360C))
                    }
                }
            }

            Text("Bancales Principales", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))

            // --- 2. REJILLA DE 6 BANCALES (Proporcional) ---
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
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

            // --- 3. GESTIÓN DE BANCAL (Altura Subida a 320.dp) ---
            Text(
                text = if (bancalSeleccionado != null) "Gestión - Bancal #${bancalSeleccionado!! + 1}" else "Selecciona un bancal",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp) // <-- ALTURA SUBIDA para que no parezca aplastado
                    .padding(top = 6.dp)
                    .background(Color(0xFFD7CCC8), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                if (bancalSeleccionado != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        Text("Toca un bancal arriba", color = Color(0xFF795548), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ZonaCultivo(idZona: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF3E2723).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF5D4037).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Text("Zona ${idZona + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))

        Spacer(modifier = Modifier.height(4.dp))

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CuadritoTierra(Modifier.weight(1f))
                CuadritoTierra(Modifier.weight(1f))
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CuadritoTierra(Modifier.weight(1f))
                CuadritoTierra(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CuadritoTierra(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF3E2723).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .clickable { }
    )
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
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f) // <-- Menos aplastado, forma más natural
            .shadow(if (esSeleccionado) 6.dp else 2.dp, RoundedCornerShape(10.dp))
            .background(Color(0xFF5D4037), RoundedCornerShape(10.dp))
            .border(width = 4.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(6.dp).background(Color(0xFF3E2723), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("#${id + 1}", color = Color.White.copy(alpha = 0.1f), fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
    }
}

object TiendaTab : Tab {
    override val options: TabOptions @Composable get() = TabOptions(1u, "Tienda", painterResource(Res.drawable.ic_shop))
    @Composable override fun Content() { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Tienda") } }
}

object PerfilTab : Tab {
    override val options: TabOptions @Composable get() = TabOptions(2u, "Perfil", painterResource(Res.drawable.ic_profile))
    @Composable override fun Content() { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Perfil") } }
}