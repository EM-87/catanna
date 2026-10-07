package com.catanna

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catanna.data.SesionViewModel
import com.catanna.ui.C
import com.catanna.ui.CatannaTheme
import com.catanna.ui.Login
import com.catanna.ui.Logo
import com.catanna.ui.t
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val claro = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = claro, navigationBarStyle = claro)
        val conFirebase = FirebaseApp.getApps(this).isNotEmpty()
        setContent {
            CatannaTheme {
                Column(Modifier.fillMaxSize().background(C.crema).statusBarsPadding().imePadding()) {
                    if (!conFirebase) SinFirebase() else Raiz()
                }
            }
        }
    }
}

@Composable
private fun Raiz() {
    val sesion: SesionViewModel = viewModel()
    val u = sesion.usuario
    if (u == null) Login(sesion, ES_MAESTRA) else Contenido(u, sesion)
}

/** Falta app/google-services.json: se dice en pantalla en vez de cerrarse. */
@Composable
private fun SinFirebase() {
    Column(Modifier.padding(18.dp)) {
        Logo(Modifier.padding(top = 24.dp))
        BasicText("Falta configurar Firebase", Modifier.padding(top = 30.dp), style = t(24f, 900))
        BasicText(
            "Copia el google-services.json de tu proyecto de Firebase en android/app/ y vuelve a compilar. " +
                "Los pasos están en android/README.md.",
            Modifier.padding(top = 8.dp),
            style = t(13.5f, 600, C.gris, lineHeight = 1.5f),
        )
    }
}
