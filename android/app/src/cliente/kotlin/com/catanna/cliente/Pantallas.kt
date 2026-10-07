@file:OptIn(ExperimentalLayoutApi::class)

package com.catanna.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.catanna.core.Catalogo
import com.catanna.core.Etiquetas
import com.catanna.core.Producto
import com.catanna.core.Vistas
import com.catanna.ui.Aviso
import com.catanna.ui.BotonNegro
import com.catanna.ui.C
import com.catanna.ui.Chip
import com.catanna.ui.FilaProducto
import com.catanna.ui.Foto
import com.catanna.ui.Panel
import com.catanna.ui.Pill
import com.catanna.ui.TagChip
import com.catanna.ui.TituloPagina
import com.catanna.ui.bordeDiscontinuo
import com.catanna.ui.punteadoAbajo
import com.catanna.ui.redondo
import com.catanna.ui.rotulo
import com.catanna.ui.t
import com.catanna.ui.tapa
import com.catanna.ui.tarjeta

private val lados = Modifier.padding(horizontal = 18.dp)

@Composable
private fun Rotulo(texto: String, modifier: Modifier = Modifier) =
    BasicText(texto.uppercase(), modifier, style = rotulo)

private fun estrellas(n: Int) = "★".repeat(n) + "☆".repeat(5 - n)

// ── Descubrir ───────────────────────────────────────────────────────────────

@Composable
fun Descubrir(vm: ClienteViewModel, ps: List<Producto>) {
    val lista = Vistas.filtrar(ps, vm.mol, vm.tipo)
    val ordenados = Vistas.ordenarPorNota(lista)
    val dest = ordenados.firstOrNull()

    Panel(conNav = true) {
        Rotulo("Molécula", Modifier.padding(start = 18.dp, top = 16.dp, bottom = 9.dp))
        FilaChips {
            Chip("Todas", vm.mol == null, C.tinta, Color.White) { vm.mol = null }
            Catalogo.MOLECULAS.forEach { m -> Chip(m, vm.mol == m, C.tinta, Color.White) { vm.mol = m } }
        }
        FilaChips(Modifier.padding(top = 8.dp)) {
            Chip("Todo", vm.tipo == null, C.rosaClaro, C.granate) { vm.tipo = null }
            Catalogo.TIPOS.forEach { k -> Chip(Vistas.plural(k), vm.tipo == k, C.rosaClaro, C.granate) { vm.tipo = k } }
        }

        if (dest != null) Destacado(dest) { vm.ir(Vista.Ficha(dest.id)) }

        Column(lados.padding(top = 22.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 11.dp), verticalAlignment = Alignment.Bottom) {
                Rotulo(if (vm.mol != null || vm.tipo != null) "Resultados" else "Todo el catálogo", Modifier.weight(1f))
                BasicText("${lista.size} de ${ps.size}", style = t(11.5f, 600, C.gris))
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                ordenados.drop(1).forEach { p ->
                    FilaProducto(p.fotoUrl, p.nombre, Vistas.sub(p), Vistas.notas(p), Vistas.nota(p), Vistas.nNotas(p)) {
                        vm.ir(Vista.Ficha(p.id))
                    }
                }
            }
            if (lista.isEmpty()) {
                BasicText(
                    if (ps.isEmpty()) "La carta está vacía todavía." else "Nada con esos filtros. Prueba otra molécula.",
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 26.dp),
                    style = t(13.5f, color = C.gris, lineHeight = 1.55f).copy(textAlign = TextAlign.Center),
                )
            }
        }
    }
}

@Composable
private fun FilaChips(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) { content() }
}

@Composable
private fun Destacado(p: Producto, onClick: () -> Unit) {
    Column(
        Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp).fillMaxWidth().tapa(onClick = onClick)
            .clip(redondo(20)).background(Color.White).border(1.dp, C.linea, redondo(20))
    ) {
        Box(Modifier.fillMaxWidth().height(180.dp)) {
            Foto(p.fotoUrl, p.nombre, Modifier.fillMaxSize())
            BasicText(
                "LO MEJOR VALORADO",
                Modifier.padding(12.dp).clip(Pill).background(C.tinta).padding(horizontal = 12.dp, vertical = 6.dp),
                style = t(10f, 800, Color.White, 0.09.em),
            )
        }
        Column(Modifier.padding(start = 17.dp, end = 17.dp, top = 15.dp, bottom = 17.dp)) {
            BasicText(p.nombre, style = t(25f, 900, spacing = (-0.015).em, lineHeight = 1.05f))
            BasicText(
                Vistas.sub(p) + if (p.detalle.isNotBlank()) " · " + p.detalle else "",
                Modifier.padding(top = 5.dp), style = t(12.5f, 600, C.gris),
            )
            val tags = Etiquetas.visibles(p)
            if (tags.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(top = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) { tags.forEach { TagChip(it.tag, it.n, pequena = true, atenuarN = false) } }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = t(19f).fontSize, fontWeight = FontWeight(900), color = C.tinta)) {
                            append(Vistas.nota(p))
                        }
                        append(" · " + Vistas.nNotas(p))
                    },
                    Modifier.weight(1f),
                    style = t(12f, 600, C.gris),
                )
                BasicText(
                    "Ver ficha",
                    Modifier.clip(Pill).background(C.rosa).padding(horizontal = 18.dp, vertical = 9.dp),
                    style = t(13f, 800),
                )
            }
        }
    }
}

// ── Carta ───────────────────────────────────────────────────────────────────

@Composable
fun Carta(vm: ClienteViewModel, ps: List<Producto>) {
    Panel(conNav = true) {
        TituloPagina("Carta completa", "${ps.size} referencias organizadas por molécula, como en la carta de la tienda.")
        Vistas.carta(ps).forEach { g ->
            Column(lados.padding(top = 20.dp)) {
                Row(
                    Modifier.fillMaxWidth().clip(redondo(12)).background(C.MOLBG[g.mol] ?: C.arena)
                        .padding(horizontal = 15.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(g.mol, style = t(17f, 900, spacing = (-0.01).em))
                    BasicText(g.resumen, style = t(11f, 700, C.resumenMol))
                }
                Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)) {
                    g.items.forEach { p ->
                        Row(
                            Modifier.fillMaxWidth().tapa { vm.ir(Vista.Ficha(p.id)) }.punteadoAbajo()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            BasicText(p.nombre, Modifier.weight(1f), style = t(14.5f, 700))
                            BasicText(p.tipo, style = t(11.5f, 600, C.gris))
                            BasicText(Vistas.nota(p), Modifier.width(56.dp), style = t(14.5f, 900).copy(textAlign = TextAlign.End))
                        }
                    }
                }
            }
        }
    }
}

// ── Ficha ───────────────────────────────────────────────────────────────────

@Composable
fun Ficha(vm: ClienteViewModel, f: Producto) {
    var todas by rememberSaveable(f.id) { mutableStateOf(false) }
    val tags = Etiquetas.visibles(f)
    val pub = Etiquetas.publicados(f)
    val pot = Vistas.mediaPotencia(f)
    val mia = vm.miResena(f)

    Panel(conNav = false) {
        vm.eco?.let { Aviso(it, C.verdeClaro, C.verdeTexto, lados.padding(top = 14.dp)) }

        Foto(f.fotoUrl, f.nombre, lados.padding(top = 14.dp).fillMaxWidth().height(230.dp).clip(redondo(20)))

        Column(lados.padding(top = 19.dp)) {
            BasicText(f.nombre, style = t(31f, 900, spacing = (-0.02).em, lineHeight = 1.02f))
            FlowRow(
                Modifier.padding(top = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Pastilla(f.molecula, C.tinta, Color.White, 800)
                Pastilla(f.tipo, C.verdeClaro, C.verde)
                if (f.detalle.isNotBlank()) Pastilla(f.detalle, C.arena, C.chipTexto)
            }
        }

        Column(
            lados.padding(top = 19.dp).fillMaxWidth().clip(redondo(20)).background(C.tinta)
                .padding(horizontal = 19.dp, vertical = 17.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                BasicText("POTENCIA PERCIBIDA", Modifier.weight(1f), style = t(11f, 800, C.potenciaLabel, 0.1.em))
                BasicText(
                    buildAnnotatedString {
                        append(if (pot > 0) "%.1f".format(pot).replace('.', ',') else "—")
                        withStyle(SpanStyle(fontSize = t(13f).fontSize, fontWeight = FontWeight(700), color = C.potenciaLabel)) { append(" /5") }
                    },
                    style = t(23f, 900, C.crema),
                )
            }
            Box(
                Modifier.padding(top = 12.dp, bottom = 7.dp).fillMaxWidth().height(10.dp).clip(Pill)
                    .background(C.crema.copy(alpha = 0.14f))
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth((pot / 5).toFloat()).clip(Pill).background(C.rosa))
            }
            Row(Modifier.fillMaxWidth()) {
                BasicText(
                    if (f.resenas.isEmpty()) "sin valoraciones" else "según ${f.resenas.size} " + if (f.resenas.size == 1) "persona" else "personas",
                    Modifier.weight(1f), style = t(11.5f, 600, C.potenciaLabel),
                )
                f.declarada?.let { BasicText("en bolsa: $it", style = t(11.5f, 600, C.potenciaLabel)) }
            }
        }

        Column(lados.padding(top = 23.dp)) {
            Rotulo("Lo que dicen los clientes", Modifier.padding(bottom = 11.dp))
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    tags.forEach { c -> TagChip(c.tag, c.n) { vm.ir(Vista.Etiqueta(c.tag, f.id)) } }
                }
                BasicText(
                    "Toca una etiqueta y verás de qué frases sale.",
                    Modifier.padding(top = 10.dp), style = t(12f, 600, C.gris),
                )
                if (pub.isEmpty()) {
                    BasicText(
                        "Provisionales: se publican al llegar a ${Etiquetas.UMBRAL} personas distintas.",
                        Modifier.padding(top = 10.dp).fillMaxWidth().clip(redondo(14)).background(C.arena)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        style = t(12.5f, 700, C.marron),
                    )
                }
            } else {
                BasicText(
                    "Sin etiquetas todavía. Aparecen cuando ${Etiquetas.UMBRAL} personas coinciden en la misma nota.",
                    Modifier.fillMaxWidth().bordeDiscontinuo(16.dp).padding(horizontal = 16.dp, vertical = 14.dp),
                    style = t(13f, color = C.gris, lineHeight = 1.5f),
                )
            }
        }

        if (f.formatos.isNotEmpty()) {
            Column(lados.padding(top = 23.dp)) {
                Rotulo("Formatos en tienda", Modifier.padding(bottom = 10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    f.formatos.forEach { z ->
                        Row(
                            Modifier.fillMaxWidth().clip(redondo(18)).background(C.tarjeta)
                                .border(1.5.dp, C.borde, redondo(18)).padding(horizontal = 17.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(z.g, Modifier.weight(1f), style = t(15f, 800))
                            BasicText(Vistas.porUnidad(z), Modifier.padding(end = 9.dp), style = t(11.5f, 600, C.gris))
                            BasicText(Vistas.eur(z.precio), style = t(17f, 900))
                        }
                    }
                }
                BasicText("Precios orientativos de la tienda.", Modifier.padding(top = 9.dp), style = t(12f, 600, C.gris))
            }
        }

        if (f.resenas.isNotEmpty()) {
            Column(lados.padding(top = 23.dp)) {
                Rotulo("Últimas valoraciones", Modifier.padding(bottom = 11.dp))
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    (if (todas) f.resenas else f.resenas.take(3)).forEach { r ->
                        Column(Modifier.fillMaxWidth().tarjeta().padding(horizontal = 16.dp, vertical = 14.dp)) {
                            BasicText("«${r.texto}»", style = t(14f, lineHeight = 1.5f))
                            Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                BasicText(
                                    r.alias + (if (r.uid == vm.usuario.uid) " (tú)" else "") + " · " + Vistas.fecha(r.fechaMillis),
                                    Modifier.weight(1f), style = t(11.5f, 600, C.gris),
                                )
                                BasicText(estrellas(r.estrellas), style = t(12f, 800, C.granate))
                            }
                        }
                    }
                }
                if (f.resenas.size > 3) {
                    BasicText(
                        if (todas) "Ver menos" else "Ver las ${f.resenas.size}",
                        Modifier.padding(top = 10.dp).tapa { todas = !todas },
                        style = t(12.5f, 800, C.chipTexto),
                    )
                }
            }
        }

        Aviso(
            if (mia != null) "Ya has valorado esta. Toca para verla o cambiarla." else "¿La has probado? Cuéntalo y valórala.",
            C.verdeClaro, C.verdeTexto, lados.padding(top = 20.dp),
        ) { vm.ir(Vista.Valorar(f.id)) }
    }
}

@Composable
private fun Pastilla(texto: String, bg: Color, color: Color, peso: Int = 700) {
    BasicText(
        texto,
        Modifier.clip(Pill).background(bg).padding(horizontal = 12.dp, vertical = 5.dp),
        style = t(12f, peso, color),
    )
}

// ── Etiqueta ────────────────────────────────────────────────────────────────

@Composable
fun Etiqueta(vm: ClienteViewModel, ps: List<Producto>, v: Vista.Etiqueta) {
    val e = Vistas.etiqueta(ps, v.tag, v.desdeId)
    val (bg, col) = C.tag(v.tag)

    Panel(conNav = false) {
        Column(lados.padding(top = 16.dp).fillMaxWidth().clip(redondo(20)).background(bg).padding(horizontal = 20.dp, vertical = 19.dp)) {
            BasicText(e.tag, style = t(30f, 900, col, lineHeight = 1f))
            Row(
                Modifier.padding(top = 18.dp).fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                e.barras.forEach { b ->
                    Box(
                        Modifier.weight(1f).fillMaxHeight(b.fraccion).clip(redondo(6))
                            .background(if (b.principal) col else col.copy(alpha = 0.4f))
                    )
                }
            }
            if (e.resumen.isNotEmpty()) {
                BasicText(e.resumen, Modifier.padding(top = 11.dp), style = t(12.5f, color = col, lineHeight = 1.5f))
            }
        }

        if (e.frases.isNotEmpty()) {
            Column(lados.padding(top = 22.dp)) {
                Rotulo("De estas frases sale", Modifier.padding(bottom = 11.dp))
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    e.frases.forEach { r ->
                        BasicText(
                            buildAnnotatedString {
                                append("«${r.texto}» ")
                                withStyle(SpanStyle(color = C.gris, fontSize = t(11.5f).fontSize, fontWeight = FontWeight(600))) { append(r.alias) }
                            },
                            Modifier.fillMaxWidth().tarjeta().padding(horizontal = 16.dp, vertical = 14.dp),
                            style = t(14f, lineHeight = 1.5f),
                        )
                    }
                }
            }
        }

        if (e.otras.isNotEmpty()) {
            Column(lados.padding(top = 22.dp)) {
                Rotulo("Otras con esta nota", Modifier.padding(bottom = 11.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    e.otras.forEach { o ->
                        Row(
                            Modifier.fillMaxWidth().tapa { vm.ir(Vista.Ficha(o.producto.id)) }.tarjeta()
                                .padding(horizontal = 13.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Foto(o.producto.fotoUrl, o.producto.nombre, Modifier.size(44.dp).clip(redondo(12)))
                            Column(Modifier.weight(1f)) {
                                BasicText(o.producto.nombre, style = t(14.5f, 800))
                                BasicText(
                                    Vistas.sub(o.producto) + " · ${o.menciones} " + if (o.menciones == 1) "mención" else "menciones",
                                    Modifier.padding(top = 2.dp), style = t(11.5f, 600, C.gris),
                                )
                            }
                            BasicText(Vistas.nota(o.producto), style = t(12.5f, 800))
                        }
                    }
                }
            }
        }

        Aviso(e.pie, C.arena, C.marron, lados.padding(top = 20.dp), size = 12.5f, peso = 600)
    }
}

// ── Valorar ─────────────────────────────────────────────────────────────────

@Composable
fun Valorar(vm: ClienteViewModel, p: Producto) {
    val det = Etiquetas.extrae(vm.vTexto)

    Panel(conNav = false) {
        Row(
            lados.padding(top = 16.dp).fillMaxWidth().clip(redondo(18)).background(C.verdeClaro)
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(C.verde), contentAlignment = Alignment.Center) {
                BasicText("✓", style = t(15f, color = Color.White))
            }
            BasicText(
                vm.vAnterior?.let { "Ya la valoraste el ${Vistas.fecha(it.fechaMillis)}. Si publicas, se sustituye por la nueva." }
                    ?: "Lo que escribas ayuda a los demás a elegir. De aquí salen las etiquetas.",
                style = t(12.5f, 600, C.verdeTexto, lineHeight = 1.45f),
            )
        }

        Row(lados.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Foto(p.fotoUrl, p.nombre, Modifier.size(60.dp).clip(redondo(16)))
            Column {
                BasicText(p.nombre, style = t(21f, 900, lineHeight = 1.1f))
                BasicText(Vistas.sub(p), Modifier.padding(top = 3.dp), style = t(12.5f, 600, C.gris))
            }
        }

        Column(lados.padding(top = 24.dp)) {
            Rotulo("1 · En general", Modifier.padding(bottom = 9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                (1..5).forEach { n ->
                    BasicText(
                        "★", Modifier.tapa { vm.vEstrellas = n },
                        style = t(33f, color = if (n <= vm.vEstrellas) C.granate else C.estrellaOff, lineHeight = 1f),
                    )
                }
            }
        }

        Column(lados.padding(top = 24.dp)) {
            Rotulo("2 · Potencia que has notado", Modifier.padding(bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { n ->
                    val sel = n == vm.vPotencia
                    BasicText(
                        n.toString(),
                        Modifier.weight(1f).tapa { vm.vPotencia = n }.clip(redondo(16))
                            .then(if (sel) Modifier.background(C.tinta) else Modifier.border(1.5.dp, C.bordeInput, redondo(16)))
                            .padding(vertical = 13.dp),
                        style = t(15f, 800, if (sel) Color.White else C.chipTexto).copy(textAlign = TextAlign.Center),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 7.dp)) {
                BasicText("suave", Modifier.weight(1f), style = t(11.5f, 600, C.gris))
                BasicText("muy potente", style = t(11.5f, 600, C.gris))
            }
        }

        Column(lados.padding(top = 24.dp)) {
            Rotulo("3 · A qué te ha recordado", Modifier.padding(bottom = 10.dp))
            FlowRow(
                Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Etiquetas.SIN.keys.forEach { tag ->
                    val (bg, col) = C.tag(tag)
                    Chip(tag, tag in vm.vChips, bg, col) { vm.alternarChip(tag) }
                }
            }
            BasicTextField(
                vm.vTexto, { vm.vTexto = it },
                Modifier.fillMaxWidth(),
                textStyle = t(14f, lineHeight = 1.5f),
                cursorBrush = SolidColor(C.tinta),
                decorationBox = { inner ->
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 92.dp).clip(redondo(18)).background(C.tarjeta)
                            .border(1.5.dp, C.borde, redondo(18)).padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        if (vm.vTexto.isEmpty()) {
                            BasicText(
                                "Cuéntalo con tus palabras. De aquí salen las etiquetas que ven los demás.",
                                style = t(14f, color = C.grisNav, lineHeight = 1.5f),
                            )
                        }
                        inner()
                    }
                },
            )
            if (det.si.isNotEmpty()) {
                Aviso(
                    "Detectado en tu texto: ${det.si.joinToString(", ")}. Se sumará a las etiquetas del producto.",
                    C.verdeClaro, C.verdeTexto, Modifier.padding(top = 11.dp), size = 12.5f, peso = 600,
                )
            }
            if (det.no.isNotEmpty()) {
                Aviso(
                    "Has dicho que NO tiene: ${det.no.joinToString(", ")}. Eso no cuenta como voto a favor.",
                    C.arena, C.marron, Modifier.padding(top = 9.dp), size = 12.5f, peso = 600,
                )
            }
        }

        Column(lados.padding(top = 22.dp, bottom = 2.dp)) {
            BasicText(
                buildAnnotatedString {
                    append("Aparecerás como ")
                    withStyle(SpanStyle(fontWeight = FontWeight(800), color = C.grisOscuro)) { append(vm.usuario.alias) }
                },
                Modifier.fillMaxWidth().padding(bottom = 11.dp),
                style = t(11.5f, 600, C.gris).copy(textAlign = TextAlign.Center),
            )
            BotonNegro(
                if (vm.puedePublicar) "Publicar valoración" else "Elige estrellas y potencia",
                enabled = vm.puedePublicar,
            ) { vm.publicar(p.id) }
        }
    }
}

// ── Probados ────────────────────────────────────────────────────────────────

@Composable
fun Probados(vm: ClienteViewModel, ps: List<Producto>) {
    val mios = ps.mapNotNull { p -> vm.miResena(p)?.let { p to it } }.sortedByDescending { it.second.fechaMillis }

    Panel(conNav = true) {
        TituloPagina("Tus productos", "Lo que has probado y valorado.")
        Column(lados.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            mios.forEach { (p, r) ->
                Row(
                    Modifier.fillMaxWidth().tapa { vm.ir(Vista.Ficha(p.id)) }.tarjeta().padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Foto(p.fotoUrl, p.nombre, Modifier.size(52.dp).clip(redondo(13)))
                    Column(Modifier.weight(1f)) {
                        BasicText(p.nombre, style = t(15f, 800))
                        BasicText(
                            "Le diste ${r.estrellas} ★ · ${Vistas.fecha(r.fechaMillis)}",
                            Modifier.padding(top = 3.dp), style = t(11.5f, 600, C.gris),
                        )
                    }
                    BasicText(
                        "Ver",
                        Modifier.clip(Pill).background(C.verdeClaro).padding(horizontal = 13.dp, vertical = 7.dp),
                        style = t(12f, 800, C.verde),
                    )
                }
            }
            if (mios.isEmpty()) {
                BasicText(
                    "Aún no has valorado nada. Abre una ficha y cuenta qué tal: así salen las etiquetas.",
                    Modifier.fillMaxWidth().bordeDiscontinuo(16.dp).padding(horizontal = 16.dp, vertical = 14.dp),
                    style = t(13f, color = C.gris, lineHeight = 1.5f),
                )
            }
        }
    }
}
