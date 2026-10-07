package com.catanna.cliente

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catanna.core.Etiquetas
import com.catanna.core.Producto
import com.catanna.core.Resena
import com.catanna.core.Vistas
import com.catanna.data.Repo
import com.catanna.data.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

sealed interface Vista {
    data object Descubrir : Vista
    data object Carta : Vista
    data object Probados : Vista
    data class Ficha(val id: String) : Vista
    data class Etiqueta(val tag: String, val desdeId: String?) : Vista
    data class Valorar(val id: String) : Vista
}

val RAICES = listOf(Vista.Descubrir, Vista.Carta, Vista.Probados)

class ClienteViewModel : ViewModel() {

    /** null mientras llega la primera carga. */
    val productos: StateFlow<List<Producto>?> =
        Repo.catalogo(soloPublicados = true).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    lateinit var usuario: Usuario

    // ── Navegación: una pila sencilla, como en el prototipo ─────────────────
    var vista by mutableStateOf<Vista>(Vista.Descubrir)
        private set
    private val pila = mutableStateListOf<Vista>()
    val hayPila get() = pila.isNotEmpty()

    fun ir(v: Vista) {
        if (v is Vista.Valorar) prepararValorar(v.id)
        eco = null
        pila.add(vista)
        vista = v
    }

    fun atras() {
        eco = null
        vista = pila.removeLastOrNull() ?: Vista.Descubrir
    }

    fun raiz(v: Vista) {
        eco = null
        pila.clear()
        vista = v
    }

    // ── Filtros de Descubrir ─────────────────────────────────────────────────
    var mol by mutableStateOf<String?>(null)
    var tipo by mutableStateOf<String?>(null)

    /** Aviso tras publicar: «Se ha publicado la etiqueta «citrico» en Lemon.» */
    var eco by mutableStateOf<String?>(null)
        private set

    // ── Borrador de valoración ───────────────────────────────────────────────
    var vEstrellas by mutableIntStateOf(0)
    var vPotencia by mutableIntStateOf(0)
    val vChips = mutableStateListOf<String>()
    var vTexto by mutableStateOf("")
    /** La valoración que ya tenía el usuario, si está editándola. */
    var vAnterior by mutableStateOf<Resena?>(null)
        private set

    fun miResena(p: Producto) = p.resenas.find { it.uid == usuario.uid }

    private fun prepararValorar(id: String) {
        val previa = productos.value?.find { it.id == id }?.let(::miResena)
        vAnterior = previa
        vEstrellas = previa?.estrellas ?: 0
        vPotencia = previa?.potencia ?: 0
        vTexto = previa?.texto.orEmpty()
        vChips.clear()
    }

    fun alternarChip(tag: String) {
        if (tag in vChips) vChips.remove(tag) else vChips.add(tag)
    }

    val puedePublicar get() = vEstrellas > 0 && vPotencia > 0

    /**
     * El texto que se guarda. Los chips marcados que el texto no menciona ya se añaden en una
     * frase aparte («Me ha recordado a dulce, pino.») para que el motor los cuente; empieza con
     * cuatro palabras neutras para que ninguna negación anterior los anule.
     */
    fun textoFinal(): String {
        val texto = vTexto.trim()
        val dichas = Etiquetas.extrae(texto).let { it.si + it.no }
        val extra = vChips.filter { it !in dichas }
        val frase = if (extra.isEmpty()) "" else "Me ha recordado a " + extra.joinToString(", ") + "."
        return listOf(texto, frase).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { "Sin comentario." }
    }

    fun publicar(id: String) {
        val p = productos.value?.find { it.id == id } ?: return
        if (!puedePublicar) return
        val nueva = Resena(usuario.uid, usuario.alias, textoFinal(), vEstrellas, vPotencia, System.currentTimeMillis())
        val estrenadas = Vistas.estrenadas(p, nueva)
        Repo.guardarResena(p.id, nueva.uid, nueva.alias, nueva.texto, nueva.estrellas, nueva.potencia)
        pila.clear()
        vista = Vista.Ficha(p.id)
        eco = if (estrenadas.isEmpty()) "Valoración publicada. Gracias." else
            "Con la tuya se ha publicado la etiqueta «" + estrenadas.joinToString("», «") + "» en " + p.nombre + "."
        vEstrellas = 0; vPotencia = 0; vTexto = ""; vChips.clear(); vAnterior = null
    }
}
