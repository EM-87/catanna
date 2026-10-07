package com.catanna.maestra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max

/** Deja la foto lista para subir: derecha, como mucho 1200 px de lado y en JPEG. */
object Fotos {
    private const val LADO_MAX = 1200

    fun desdeUri(ctx: Context, uri: Uri): ByteArray {
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se puede leer la foto")
        return preparar(bytes)
    }

    fun preparar(bytes: ByteArray): ByteArray {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
        var muestra = 1
        while (max(limites.outWidth, limites.outHeight) / (muestra * 2) >= LADO_MAX) muestra *= 2

        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = muestra })
            ?: error("No es una imagen")

        val lado = max(bmp.width, bmp.height)
        if (lado > LADO_MAX) {
            val k = LADO_MAX.toFloat() / lado
            bmp = Bitmap.createScaledBitmap(bmp, (bmp.width * k).toInt(), (bmp.height * k).toInt(), true)
        }

        val grados = when (ExifInterface(bytes.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (grados != 0f) {
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(grados) }, true)
        }

        return ByteArrayOutputStream().use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
    }
}
