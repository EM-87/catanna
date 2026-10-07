package com.catanna.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.catanna.R

/** Paleta 2 del diseño: crema, el negro del logotipo mandando y pasteles en las etiquetas. */
object C {
    val crema = Color(0xFFFDF8F0)
    val tinta = Color(0xFF1B1B1B)
    val tarjeta = Color(0xFFFFFDF9)
    val arena = Color(0xFFF4EAD6)
    val linea = Color(0xFFEEE3D0)
    val borde = Color(0xFFE6DCC9)
    val bordeInput = Color(0xFFD8CCB6)
    val discontinua = Color(0xFFDFD2BA)
    val puntos = Color(0xFFE2D7C0)
    val gris = Color(0xFF8D8578)
    val grisNav = Color(0xFFA89F92)
    val grisOscuro = Color(0xFF5C5C5C)
    val chipTexto = Color(0xFF6B6154)
    val resumenMol = Color(0xFF5F5A52)
    val marron = Color(0xFF7A6A44)
    val verdeClaro = Color(0xFFE3ECDF)
    val verde = Color(0xFF3D5C42)
    val verdeTexto = Color(0xFF2F4A35)
    val rosa = Color(0xFFF4C3D4)
    val rosaClaro = Color(0xFFF9DDE6)
    val granate = Color(0xFF8F3A56)
    val lila = Color(0xFFE4E2F0)
    val lilaTexto = Color(0xFF494379)
    val estrellaOff = Color(0xFFE0D5C0)
    val potenciaLabel = Color(0xFF9D968C)

    /** Fondo y texto de cada etiqueta. */
    val PAL: Map<String, Pair<Color, Color>> = mapOf(
        "citrico" to (verdeClaro to verde), "diesel" to (rosaClaro to granate), "dulce" to (rosaClaro to granate),
        "terroso" to (arena to marron), "pino" to (lila to lilaTexto), "menta" to (verdeClaro to verde),
        "frutal" to (rosaClaro to granate), "cremoso" to (arena to marron), "especiado" to (lila to lilaTexto),
        "floral" to (rosaClaro to granate),
    )

    fun tag(t: String) = PAL[t] ?: (arena to marron)

    /** Fondo de la cabecera de cada molécula en la carta. */
    val MOLBG = mapOf(
        "OH10" to Color(0xFFF9DDE6), "THZ" to Color(0xFFE4E2F0), "CBC" to Color(0xFFE3ECDF),
        "FTS" to Color(0xFFDFE7F2), "D10" to Color(0xFFF6E6CF),
    )
}

@OptIn(ExperimentalTextApi::class)
val Figtree = FontFamily(
    listOf(400, 500, 600, 700, 800, 900).map { w ->
        Font(R.font.figtree, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    }
)

/** Estilo de texto con la tipografía de la app. */
fun t(
    size: Float,
    weight: Int = 400,
    color: Color = C.tinta,
    spacing: TextUnit = TextUnit.Unspecified,
    lineHeight: Float? = null,
) = TextStyle(
    fontFamily = Figtree,
    fontSize = size.sp,
    fontWeight = FontWeight(weight),
    color = color,
    letterSpacing = spacing,
    lineHeight = lineHeight?.em ?: TextUnit.Unspecified,
)

/** Los rótulos en mayúsculas pequeñas: «MOLÉCULA», «LO QUE DICEN LOS CLIENTES». */
val rotulo = t(11f, 800, C.gris, 0.1.em)

@Composable
fun CatannaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = C.tinta, onPrimary = Color.White, background = C.crema, surface = C.crema,
            onBackground = C.tinta, onSurface = C.tinta,
        ),
        typography = MaterialTheme.typography.let { ty ->
            ty.copy(
                bodyLarge = ty.bodyLarge.copy(fontFamily = Figtree),
                bodyMedium = ty.bodyMedium.copy(fontFamily = Figtree),
                labelLarge = ty.labelLarge.copy(fontFamily = Figtree),
            )
        },
        content = content,
    )
}
