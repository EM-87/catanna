package com.catanna.core

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.max

/** Textos y agregados que pintan las pantallas. Sin dependencias de Android, para poder probarlos. */
object Vistas {

    private fun coma(d: Double, decimales: Int = 1) = "%.${decimales}f".format(java.util.Locale.ROOT, d).replace('.', ',')

    fun media(p: Producto, campo: (Resena) -> Int): Double =
        if (p.resenas.isEmpty()) 0.0 else p.resenas.sumOf(campo).toDouble() / p.resenas.size

    fun mediaEstrellas(p: Producto) = media(p) { it.estrellas }
    fun mediaPotencia(p: Producto) = media(p) { it.potencia }

    /** «4,6 ★» o «—» si nadie la ha valorado. */
    fun nota(p: Producto) = if (p.resenas.isEmpty()) "—" else coma(mediaEstrellas(p)) + " ★"

    fun nNotas(n: Int) = if (n == 1) "1 valoración" else "$n valoraciones"
    fun nNotas(p: Producto) = nNotas(p.resenas.size)

    fun eur(n: Double): String =
        if (n == Math.floor(n)) "${n.toLong()} €" else coma(n, 2) + " €"

    /** «3,50 €/g» para tamaños de más de una unidad; vacío para 1 g, 1 ml o 1 ud. */
    fun porUnidad(f: Formato): String {
        val q = f.g.replace(',', '.').takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return ""
        if (q == 0.0 || q == 1.0) return ""
        val u = when {
            "ml" in f.g -> "ml"
            "ud" in f.g -> "ud"
            else -> "g"
        }
        return eur(f.precio / q) + "/" + u
    }

    private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    /** «hoy» o «12 ago». */
    fun fecha(millis: Long, ahora: Long = System.currentTimeMillis(), zona: TimeZone = TimeZone.getDefault()): String {
        val c = Calendar.getInstance(zona).apply { timeInMillis = millis }
        val h = Calendar.getInstance(zona).apply { timeInMillis = ahora }
        if (c.get(Calendar.YEAR) == h.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == h.get(Calendar.DAY_OF_YEAR)) return "hoy"
        return "${c.get(Calendar.DAY_OF_MONTH)} ${MESES[c.get(Calendar.MONTH)]}"
    }

    fun sub(p: Producto) = p.molecula + " " + p.tipo.lowercase()

    /** Etiquetas visibles en una línea: «citrico, diesel» o «sin etiquetas». */
    fun notas(p: Producto) = Etiquetas.visibles(p).joinToString(", ") { it.tag }.ifEmpty { "sin etiquetas" }

    fun filtrar(ps: List<Producto>, mol: String?, tipo: String?) =
        ps.filter { (mol == null || it.molecula == mol) && (tipo == null || it.tipo == tipo) }

    /** De mejor a peor nota media. Los que no tienen valoraciones van al final. */
    fun ordenarPorNota(ps: List<Producto>) =
        ps.sortedWith(compareByDescending<Producto> { mediaEstrellas(it) }.thenByDescending { it.resenas.size })

    fun plural(tipo: String) = when (tipo) {
        "Flor" -> "Flores"
        else -> tipo + "s"
    }

    // ── Carta ───────────────────────────────────────────────────────────────

    data class Grupo(val mol: String, val resumen: String, val items: List<Producto>)

    fun carta(ps: List<Producto>): List<Grupo> = Catalogo.MOLECULAS.mapNotNull { m ->
        val items = ps.filter { it.molecula == m }
        if (items.isEmpty()) return@mapNotNull null
        val resumen = Catalogo.TIPOS.mapNotNull { t ->
            val n = items.count { it.tipo == t }
            if (n == 0) null else "$n " + (if (n == 1) t else plural(t)).lowercase()
        }.joinToString(" · ")
        Grupo(m, resumen, items)
    }

    // ── Etiqueta ────────────────────────────────────────────────────────────

    data class Barra(val fraccion: Float, val principal: Boolean)
    data class Otra(val producto: Producto, val menciones: Int)
    data class Etiqueta(
        val tag: String,
        val barras: List<Barra>,
        val resumen: String,
        val frases: List<Resena>,
        val otras: List<Otra>,
        val pie: String,
    )

    /**
     * Pantalla de una etiqueta. Si venimos de una ficha ([desdeId]) las frases son las de ese
     * producto; si no, las del producto con más menciones.
     */
    fun etiqueta(ps: List<Producto>, tag: String, desdeId: String?): Etiqueta {
        val conTag = ps.mapNotNull { p -> Etiquetas.tagsDe(p).find { it.tag == tag }?.let { p to it } }
            .sortedByDescending { it.second.n }
        val maxN = conTag.firstOrNull()?.second?.n ?: 1
        val aqui = conTag.find { it.first.id == desdeId } ?: conTag.firstOrNull()
        val resumen = when {
            aqui == null -> ""
            aqui === conTag.first() ->
                "${aqui.first.nombre} es la más «$tag» del catálogo: ${aqui.second.n} menciones de ${aqui.first.resenas.size}."
            else -> "${aqui.first.nombre}: ${aqui.second.n} menciones de ${aqui.first.resenas.size}. " +
                "La más «$tag» es ${conTag.first().first.nombre}."
        }
        return Etiqueta(
            tag = tag,
            barras = conTag.take(5).mapIndexed { i, (_, c) -> Barra(max(0.14f, c.n.toFloat() / maxN), i == 0) },
            resumen = resumen,
            frases = aqui?.second?.resenas?.take(4).orEmpty(),
            otras = conTag.filter { it !== aqui }.take(4).map { Otra(it.first, it.second.n) },
            pie = "Etiqueta generada automáticamente. Cuenta también: " +
                Etiquetas.SIN[tag].orEmpty().take(5).joinToString(", ") + ".",
        )
    }

    // ── Panel de etiquetas (maestra) ────────────────────────────────────────

    data class FilaEtiqueta(val tag: String, val total: Int, val publicada: Boolean, val donde: String, val sinonimos: String)

    fun panelEtiquetas(ps: List<Producto>): List<FilaEtiqueta> {
        val porTag = linkedMapOf<String, MutableList<Pair<Producto, Etiquetas.Cuenta>>>()
        ps.forEach { p -> Etiquetas.tagsDe(p).forEach { c -> porTag.getOrPut(c.tag) { mutableListOf() } += p to c } }
        return porTag.map { (tag, lista) ->
            FilaEtiqueta(
                tag = tag,
                total = lista.sumOf { it.second.n },
                publicada = lista.any { it.second.n >= Etiquetas.UMBRAL },
                donde = lista.take(3).joinToString(" · ") { "${it.first.nombre} ${it.second.n}" },
                sinonimos = "Cuenta: " + Etiquetas.SIN[tag].orEmpty().joinToString(", "),
            )
        }.sortedByDescending { it.total }
    }

    /** Etiquetas que pasan a publicadas al añadir [nueva] a [p]. Para avisar al que valora. */
    fun estrenadas(p: Producto, nueva: Resena): List<String> {
        val antes = Etiquetas.publicados(p).map { it.tag }.toSet()
        val despues = p.copy(resenas = p.resenas.filter { it.uid != nueva.uid } + nueva)
        return Etiquetas.publicados(despues).map { it.tag }.filter { it !in antes }
    }
}
