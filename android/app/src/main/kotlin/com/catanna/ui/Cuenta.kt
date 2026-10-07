package com.catanna.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.catanna.data.Usuario

/** Lo que sale al tocar las iniciales de la cabecera. */
@Composable
fun DialogoCuenta(u: Usuario, onSalir: () -> Unit, onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Column(Modifier.fillMaxWidth().clip(redondo(24)).background(C.crema).padding(20.dp)) {
            BasicText(u.nombre, style = t(21f, 900, lineHeight = 1.1f))
            BasicText(
                "Tus valoraciones salen firmadas como ${u.alias}",
                Modifier.padding(top = 6.dp),
                style = t(13f, 600, C.gris, lineHeight = 1.45f),
            )
            BotonNegro("Cerrar sesión", modifier = Modifier.padding(top = 20.dp), onClick = onSalir)
            BotonBorde("Volver", Modifier.padding(top = 9.dp), onClick = onCerrar)
        }
    }
}

@Composable
fun Cargando(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        BasicText(texto, style = t(13.5f, 600, C.gris, lineHeight = 1.55f).copy(textAlign = TextAlign.Center))
    }
}
