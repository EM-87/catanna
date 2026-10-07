package com.catanna.cliente

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catanna.data.SesionViewModel
import com.catanna.data.Usuario
import com.catanna.ui.BotonCirculo
import com.catanna.ui.C
import com.catanna.ui.Cabecera
import com.catanna.ui.Cargando
import com.catanna.ui.DialogoCuenta
import com.catanna.ui.ItemNav
import com.catanna.ui.NavInferior

@Composable
fun ClienteApp(u: Usuario, sesion: SesionViewModel) {
    val vm: ClienteViewModel = viewModel()
    vm.usuario = u
    val productos by vm.productos.collectAsStateWithLifecycle()
    var cuenta by remember { mutableStateOf(false) }
    val v = vm.vista

    BackHandler(enabled = vm.hayPila) { vm.atras() }

    Column(Modifier.fillMaxSize()) {
        Cabecera(
            izquierda = {
                BotonCirculo(if (vm.hayPila) "‹" else "◉") {
                    if (vm.hayPila) vm.atras() else vm.raiz(Vista.Descubrir)
                }
            },
            derecha = { BotonCirculo(u.alias.filter { it.isLetter() }, 12f, 800, C.chipTexto) { cuenta = true } },
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            val ps = productos
            if (ps == null) Cargando("Cargando la carta…")
            else when (v) {
                Vista.Descubrir -> Descubrir(vm, ps)
                Vista.Carta -> Carta(vm, ps)
                Vista.Probados -> Probados(vm, ps)
                is Vista.Ficha -> ps.find { it.id == v.id }?.let { Ficha(vm, it) } ?: Cargando("Este producto ya no está en la carta.")
                is Vista.Etiqueta -> Etiqueta(vm, ps, v)
                is Vista.Valorar -> ps.find { it.id == v.id }?.let { Valorar(vm, it) } ?: Cargando("Este producto ya no está en la carta.")
            }
        }

        if (v in RAICES) {
            NavInferior(
                listOf(
                    Triple(Vista.Descubrir, "◈", "Descubrir"),
                    Triple(Vista.Carta, "◫", "Carta"),
                    Triple(Vista.Probados, "★", "Probados"),
                ).map { (k, icono, label) -> ItemNav(icono, label, v == k) { vm.raiz(k) } }
            )
        }
    }

    if (cuenta) DialogoCuenta(u, onSalir = { cuenta = false; sesion.salir() }, onCerrar = { cuenta = false })
}
