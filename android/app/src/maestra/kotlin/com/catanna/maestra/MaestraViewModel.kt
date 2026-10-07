package com.catanna.maestra

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catanna.core.Catalogo
import com.catanna.core.Formato
import com.catanna.core.Producto
import com.catanna.data.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Pestana { ALTA, CATALOGO, ETIQUETAS }

/** Una fila de «Tamaños y precio». Las propias del formato no dejan cambiar el tamaño. */
data class FilaPrecio(val g: String, val precio: String, val libre: Boolean = false)

class MaestraViewModel(app: Application) : AndroidViewModel(app) {

    /** Todo el catálogo, borradores incluidos. */
    val productos: StateFlow<List<Producto>?> =
        Repo.catalogo(soloPublicados = false).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var pestana by mutableStateOf(Pestana.ALTA)

    // ── Formulario de alta / edición ─────────────────────────────────────────
    /** null = producto nuevo. */
    var editando by mutableStateOf<Producto?>(null)
        private set
    var nombre by mutableStateOf("")
    var distribuidor by mutableStateOf("")
    var detalle by mutableStateOf("")
    var molecula by mutableStateOf("THZ")
        private set
    var tipo by mutableStateOf("Resina")
        private set
    var declarada by mutableStateOf<String?>(null)
    val filas = mutableStateListOf<FilaPrecio>().apply { addAll(filasPara("Resina", emptyList())) }
    /** Foto elegida y aún sin subir. */
    var foto by mutableStateOf<Uri?>(null)

    var ocupado by mutableStateOf(false)
        private set
    var eco by mutableStateOf<String?>(null)
        private set
    var ecoError by mutableStateOf(false)
        private set

    fun elegirMolecula(m: String) { molecula = m }

    fun elegirTipo(t: String) {
        val previas = filas.toList()
        tipo = t
        filas.clear()
        filas.addAll(filasPara(t, previas))
    }

    /** Los tamaños típicos del formato, conservando los precios ya escritos. */
    private fun filasPara(t: String, previas: List<FilaPrecio>): List<FilaPrecio> =
        Catalogo.tamanosPorDefecto(t).map { g -> FilaPrecio(g, previas.find { it.g == g }?.precio.orEmpty()) } +
            previas.filter { it.libre }

    fun otroTamano() = filas.add(FilaPrecio("", "", libre = true))

    fun cambiarFila(i: Int, fila: FilaPrecio) { filas[i] = fila }

    private fun precio(s: String) = s.trim().replace("€", "").replace(',', '.').trim().toDoubleOrNull()

    /** Las filas con precio válido, que son las que se guardan. */
    fun formatos(): List<Formato> = filas.mapNotNull { f ->
        val p = precio(f.precio) ?: return@mapNotNull null
        if (f.g.isBlank()) null else Formato(f.g.trim(), p)
    }

    fun editar(p: Producto) {
        editando = p
        nombre = p.nombre; distribuidor = p.distribuidor; detalle = p.detalle
        molecula = p.molecula; tipo = p.tipo; declarada = p.declarada; foto = null
        val propias = Catalogo.tamanosPorDefecto(p.tipo)
        filas.clear()
        filas.addAll(propias.map { g -> FilaPrecio(g, p.formatos.find { it.g == g }?.let { precioTexto(it.precio) }.orEmpty()) })
        filas.addAll(p.formatos.filter { it.g !in propias }.map { FilaPrecio(it.g, precioTexto(it.precio), libre = true) })
        eco = null
        pestana = Pestana.ALTA
    }

    private fun precioTexto(d: Double) = "%.2f".format(java.util.Locale.ROOT, d).replace('.', ',')

    fun limpiar() {
        editando = null
        nombre = ""; distribuidor = ""; detalle = ""; declarada = null; foto = null
        filas.clear()
        filas.addAll(filasPara(tipo, emptyList()))
    }

    /**
     * Guarda el formulario. [publicado] null = mantener el estado que tenía (al editar).
     */
    fun guardar(publicado: Boolean?) {
        val n = nombre.trim()
        if (n.isEmpty()) { aviso("Ponle un nombre antes de guardar.", error = true); return }
        val e = editando
        val pub = publicado ?: e?.publicado ?: true
        val fotoUri = foto
        ocupado = true
        viewModelScope.launch {
            try {
                val bytes = fotoUri?.let { withContext(Dispatchers.IO) { Fotos.desdeUri(getApplication(), it) } }
                Repo.guardarProducto(
                    id = e?.id ?: Repo.nuevoId(), nombre = n, distribuidor = distribuidor.trim(),
                    molecula = molecula, tipo = tipo, detalle = detalle.trim(), formatos = formatos(),
                    declarada = declarada, publicado = pub, foto = bytes, esNuevo = e == null,
                )
                limpiar()
                aviso(
                    when {
                        e != null -> "Cambios guardados en $n."
                        pub -> "$n ya está en el catálogo de la app, sin etiquetas todavía."
                        else -> "$n guardado como borrador. Nadie lo ve hasta que lo publiques."
                    }
                )
            } catch (x: Exception) {
                Log.w("Maestra", "No se pudo guardar", x)
                aviso("No se ha podido guardar. Revisa la conexión y vuelve a intentarlo.", error = true)
            } finally {
                ocupado = false
            }
        }
    }

    fun cambiarPublicado(p: Producto, publicado: Boolean) {
        viewModelScope.launch {
            try {
                Repo.publicar(p.id, publicado)
                if (editando?.id == p.id) editando = p.copy(publicado = publicado)
                aviso(if (publicado) "${p.nombre} vuelve a estar en la app." else "${p.nombre} ya no se ve en la app. Sus valoraciones se conservan.")
            } catch (x: Exception) {
                aviso("No se ha podido cambiar. Revisa la conexión.", error = true)
            }
        }
    }

    private fun aviso(texto: String, error: Boolean = false) {
        eco = texto
        ecoError = error
    }

    // ── Carta de ejemplo ─────────────────────────────────────────────────────
    var sembrando by mutableStateOf<String?>(null)
        private set

    fun sembrar() {
        if (sembrando != null) return
        viewModelScope.launch {
            try {
                SEMILLA.forEachIndexed { i, s ->
                    sembrando = "Cargando ${i + 1} de ${SEMILLA.size}: ${s.nombre}"
                    val bytes = s.foto?.let { nombre ->
                        withContext(Dispatchers.IO) {
                            Fotos.preparar(getApplication<Application>().assets.open("seed/$nombre").use { it.readBytes() })
                        }
                    }
                    Repo.guardarProducto(
                        id = Repo.nuevoId(), nombre = s.nombre, distribuidor = "", molecula = s.molecula, tipo = s.tipo,
                        detalle = s.detalle, formatos = s.formatos, declarada = s.declarada, publicado = true,
                        foto = bytes, esNuevo = true,
                    )
                }
                aviso("Carta de ejemplo cargada: ${SEMILLA.size} referencias.")
            } catch (x: Exception) {
                Log.w("Maestra", "No se pudo sembrar", x)
                aviso("Se cortó la carga. Lo ya subido se queda; revisa la conexión.", error = true)
            } finally {
                sembrando = null
            }
        }
    }
}
