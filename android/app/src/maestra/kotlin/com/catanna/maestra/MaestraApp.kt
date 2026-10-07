@file:OptIn(ExperimentalLayoutApi::class)

package com.catanna.maestra

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catanna.core.Catalogo
import com.catanna.core.Etiquetas
import com.catanna.core.Producto
import com.catanna.core.Vistas
import com.catanna.data.SesionViewModel
import com.catanna.data.Usuario
import com.catanna.ui.Aviso
import com.catanna.ui.BotonBorde
import com.catanna.ui.BotonCirculo
import com.catanna.ui.BotonNegro
import com.catanna.ui.C
import com.catanna.ui.CampoPill
import com.catanna.ui.Cargando
import com.catanna.ui.Chip
import com.catanna.ui.DialogoCuenta
import com.catanna.ui.EtiquetaMaestra
import com.catanna.ui.Etiquetado
import com.catanna.ui.FilaProducto
import com.catanna.ui.Foto
import com.catanna.ui.ItemNav
import com.catanna.ui.Logo
import com.catanna.ui.NavInferior
import com.catanna.ui.Panel
import com.catanna.ui.Pill
import com.catanna.ui.TituloPagina
import com.catanna.ui.bordeDiscontinuo
import com.catanna.ui.lineaAbajo
import com.catanna.ui.redondo
import com.catanna.ui.rotulo
import com.catanna.ui.t
import com.catanna.ui.tapa
import com.catanna.ui.tarjeta
import java.io.File

private val lados = Modifier.padding(horizontal = 18.dp)

@Composable
private fun CabeceraMaestra(u: Usuario, onCuenta: (() -> Unit)?) {
    Row(
        Modifier.fillMaxWidth().background(C.crema).lineaAbajo()
            .padding(start = 18.dp, end = 18.dp, top = 13.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Logo(Modifier.padding(end = 9.dp))
        EtiquetaMaestra()
        Box(Modifier.weight(1f))
        BotonCirculo(u.alias.filter { it.isLetter() }, 12f, 800, C.chipTexto, onCuenta)
    }
}

@Composable
fun MaestraApp(u: Usuario, sesion: SesionViewModel) {
    val vm: MaestraViewModel = viewModel()
    val productos by vm.productos.collectAsStateWithLifecycle()
    var cuenta by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        CabeceraMaestra(u) { cuenta = true }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val ps = productos
            when {
                vm.pestana == Pestana.ALTA -> Alta(vm)
                ps == null -> Cargando("Cargando el catálogo…")
                vm.pestana == Pestana.CATALOGO -> CatalogoPanel(vm, ps)
                else -> EtiquetasPanel(ps)
            }
        }
        NavInferior(
            listOf(
                Triple(Pestana.ALTA, "+", "Nuevo"),
                Triple(Pestana.CATALOGO, "◫", "Catálogo"),
                Triple(Pestana.ETIQUETAS, "◈", "Etiquetas"),
            ).map { (k, icono, label) -> ItemNav(icono, label, vm.pestana == k) { vm.pestana = k } }
        )
    }

    if (cuenta) DialogoCuenta(u, onSalir = { cuenta = false; sesion.salir() }, onCerrar = { cuenta = false })
}

// ── Nuevo / editar ──────────────────────────────────────────────────────────

@Composable
private fun Alta(vm: MaestraViewModel) {
    val ctx = LocalContext.current
    val e = vm.editando
    var elegirFoto by remember { mutableStateOf(false) }
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.foto = uri
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.foto = uriCamara
    }

    val fotoVista = vm.foto?.toString() ?: e?.fotoUrl

    Panel(conNav = true) {
        TituloPagina(
            if (e == null) "Nuevo producto" else "Editar producto",
            if (e == null) "Cuatro campos y una foto. Las etiquetas las ponen los clientes."
            else if (e.publicado) "Está publicado: los cambios se ven al momento en la app."
            else "Es un borrador: nadie lo ve todavía.",
        )

        Box(
            lados.padding(top = 18.dp).fillMaxWidth().height(150.dp).tapa { elegirFoto = true }
                .clip(redondo(20)).background(C.arena).bordeDiscontinuo(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (fotoVista != null) {
                Foto(fotoVista, vm.nombre, Modifier.fillMaxSize())
            } else {
                BasicText(
                    "Toca para hacer la foto\no elegirla de la galería",
                    style = t(12f, 700, C.gris, lineHeight = 1.55f).copy(textAlign = TextAlign.Center),
                )
            }
        }

        Column(lados.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Etiquetado("Nombre") { CampoPill(vm.nombre, { vm.nombre = it }, "Kush Mint") }
            Etiquetado("Distribuidor") { CampoPill(vm.distribuidor, { vm.distribuidor = it }, "Elegir…") }
            Etiquetado("Detalle (opcional)") { CampoPill(vm.detalle, { vm.detalle = it }, "Dry triple filtrado") }
        }

        Etiquetado("Molécula", lados.padding(top = 18.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Catalogo.MOLECULAS.forEach { m -> Chip(m, vm.molecula == m, C.tinta, Color.White) { vm.elegirMolecula(m) } }
            }
        }

        Etiquetado("Formato", lados.padding(top = 16.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Catalogo.TIPOS.forEach { k -> Chip(k, vm.tipo == k, C.verdeClaro, C.verde) { vm.elegirTipo(k) } }
            }
        }

        Etiquetado("Potencia que pone la bolsa (opcional)", lados.padding(top = 16.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("baja", "media", "alta").forEach { d ->
                    Chip(d, vm.declarada == d, C.rosaClaro, C.granate) { vm.declarada = if (vm.declarada == d) null else d }
                }
            }
        }

        Etiquetado("Tamaños y precio en tienda", lados.padding(top = 16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.filas.forEachIndexed { i, fila ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (fila.libre) {
                            CampoPill(fila.g, { vm.cambiarFila(i, fila.copy(g = it)) }, "50 g", Modifier.width(86.dp), compacto = true)
                        } else {
                            BasicText(fila.g, Modifier.width(56.dp), style = t(14f, 800))
                        }
                        CampoPill(
                            fila.precio, { vm.cambiarFila(i, fila.copy(precio = it)) }, "Precio (vacío = no lo hay)",
                            Modifier.weight(1f), tipo = KeyboardType.Decimal, compacto = true,
                        )
                    }
                }
                BasicText("+ Otro tamaño", Modifier.padding(top = 2.dp).tapa { vm.otroTamano() }, style = t(12.5f, 800, C.chipTexto))
            }
        }

        Column(lados.padding(top = 22.dp)) {
            BasicText("ASÍ LO VERÁ EL CLIENTE", Modifier.padding(bottom = 10.dp), style = rotulo)
            val p = e
            FilaProducto(
                fotoVista, vm.nombre.ifBlank { "Sin nombre" }, vm.molecula + " " + vm.tipo.lowercase(),
                p?.let { Vistas.notas(it) } ?: "sin etiquetas",
                p?.let { Vistas.nota(it) } ?: "—",
                p?.let { Vistas.nNotas(it) } ?: "0 valoraciones",
            )
        }

        Column(lados.padding(top = 20.dp, bottom = 2.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (e == null) {
                BotonNegro(if (vm.ocupado) "Publicando…" else "Publicar en el catálogo", enabled = !vm.ocupado) { vm.guardar(true) }
                BotonBorde("Guardar borrador") { if (!vm.ocupado) vm.guardar(false) }
            } else {
                BotonNegro(if (vm.ocupado) "Guardando…" else "Guardar cambios", enabled = !vm.ocupado) { vm.guardar(null) }
                BotonBorde(if (e.publicado) "Retirar de la app" else "Publicar en la app") { vm.cambiarPublicado(e, !e.publicado) }
                BasicText(
                    "Cancelar edición",
                    Modifier.fillMaxWidth().padding(top = 6.dp).tapa { vm.limpiar() },
                    style = t(12.5f, 700, C.gris).copy(textAlign = TextAlign.Center),
                )
            }
            vm.eco?.let {
                if (vm.ecoError) Aviso(it, C.rosaClaro, C.granate) else Aviso(it, C.verdeClaro, C.verdeTexto)
            }
        }
    }

    if (elegirFoto) {
        Dialog(onDismissRequest = { elegirFoto = false }) {
            Column(Modifier.fillMaxWidth().clip(redondo(24)).background(C.crema).padding(20.dp)) {
                BasicText("Foto del producto", style = t(21f, 900))
                BotonNegro("Hacer foto", modifier = Modifier.padding(top = 18.dp)) {
                    elegirFoto = false
                    val dir = File(ctx.cacheDir, "fotos").apply { mkdirs() }
                    val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fotos", File(dir, "foto-${System.currentTimeMillis()}.jpg"))
                    uriCamara = uri
                    camara.launch(uri)
                }
                BotonBorde("Elegir de la galería", Modifier.padding(top = 9.dp)) {
                    elegirFoto = false
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                if (vm.foto != null) {
                    BasicText(
                        "Quitar la foto nueva",
                        Modifier.fillMaxWidth().padding(top = 14.dp).tapa { vm.foto = null; elegirFoto = false },
                        style = t(12.5f, 700, C.gris).copy(textAlign = TextAlign.Center),
                    )
                }
            }
        }
    }
}

// ── Catálogo ────────────────────────────────────────────────────────────────

@Composable
private fun CatalogoPanel(vm: MaestraViewModel, ps: List<Producto>) {
    Panel(conNav = true) {
        Row(lados.padding(top = 18.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                BasicText("Catálogo", style = t(26f, 900, spacing = (-0.02).em))
                BasicText(
                    "${ps.size} referencias · ${ps.sumOf { it.resenas.size }} valoraciones",
                    Modifier.padding(top = 6.dp), style = t(13f, 600, C.gris),
                )
            }
            BasicText(
                "+ Nuevo",
                Modifier.tapa { vm.limpiar(); vm.pestana = Pestana.ALTA }.clip(Pill).background(C.tinta)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                style = t(13f, 800, Color.White),
            )
        }

        vm.eco?.let {
            Box(lados.padding(top = 14.dp)) {
                if (vm.ecoError) Aviso(it, C.rosaClaro, C.granate) else Aviso(it, C.verdeClaro, C.verdeTexto)
            }
        }

        if (ps.isEmpty()) {
            Column(lados.padding(top = 18.dp)) {
                BasicText(
                    "El catálogo está vacío. Puedes empezar dando de alta productos o cargar la carta de ejemplo " +
                        "(${SEMILLA.size} referencias de la carta de la tienda, sin valoraciones).",
                    Modifier.fillMaxWidth().bordeDiscontinuo(16.dp).padding(horizontal = 16.dp, vertical = 14.dp),
                    style = t(13f, color = C.gris, lineHeight = 1.5f),
                )
                BotonNegro(vm.sembrando ?: "Cargar la carta de ejemplo", enabled = vm.sembrando == null, modifier = Modifier.padding(top = 12.dp)) {
                    vm.sembrar()
                }
            }
        }

        Column(lados.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            ps.forEach { p ->
                Column(Modifier.fillMaxWidth().tapa { vm.editar(p) }.tarjeta().padding(horizontal = 15.dp, vertical = 13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BasicText(p.nombre, Modifier.weight(1f), style = t(15f, 800))
                        if (!p.publicado) {
                            BasicText(
                                "borrador",
                                Modifier.clip(Pill).background(C.arena).padding(horizontal = 10.dp, vertical = 4.dp),
                                style = t(11f, 800, C.marron),
                            )
                        }
                        BasicText(
                            if (p.resenas.isEmpty()) "—" else "%.1f ★".format(java.util.Locale.ROOT, Vistas.mediaEstrellas(p)),
                            style = t(14f, 900),
                        )
                    }
                    BasicText(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight(800), color = C.granate)) {
                                append(p.molecula)
                            }
                            append(" · ${p.tipo} · ${Vistas.nNotas(p)}")
                        },
                        Modifier.padding(top = 3.dp), style = t(11.5f, 600, C.gris),
                    )
                    BasicText(
                        Etiquetas.visibles(p).joinToString(" · ") { "${it.tag} ${it.n}" }.ifEmpty { "sin etiquetas" },
                        Modifier.padding(top = 6.dp), style = t(12f, 700, C.marron),
                    )
                }
            }
        }
    }
}

// ── Etiquetas ───────────────────────────────────────────────────────────────

@Composable
private fun EtiquetasPanel(ps: List<Producto>) {
    val filas = Vistas.panelEtiquetas(ps)
    Panel(conNav = true) {
        TituloPagina(
            "Etiquetas",
            "Una etiqueta se publica sola con ${Etiquetas.UMBRAL} personas distintas. Estas son las palabras que cuentan para cada una.",
        )
        Column(lados.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            filas.forEach { f ->
                val (bg, col) = if (f.publicada) C.tag(f.tag) else (C.arena to C.marron)
                Column(Modifier.fillMaxWidth().tarjeta().padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText(f.tag, Modifier.weight(1f), style = t(17f, 900))
                        BasicText(
                            if (f.publicada) "publicada" else "provisional",
                            Modifier.clip(Pill).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
                            style = t(11f, 800, col),
                        )
                    }
                    BasicText(f.donde, Modifier.padding(top = 7.dp), style = t(12f, 600, C.gris, lineHeight = 1.45f))
                    BasicText(f.sinonimos, Modifier.padding(top = 7.dp), style = t(11.5f, 700, C.marron, lineHeight = 1.5f))
                }
            }
            if (filas.isEmpty()) {
                BasicText(
                    "Aún no hay etiquetas. Salen solas de lo que escriben los clientes al valorar.",
                    Modifier.fillMaxWidth().bordeDiscontinuo(16.dp).padding(horizontal = 16.dp, vertical = 14.dp),
                    style = t(13f, color = C.gris, lineHeight = 1.5f),
                )
            }
        }
    }
}

// ── Cuenta sin permiso ──────────────────────────────────────────────────────

@Composable
fun NoEsMaestra(u: Usuario, sesion: SesionViewModel) {
    val portapapeles = LocalClipboardManager.current
    Column(Modifier.fillMaxSize()) {
        CabeceraMaestra(u, null)
        Panel(conNav = false) {
            TituloPagina(
                "Esta cuenta no es maestra",
                "Para dar permiso de maestra a esta cuenta, crea en Firestore " +
                    "un documento vacío en la colección «admins» cuyo id sea este:",
            )
            SelectionContainer(lados.padding(top = 16.dp)) {
                BasicText(
                    u.uid,
                    Modifier.fillMaxWidth().tarjeta(14).padding(horizontal = 14.dp, vertical = 12.dp),
                    style = t(13.5f, 700),
                )
            }
            Column(lados.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                BotonNegro("Copiar el id") { portapapeles.setText(AnnotatedString(u.uid)) }
                BotonBorde("Cerrar sesión") { sesion.salir() }
            }
        }
    }
}
