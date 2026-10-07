package com.catanna

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catanna.data.Repo
import com.catanna.data.SesionViewModel
import com.catanna.data.Usuario
import com.catanna.maestra.MaestraApp
import com.catanna.maestra.NoEsMaestra
import com.catanna.ui.Cargando

const val ES_MAESTRA = true

@Composable
fun Contenido(u: Usuario, sesion: SesionViewModel) {
    val admin by remember(u.uid) { Repo.esAdmin(u.uid) }.collectAsStateWithLifecycle(null)
    when (admin) {
        null -> Cargando("Comprobando la cuenta…")
        false -> NoEsMaestra(u, sesion)
        true -> MaestraApp(u, sesion)
    }
}
