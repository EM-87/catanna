package com.catanna.data

import android.util.Log
import com.catanna.core.Formato
import com.catanna.core.Producto
import com.catanna.core.Resena
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

/**
 * Firestore:
 *   productos/{id}                  nombre, distribuidor, molecula, tipo, detalle, fotoUrl,
 *                                   formatos[{g, precio}], declarada, publicado, creado
 *   productos/{id}/resenas/{uid}    uid, alias, texto, estrellas, potencia, fecha
 *   admins/{uid}                    existe = puede usar la maestra
 * Storage:
 *   productos/{id}/{marca}.jpg
 *
 * Las etiquetas no se guardan: cada app las calcula al vuelo con el motor de `core`
 * a partir de las reseñas, así un cambio de sinónimos se aplica a todo lo ya escrito.
 */
object Repo {
    private const val TAG = "Repo"
    private val db get() = FirebaseFirestore.getInstance()
    private val storage get() = FirebaseStorage.getInstance()

    private fun Query.flujo(): Flow<QuerySnapshot> = callbackFlow {
        val reg = addSnapshotListener { s, e ->
            if (e != null) Log.w(TAG, "Escucha fallida", e) else if (s != null) trySend(s)
        }
        awaitClose { reg.remove() }
    }

    /** Catálogo con sus reseñas, en vivo. La app de cliente solo ve lo publicado. */
    fun catalogo(soloPublicados: Boolean): Flow<List<Producto>> {
        val base: Query = db.collection("productos").let { if (soloPublicados) it.whereEqualTo("publicado", true) else it }
        return combine(base.flujo(), db.collectionGroup("resenas").flujo()) { ps, rs ->
            val porProducto = rs.documents.groupBy { it.reference.parent.parent?.id }
            ps.documents
                .map { d -> d.aProducto(porProducto[d.id].orEmpty().map { it.aResena() }.sortedByDescending { it.fechaMillis }) }
                .sortedBy { it.nombre.lowercase() }
        }
    }

    private fun DocumentSnapshot.aProducto(resenas: List<Resena>) = Producto(
        id = id,
        nombre = getString("nombre").orEmpty(),
        distribuidor = getString("distribuidor").orEmpty(),
        molecula = getString("molecula").orEmpty(),
        tipo = getString("tipo").orEmpty(),
        detalle = getString("detalle").orEmpty(),
        fotoUrl = getString("fotoUrl"),
        formatos = (get("formatos") as? List<*>).orEmpty().mapNotNull { f ->
            val m = f as? Map<*, *> ?: return@mapNotNull null
            Formato(m["g"] as? String ?: return@mapNotNull null, (m["precio"] as? Number)?.toDouble() ?: 0.0)
        },
        declarada = getString("declarada"),
        publicado = getBoolean("publicado") ?: false,
        resenas = resenas,
    )

    private fun DocumentSnapshot.aResena() = Resena(
        uid = id,
        alias = getString("alias").orEmpty(),
        texto = getString("texto").orEmpty(),
        estrellas = getLong("estrellas")?.toInt() ?: 0,
        potencia = getLong("potencia")?.toInt() ?: 0,
        // Recién escrita y sin confirmar por el servidor: usa la hora estimada.
        fechaMillis = getTimestamp("fecha", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
            ?: System.currentTimeMillis(),
    )

    /**
     * Crea o sustituye la valoración del usuario. No espera al servidor: Firestore la pinta
     * al momento y la sube cuando haya red.
     */
    fun guardarResena(productoId: String, uid: String, alias: String, texto: String, estrellas: Int, potencia: Int) {
        db.collection("productos").document(productoId).collection("resenas").document(uid)
            .set(
                mapOf(
                    "uid" to uid, "alias" to alias, "texto" to texto,
                    "estrellas" to estrellas, "potencia" to potencia,
                    "fecha" to FieldValue.serverTimestamp(),
                )
            )
            .addOnFailureListener { Log.w(TAG, "No se pudo guardar la valoración", it) }
    }

    fun nuevoId(): String = db.collection("productos").document().id

    /** Alta o edición de producto. Si hay foto nueva, la sube antes. */
    suspend fun guardarProducto(
        id: String,
        nombre: String,
        distribuidor: String,
        molecula: String,
        tipo: String,
        detalle: String,
        formatos: List<Formato>,
        declarada: String?,
        publicado: Boolean,
        foto: ByteArray?,
        esNuevo: Boolean,
    ) {
        val datos = mutableMapOf<String, Any?>(
            "nombre" to nombre, "distribuidor" to distribuidor, "molecula" to molecula, "tipo" to tipo,
            "detalle" to detalle, "formatos" to formatos.map { mapOf("g" to it.g, "precio" to it.precio) },
            "declarada" to declarada, "publicado" to publicado,
        )
        if (foto != null) datos["fotoUrl"] = subirFoto(id, foto)
        if (esNuevo) datos["creado"] = FieldValue.serverTimestamp()
        val doc = db.collection("productos").document(id)
        if (esNuevo) doc.set(datos).await() else doc.update(datos).await()
    }

    suspend fun publicar(id: String, publicado: Boolean) {
        db.collection("productos").document(id).update("publicado", publicado).await()
    }

    private suspend fun subirFoto(id: String, bytes: ByteArray): String {
        val ref = storage.reference.child("productos/$id/${System.currentTimeMillis()}.jpg")
        ref.putBytes(bytes, StorageMetadata.Builder().setContentType("image/jpeg").build()).await()
        return ref.downloadUrl.await().toString()
    }

    /** ¿Esta cuenta puede usar la maestra? Se marca a mano creando admins/{uid} en la consola. */
    fun esAdmin(uid: String): Flow<Boolean> = callbackFlow {
        val reg = db.collection("admins").document(uid).addSnapshotListener { s, e ->
            if (e != null) trySend(false) else trySend(s?.exists() == true)
        }
        awaitClose { reg.remove() }
    }
}
