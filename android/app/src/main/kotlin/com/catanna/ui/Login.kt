package com.catanna.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.catanna.data.SesionViewModel

/** Entrar o crear cuenta. No estaba en el prototipo (era sin login); sigue su mismo lenguaje. */
@Composable
fun Login(sesion: SesionViewModel, maestra: Boolean) {
    var registro by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 40.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Logo(Modifier.padding(end = 9.dp))
            if (maestra) EtiquetaMaestra()
        }
        BasicText(
            if (registro) "Crea tu cuenta" else "Entra",
            Modifier.padding(top = 34.dp),
            style = t(31f, 900, spacing = (-0.02).em, lineHeight = 1.05f),
        )
        BasicText(
            when {
                maestra -> "Gestión del catálogo. Solo para cuentas marcadas como maestras."
                registro -> "Tus valoraciones salen firmadas solo con tus iniciales."
                else -> "Catanna es privada: entra con tu cuenta para ver la carta y valorar."
            },
            Modifier.padding(top = 8.dp),
            style = t(13.5f, 600, C.gris, lineHeight = 1.45f),
        )

        Column(Modifier.padding(top = 26.dp)) {
            if (registro) {
                Etiquetado("Nombre", Modifier.padding(bottom = 14.dp)) {
                    CampoPill(nombre, { nombre = it }, "María Ruiz")
                }
            }
            Etiquetado("Correo", Modifier.padding(bottom = 14.dp)) {
                CampoPill(email, { email = it }, "tu@correo.com", tipo = KeyboardType.Email)
            }
            Etiquetado("Contraseña") {
                CampoPill(clave, { clave = it }, if (registro) "Mínimo 6 caracteres" else "", tipo = KeyboardType.Password, secreto = true)
            }
        }

        sesion.error?.let { Aviso(it, C.rosaClaro, C.granate, Modifier.padding(top = 16.dp)) }
        sesion.aviso?.let { Aviso(it, C.verdeClaro, C.verdeTexto, Modifier.padding(top = 16.dp)) }

        BotonNegro(
            when {
                sesion.ocupado -> "Un momento…"
                registro -> "Crear cuenta"
                else -> "Entrar"
            },
            enabled = !sesion.ocupado && email.isNotBlank() && clave.isNotEmpty(),
            modifier = Modifier.padding(top = 22.dp),
        ) {
            if (registro) sesion.registrar(nombre, email, clave) else sesion.entrar(email, clave)
        }

        BotonBorde(
            if (registro) "Ya tengo cuenta" else "Crear una cuenta",
            Modifier.padding(top = 9.dp),
        ) { registro = !registro; sesion.limpiar() }

        if (!registro) {
            BasicText(
                "He olvidado la contraseña",
                Modifier.fillMaxWidth().padding(top = 18.dp).tapa { sesion.recuperar(email) },
                style = t(12.5f, 700, C.gris).copy(textAlign = TextAlign.Center),
            )
        }
    }
}

/** La pastilla negra «MAESTRA» junto al logotipo. */
@Composable
fun EtiquetaMaestra() {
    BasicText(
        "MAESTRA",
        Modifier.clip(Pill).background(C.tinta).padding(horizontal = 9.dp, vertical = 4.dp),
        style = t(10f, 800, Color.White, 0.1.em),
    )
}
