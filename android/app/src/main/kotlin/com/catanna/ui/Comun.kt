package com.catanna.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.catanna.R
import androidx.compose.foundation.text.BasicText as Texto

val Pill = RoundedCornerShape(999.dp)
fun redondo(r: Int) = RoundedCornerShape(r.dp)

/** Pulsación del prototipo: encoge un poco y se aclara, sin onda de Material. */
fun Modifier.tapa(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pulsado by src.collectIsPressedAsState()
    val k by animateFloatAsState(if (pulsado) 0.978f else 1f, tween(120), label = "tapa")
    graphicsLayer { scaleX = k; scaleY = k; alpha = if (pulsado) 0.9f else 1f }
        .clickable(src, indication = null, enabled = enabled, onClick = onClick)
}

fun Modifier.lineaAbajo(color: Color = C.linea) = drawBehind {
    val y = size.height - 0.5.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
}

fun Modifier.lineaArriba(color: Color = C.linea) = drawBehind {
    drawLine(color, Offset(0f, 0.5.dp.toPx()), Offset(size.width, 0.5.dp.toPx()), 1.dp.toPx())
}

fun Modifier.punteadoAbajo() = drawBehind {
    val y = size.height - 0.5.dp.toPx()
    drawLine(
        C.puntos, Offset(0f, y), Offset(size.width, y), 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.dp.toPx(), 2.dp.toPx())),
    )
}

fun Modifier.bordeDiscontinuo(radio: Dp) = drawBehind {
    val w = 1.5.dp.toPx()
    drawRoundRect(
        C.discontinua, topLeft = Offset(w / 2, w / 2),
        size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius(radio.toPx()),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))),
    )
}

/** Tarjeta blanca roto con borde fino, la pieza básica de todas las listas. */
fun Modifier.tarjeta(radio: Int = 18) =
    clip(redondo(radio)).background(C.tarjeta).border(1.dp, C.linea, redondo(radio))

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.logo_catanna), "Catanna", modifier.height(17.dp))
}

@Composable
fun Cabecera(izquierda: @Composable () -> Unit, derecha: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxWidth().background(C.crema).lineaAbajo()
            .padding(start = 18.dp, end = 18.dp, top = 13.dp, bottom = 11.dp)
    ) {
        Box(Modifier.align(Alignment.CenterStart)) { izquierda() }
        Logo(Modifier.align(Alignment.Center))
        Box(Modifier.align(Alignment.CenterEnd)) { derecha() }
    }
}

/** El botón redondo de la cabecera (atrás, iniciales). */
@Composable
fun BotonCirculo(texto: String, size: Float = 17f, peso: Int = 400, color: Color = C.tinta, onClick: (() -> Unit)? = null) {
    Box(
        Modifier.size(32.dp).clip(CircleShape).background(C.arena)
            .then(if (onClick != null) Modifier.tapa(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) { Texto(texto, style = t(size, peso, color)) }
}

/** La zona desplazable entre cabecera y barra inferior. */
@Composable
fun Panel(conNav: Boolean, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .then(if (conNav) Modifier else Modifier.navigationBarsPadding())
    ) {
        content()
        Spacer(Modifier.height(22.dp))
    }
}

data class ItemNav(val icono: String, val label: String, val activo: Boolean, val onClick: () -> Unit)

@Composable
fun NavInferior(items: List<ItemNav>) {
    Row(Modifier.fillMaxWidth().background(C.tarjeta).lineaArriba().navigationBarsPadding()) {
        items.forEach { n ->
            val col = if (n.activo) C.tinta else C.grisNav
            Column(
                Modifier.weight(1f).tapa(onClick = n.onClick).padding(top = 11.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Texto(n.icono, style = t(16f, color = col, lineHeight = 1.2f))
                Texto(n.label, Modifier.padding(top = 3.dp), style = t(10.5f, 800, col))
            }
        }
    }
}

@Composable
fun TituloPagina(titulo: String, sub: String) {
    Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp)) {
        Texto(titulo, style = t(26f, 900, spacing = (-0.02).em))
        Texto(sub, Modifier.padding(top = 6.dp), style = t(13f, 600, C.gris, lineHeight = 1.45f))
    }
}

/** Chip de filtro o selección. Inactivo: arena; activo: los colores que se le pasen. */
@Composable
fun Chip(label: String, activo: Boolean, bgActivo: Color, colorActivo: Color, onClick: () -> Unit) {
    Texto(
        label,
        Modifier.tapa(onClick = onClick).clip(Pill).background(if (activo) bgActivo else C.arena)
            .padding(horizontal = 15.dp, vertical = 8.dp),
        style = t(12.5f, if (activo) 800 else 600, if (activo) colorActivo else C.chipTexto),
        softWrap = false,
    )
}

/** Etiqueta de sabor con su color pastel. [n] se pinta atenuado detrás. */
@Composable
fun TagChip(tag: String, n: Int? = null, pequena: Boolean = false, atenuarN: Boolean = true, onClick: (() -> Unit)? = null) {
    val (bg, col) = C.tag(tag)
    val txt = buildAnnotatedString {
        append(tag)
        if (n != null) {
            append(" ")
            withStyle(SpanStyle(color = if (atenuarN) col.copy(alpha = 0.6f) else col)) { append(n.toString()) }
        }
    }
    Texto(
        txt,
        Modifier.then(if (onClick != null) Modifier.tapa(onClick = onClick) else Modifier)
            .clip(Pill).background(bg)
            .padding(horizontal = if (pequena) 12.dp else 14.dp, vertical = if (pequena) 6.dp else 8.dp),
        style = t(if (pequena) 12f else 13f, 700, col),
        softWrap = false,
    )
}

/** Foto de producto recortada. Sin foto, un hueco arena con la inicial (no la de otro producto). */
@Composable
fun Foto(url: String?, nombre: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.background(C.arena), contentAlignment = Alignment.Center) {
        if (url.isNullOrBlank()) {
            Texto(
                nombre.trim().take(1).uppercase(),
                style = t((maxWidth.value * 0.42f).coerceAtMost(90f), 900, C.discontinua),
            )
        } else {
            AsyncImage(url, nombre, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

/** Fila de la lista de Descubrir. También la usa la maestra para la vista previa. */
@Composable
fun FilaProducto(
    foto: String?, nombre: String, sub: String, notas: String, nota: String, nNotas: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.tapa(onClick = onClick) else Modifier)
            .tarjeta().padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Foto(foto, nombre, Modifier.size(58.dp).clip(redondo(14)))
        Column(Modifier.weight(1f)) {
            Texto(nombre, style = t(15f, 800, lineHeight = 1.2f))
            Texto(sub, Modifier.padding(top = 3.dp), style = t(11.5f, 600, C.gris))
            Texto(notas, Modifier.padding(top = 4.dp), style = t(11.5f, 700, C.marron))
        }
        Column(horizontalAlignment = Alignment.End) {
            Texto(nota, style = t(16f, 900))
            Texto(nNotas, Modifier.padding(top = 2.dp), style = t(11f, 700, C.gris))
        }
    }
}

@Composable
fun BotonNegro(texto: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Texto(
        texto,
        modifier.fillMaxWidth().tapa(enabled, onClick).clip(Pill)
            .background(if (enabled) C.tinta else C.bordeInput).padding(15.dp),
        style = t(15f, 800, Color.White).copy(textAlign = TextAlign.Center),
    )
}

@Composable
fun BotonBorde(texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Texto(
        texto,
        modifier.fillMaxWidth().tapa(onClick = onClick).clip(Pill).border(1.5.dp, C.bordeInput, Pill).padding(14.dp),
        style = t(14f, 800, C.chipTexto).copy(textAlign = TextAlign.Center),
    )
}

/** Recuadro de aviso a todo el ancho (verde, arena…). */
@Composable
fun Aviso(
    texto: String, bg: Color, color: Color, modifier: Modifier = Modifier,
    size: Float = 13f, peso: Int = 700, onClick: (() -> Unit)? = null,
) {
    Texto(
        texto,
        modifier.fillMaxWidth().then(if (onClick != null) Modifier.tapa(onClick = onClick) else Modifier)
            .clip(redondo(18)).background(bg).padding(horizontal = 16.dp, vertical = 14.dp),
        style = t(size, peso, color, lineHeight = 1.45f),
    )
}

/** Campo de texto en píldora, como los del alta de producto. */
@Composable
fun CampoPill(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    tipo: KeyboardType = KeyboardType.Text,
    secreto: Boolean = false,
    compacto: Boolean = false,
) {
    BasicTextField(
        value, onChange,
        modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = t(if (compacto) 13.5f else 14f),
        cursorBrush = SolidColor(C.tinta),
        keyboardOptions = KeyboardOptions(keyboardType = tipo),
        visualTransformation = if (secreto) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { inner ->
            Box(
                Modifier.clip(Pill).background(C.tarjeta).border(BorderStroke(1.5.dp, C.bordeInput), Pill)
                    .padding(horizontal = if (compacto) 14.dp else 16.dp, vertical = if (compacto) 9.dp else 11.dp)
            ) {
                if (value.isEmpty()) Texto(placeholder, style = t(if (compacto) 13.5f else 14f, color = C.grisNav))
                inner()
            }
        },
    )
}

@Composable
fun Etiquetado(texto: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) {
        Texto(texto, Modifier.padding(bottom = 7.dp), style = t(11f, 800, C.chipTexto))
        content()
    }
}

@Composable
fun Separador(h: Int) = Spacer(Modifier.height(h.dp))
