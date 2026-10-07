package com.catanna.maestra

import com.catanna.core.Formato

/**
 * La carta de ejemplo del prototipo, para no empezar con el catálogo vacío. Solo productos:
 * las valoraciones de prueba del prototipo no se cargan, para no inventar opiniones.
 * Los que no tienen foto recortada de los carteles se quedan sin foto.
 */
data class ProductoSemilla(
    val nombre: String,
    val molecula: String,
    val tipo: String,
    val detalle: String,
    val foto: String?,
    val formatos: List<Formato>,
    val declarada: String,
)

private fun f(vararg pares: Pair<String, Int>) = pares.map { (g, p) -> Formato(g, p.toDouble()) }

val SEMILLA = listOf(
    ProductoSemilla("Diésel Dry", "THZ", "Resina", "Dry triple filtrado", "p-diesel-dry.png", f("1 g" to 7, "2 g" to 14, "5 g" to 30), "alta"),
    ProductoSemilla("Royal Cream", "THZ", "Resina", "Resina hash", "p-royal-cream.png", f("1 g" to 8, "2 g" to 16, "5 g" to 35), "media"),
    ProductoSemilla("Static", "FTS", "Resina", "Resina hash", "p-static.png", f("1 g" to 8, "2 g" to 16, "5 g" to 35), "alta"),
    ProductoSemilla("Dorado", "FTS", "Resina", "Resina hash", "p-dorado.png", f("1 g" to 7, "2 g" to 14), "media"),
    ProductoSemilla("Iceolator", "CBC", "Resina", "Extracción, nueva fórmula", "p-iceolator.png", f("1 g" to 10, "2 g" to 20, "5 g" to 45, "10 g" to 80), "alta"),
    ProductoSemilla("Zkittlez Dry", "OH10", "Resina", "Dry sift 90 micras", null, f("1 g" to 8, "2 g" to 16, "5 g" to 35), "media"),
    ProductoSemilla("Amnesia", "OH10", "Flor", "Indoor", null, f("2 g" to 10, "5 g" to 30, "10 g" to 50), "media"),
    ProductoSemilla("Lemon", "CBC", "Flor", "Indoor", null, f("2 g" to 10, "5 g" to 30), "baja"),
    ProductoSemilla("Kush Mint", "THZ", "Resina", "Resina hash", null, f("1 g" to 8, "2 g" to 16), "media"),
    ProductoSemilla("Banana Z", "THZ", "Resina", "Resina hash", null, f("1 g" to 8, "2 g" to 16), "media"),
    ProductoSemilla("Nepal Dry", "FTS", "Resina", "Dry hash", null, f("1 g" to 8, "2 g" to 16, "5 g" to 35), "alta"),
    ProductoSemilla("Piatella", "CBC", "Resina", "Resina hash", null, f("1 g" to 8, "2 g" to 16), "media"),
    ProductoSemilla("Vaper Rollz OH-10 97%", "OH10", "Vaper", "1 ml", null,
        listOf(Formato("0,5 ml", 20.0), Formato("1 ml", 30.0), Formato("2 ml", 35.0)), "alta"),
    ProductoSemilla("Vaper Grape Soda", "FTS", "Vaper", "1 ml, Rollz", null, f("1 ml" to 30), "media"),
    ProductoSemilla("Pre-roll FTS King Size", "FTS", "Pre-roll", "Unidad", null, f("1 ud" to 10), "media"),
    ProductoSemilla("Pre-roll OH-10", "OH10", "Pre-roll", "Unidad", null, f("1 ud" to 10), "alta"),
)
