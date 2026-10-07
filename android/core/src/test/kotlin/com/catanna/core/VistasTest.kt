package com.catanna.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class VistasTest {

    @Test
    fun euros() {
        assertEquals("7 €", Vistas.eur(7.0))
        assertEquals("3,50 €", Vistas.eur(3.5))
    }

    @Test
    fun precioPorUnidad() {
        assertEquals("", Vistas.porUnidad(Formato("1 g", 7.0)))
        assertEquals("7 €/g", Vistas.porUnidad(Formato("2 g", 14.0)))
        assertEquals("40 €/ml", Vistas.porUnidad(Formato("0,5 ml", 20.0)))
        assertEquals("", Vistas.porUnidad(Formato("1 ud", 10.0)))
    }

    @Test
    fun notaMedia() {
        val p = Producto("a", "A", molecula = "THZ", tipo = "Resina", resenas = listOf(
            Resena("1", "", "", 5, 4, 0), Resena("2", "", "", 4, 4, 0)))
        assertEquals("4,5 ★", Vistas.nota(p))
        assertEquals("2 valoraciones", Vistas.nNotas(p))
        assertEquals("—", Vistas.nota(p.copy(resenas = emptyList())))
    }

    @Test
    fun iniciales() {
        assertEquals("M. R.", iniciales(" maría  ruiz gil "))
        assertEquals("A.", iniciales("Ana"))
    }

    @Test
    fun fechas() {
        val utc = TimeZone.getTimeZone("UTC")
        val ago12 = 1786492800000L // 2026-08-12 00:00 UTC
        assertEquals("12 ago", Vistas.fecha(ago12, ago12 + 86_400_000L * 3, utc))
        assertEquals("hoy", Vistas.fecha(ago12, ago12 + 3_600_000L, utc))
    }

    @Test
    fun cartaAgrupaPorMolecula() {
        val ps = listOf(
            Producto("1", "A", molecula = "FTS", tipo = "Resina"),
            Producto("2", "B", molecula = "OH10", tipo = "Flor"),
            Producto("3", "C", molecula = "OH10", tipo = "Vaper"),
            Producto("4", "D", molecula = "OH10", tipo = "Flor"),
        )
        val g = Vistas.carta(ps)
        assertEquals(listOf("OH10", "FTS"), g.map { it.mol })
        assertEquals("2 flores · 1 vaper", g[0].resumen)
    }
}
