package com.catanna.core

import java.text.Normalizer

/**
 * Motor de etiquetas. Lee el texto libre de cada valoración, busca palabras de cada
 * familia (sinónimos) y descarta las que van negadas («nada de menta»). Una etiqueta se
 * publica sola cuando la mencionan [UMBRAL] valoraciones distintas del mismo producto.
 */
object Etiquetas {
    const val UMBRAL = 4

    /** Familias de palabras. La primera es el nombre de la etiqueta. Se comparan como prefijo. */
    val SIN: Map<String, List<String>> = linkedMapOf(
        "citrico" to listOf("citric", "limon", "lima", "naranja", "pomelo", "acido", "mandarina"),
        "diesel" to listOf("diesel", "gasolina", "gasoil", "combustible", "queroseno"),
        "dulce" to listOf("dulce", "azucar", "caramelo", "miel", "golosina"),
        "terroso" to listOf("terroso", "tierra", "humedo", "bosque", "madera"),
        "pino" to listOf("pino", "resinoso", "conifera", "eucalipto"),
        "menta" to listOf("menta", "mentol", "fresco", "frescor", "hierbabuena"),
        "frutal" to listOf("frutal", "fruta", "mango", "platano", "banana", "uva", "fresa", "baya", "melocoton"),
        "cremoso" to listOf("cremoso", "crema", "nata", "vainilla", "lacteo", "mantequilla"),
        "especiado" to listOf("especiado", "pimienta", "canela", "picante", "clavo"),
        "floral" to listOf("floral", "flor", "lavanda", "rosa", "jazmin"),
    )

    /** Palabras que niegan si aparecen en las cuatro anteriores a la mención. */
    val NEG = setOf("no", "nada", "sin", "poco", "apenas", "ningun", "ninguna", "tampoco", "ni")

    private const val VENTANA_NEGACION = 4

    fun norm(s: String): String =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

    data class Deteccion(val si: List<String>, val no: List<String>)

    fun extrae(texto: String): Deteccion {
        val pal = norm(texto).split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
        val si = mutableListOf<String>()
        val no = mutableListOf<String>()
        for ((tag, sinonimos) in SIN) {
            var hit = -1
            for (w in sinonimos) {
                val i = pal.indexOfFirst { it.startsWith(w) }
                if (i >= 0 && (hit < 0 || i < hit)) hit = i
            }
            if (hit < 0) continue
            val ventana = pal.subList(maxOf(0, hit - VENTANA_NEGACION), hit)
            if (ventana.any { it in NEG }) no += tag else si += tag
        }
        return Deteccion(si, no)
    }

    data class Cuenta(val tag: String, val resenas: List<Resena>) {
        val n get() = resenas.size
    }

    /** Todas las etiquetas mencionadas en un producto, de más a menos menciones. */
    fun tagsDe(p: Producto): List<Cuenta> {
        val cuenta = linkedMapOf<String, MutableList<Resena>>()
        for (r in p.resenas) for (t in extrae(r.texto).si) cuenta.getOrPut(t) { mutableListOf() } += r
        return cuenta.map { (t, rs) -> Cuenta(t, rs) }.sortedByDescending { it.n }
    }

    fun publicados(p: Producto, umbral: Int = UMBRAL) = tagsDe(p).filter { it.n >= umbral }

    /** Lo que se enseña: las publicadas o, si no hay, las tres provisionales más mencionadas. */
    fun visibles(p: Producto, umbral: Int = UMBRAL): List<Cuenta> =
        publicados(p, umbral).ifEmpty { tagsDe(p).take(3) }
}
