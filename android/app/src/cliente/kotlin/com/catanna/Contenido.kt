package com.catanna

import androidx.compose.runtime.Composable
import com.catanna.cliente.ClienteApp
import com.catanna.data.SesionViewModel
import com.catanna.data.Usuario

const val ES_MAESTRA = false

@Composable
fun Contenido(u: Usuario, sesion: SesionViewModel) = ClienteApp(u, sesion)
