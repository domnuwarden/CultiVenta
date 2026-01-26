package com.example.cultiventa.model

import kotlinx.serialization.Serializable
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.auth.auth

@Serializable
data class UsuarioDatos(
    val monedas: Int = 1200,
    val dinero_ganado: Int = 0,
    val cosechas_logradas: Int = 0,
    val cosechas_perdidas: Int = 0,
    val avatarUrl: String? = null,
    val inventario: Map<String, Int> = emptyMap(),
    val bancales_desbloqueados: List<Int> = listOf(0, 1),
    val plantas_activas: Map<String, PlantaInstancia> = emptyMap()
)

suspend fun cargarDatosUsuario(): UsuarioDatos {
    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid ?: return UsuarioDatos()
    val docRef = db.collection("usuarios").document(userId)

    return try {
        val snapshot = docRef.get()
        if (snapshot.exists) {
            snapshot.data<UsuarioDatos>()
        } else {
            val iniciales = UsuarioDatos()
            docRef.set(iniciales)
            iniciales
        }
    } catch (e: Exception) {
        UsuarioDatos()
    }
}

suspend fun actualizarDatosUsuario(nuevosDatos: UsuarioDatos) {
    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid ?: return
    try {
        // .set() creará el documento si no existe
        db.collection("usuarios").document(userId).set(nuevosDatos)
        println("DEBUG: Guardado con éxito en Firestore")
    } catch (e: Exception) {
        println("ERROR FIRESTORE: ${e.message}") // Esto te dirá si faltan permisos
    }
}

suspend fun eliminarItemInventario(nombreItem: String) {
    val db = Firebase.firestore
    val userId = Firebase.auth.currentUser?.uid ?: return
    val docRef = db.collection("usuarios").document(userId)

    try {
        val snapshot = docRef.get()
        if (snapshot.exists) {
            val datos = snapshot.data<UsuarioDatos>()
            val nuevoInventario = datos.inventario.toMutableMap()

            // Si hay más de 1, restamos. Si hay 1 o menos, eliminamos la clave.
            val cantidadActual = nuevoInventario[nombreItem] ?: 0
            if (cantidadActual > 1) {
                nuevoInventario[nombreItem] = cantidadActual - 1
            } else {
                nuevoInventario.remove(nombreItem)
            }

            docRef.set(datos.copy(inventario = nuevoInventario))
        }
    } catch (e: Exception) {
        println("Error al eliminar item: ${e.message}")
    }
}