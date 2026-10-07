package com.catanna.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catanna.core.iniciales
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Usuario(val uid: String, val nombre: String) {
    /** Así firma sus valoraciones: solo iniciales. */
    val alias get() = iniciales(nombre)
}

/** Quién está dentro. App privada: hace falta cuenta para ver y valorar. */
class SesionViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var usuario by mutableStateOf(auth.currentUser?.aUsuario())
        private set
    var ocupado by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var aviso by mutableStateOf<String?>(null)
        private set

    private val escucha = FirebaseAuth.AuthStateListener { usuario = it.currentUser?.aUsuario() }

    init {
        auth.addAuthStateListener(escucha)
    }

    override fun onCleared() = auth.removeAuthStateListener(escucha)

    private fun FirebaseUser.aUsuario() = Usuario(uid, displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty())

    fun limpiar() {
        error = null
        aviso = null
    }

    fun entrar(email: String, clave: String) = intento {
        auth.signInWithEmailAndPassword(email.trim(), clave).await()
    }

    fun registrar(nombre: String, email: String, clave: String) = intento {
        if (nombre.isBlank()) throw IllegalArgumentException("Pon tu nombre: de ahí salen tus iniciales.")
        val u = auth.createUserWithEmailAndPassword(email.trim(), clave).await().user ?: return@intento
        u.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nombre.trim()).build()).await()
        usuario = u.aUsuario().copy(nombre = nombre.trim())
    }

    fun recuperar(email: String) = intento {
        if (email.isBlank()) throw IllegalArgumentException("Escribe tu correo y te mandamos un enlace.")
        auth.sendPasswordResetEmail(email.trim()).await()
        aviso = "Te hemos mandado un correo para cambiar la contraseña."
    }

    fun salir() = auth.signOut()

    private fun intento(bloque: suspend () -> Unit) {
        limpiar()
        ocupado = true
        viewModelScope.launch {
            try {
                bloque()
            } catch (e: FirebaseAuthException) {
                error = traduce(e.errorCode)
            } catch (e: IllegalArgumentException) {
                error = e.message
            } catch (e: Exception) {
                error = "No se ha podido conectar. Revisa la conexión."
            } finally {
                ocupado = false
            }
        }
    }

    private fun traduce(codigo: String) = when (codigo) {
        "ERROR_INVALID_EMAIL" -> "Ese correo no es válido."
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya hay una cuenta con ese correo. Entra con ella."
        "ERROR_WEAK_PASSWORD" -> "La contraseña tiene que tener al menos 6 caracteres."
        "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un poco."
        else -> "No se ha podido entrar ($codigo)."
    }
}
