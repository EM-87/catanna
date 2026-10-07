package com.catanna.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EtiquetasTest {

    private fun r(uid: String, texto: String, estrellas: Int = 5, potencia: Int = 3) =
        Resena(uid, "X. X.", texto, estrellas, potencia, 0L)

    @Test
    fun detectaYDescartaNegacion() {
        val d = Etiquetas.extrae("Huele a limón pero no tiene nada de menta")
        assertEquals(listOf("citrico"), d.si)
        assertEquals(listOf("menta"), d.no)
    }

    @Test
    fun ignoraTildesYMayusculas() {
        assertEquals(listOf("diesel", "dulce"), Etiquetas.extrae("DIÉSEL y Azúcar").si)
    }

    @Test
    fun negacionSoloEnLasCuatroPalabrasAnteriores() {
        // «sin» queda a más de cuatro palabras de «limón»: cuenta a favor.
        assertEquals(listOf("citrico"), Etiquetas.extrae("sin duda una de las mejores, limón").si)
        assertEquals(listOf("dulce"), Etiquetas.extrae("Notas cítricas claras, casi como lima. Nada dulce.").no)
    }

    @Test
    fun publicaAlLlegarAlUmbral() {
        val tres = Producto("a", "A", molecula = "THZ", tipo = "Resina",
            resenas = (1..3).map { r("u$it", "muy cítrica") })
        assertTrue(Etiquetas.publicados(tres).isEmpty())
        assertEquals(listOf("citrico"), Etiquetas.visibles(tres).map { it.tag })

        val nueva = r("u4", "a limón")
        assertEquals(listOf("citrico"), Vistas.estrenadas(tres, nueva))
        val cuatro = tres.copy(resenas = tres.resenas + nueva)
        assertEquals(4, Etiquetas.publicados(cuatro).single().n)
    }

    @Test
    fun editarTuValoracionNoCuentaDosVeces() {
        val p = Producto("a", "A", molecula = "THZ", tipo = "Resina",
            resenas = (1..3).map { r("u$it", "limón") })
        // u1 reescribe la suya: siguen siendo tres clientes, no se publica.
        assertTrue(Vistas.estrenadas(p, r("u1", "limón y lima")).isEmpty())
    }

    @Test
    fun dieselDryDelPrototipo() {
        val diesel = Producto("diesel", "Diésel Dry", molecula = "THZ", tipo = "Resina", resenas = listOf(
            r("1", "Huele a gasolina de verdad, muy fuerte al abrir la bolsa, y luego queda un fondo a limón que no esperaba."),
            r("2", "Cítrica y terrosa a la vez. Potente, pero menos de lo que decía la bolsa."),
            r("3", "Al abrir me recordó a piel de naranja, muy fresca, con el diésel detrás."),
            r("4", "Notas cítricas claras, casi como lima. Nada dulce."),
            r("5", "Diésel puro y algo de tierra húmeda. Muy potente."),
        ))
        val tags = Etiquetas.tagsDe(diesel).associate { it.tag to it.n }
        assertEquals(4, tags["citrico"])
        assertEquals(3, tags["diesel"])
        assertEquals(null, tags["dulce"])
        assertEquals(listOf("citrico"), Etiquetas.publicados(diesel).map { it.tag })
    }
}
