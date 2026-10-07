package com.catanna.core

/** Un tamaño a la venta en tienda: «2 g», «1 ml», «1 ud». El precio es orientativo. */
data class Formato(val g: String, val precio: Double)

/** Una valoración. Hay como mucho una por usuario y producto (el id del documento es el uid). */
data class Resena(
    val uid: String,
    val alias: String,
    val texto: String,
    val estrellas: Int,
    val potencia: Int,
    val fechaMillis: Long,
)

data class Producto(
    val id: String,
    val nombre: String,
    val distribuidor: String = "",
    val molecula: String,
    val tipo: String,
    val detalle: String = "",
    val fotoUrl: String? = null,
    val formatos: List<Formato> = emptyList(),
    /** Potencia que pone la bolsa: «baja», «media», «alta». Puede no constar. */
    val declarada: String? = null,
    val publicado: Boolean = true,
    val resenas: List<Resena> = emptyList(),
)

object Catalogo {
    val MOLECULAS = listOf("OH10", "THZ", "CBC", "FTS", "D10")
    val TIPOS = listOf("Flor", "Resina", "Vaper", "Pre-roll")

    /** Tamaños que propone el alta según el formato elegido. */
    fun tamanosPorDefecto(tipo: String): List<String> = when (tipo) {
        "Flor" -> listOf("2 g", "5 g", "10 g")
        "Vaper" -> listOf("0,5 ml", "1 ml", "2 ml")
        "Pre-roll" -> listOf("1 ud")
        else -> listOf("1 g", "2 g", "5 g")
    }
}

/** «María Ruiz Gil» → «M. R.»: solo iniciales, como se decidió. Máximo dos letras. */
fun iniciales(nombre: String): String =
    nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString(" ") { it.first().uppercaseChar() + "." }
        .ifEmpty { "?" }
