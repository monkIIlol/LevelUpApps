package com.example.levelup.ui.catalog

import com.example.levelup.R
import com.example.levelup.ui.catalog.productImageResource
import org.junit.Assert.*
import org.junit.Test

class ProductImagesTest {
        @Test
        fun productImageResource_devuelve_misma_imagen_para_claves_desconocidas() {
            val res1 = productImageResource("clave_inexistente_1")
            val res2 = productImageResource("otra_clave_inexistente")

            assertEquals(res1, res2)   // todas las desconocidas usan el mismo fallback
        }

        @Test
        fun productImageResource_devuelve_alguna_imagen_para_clave_conocida_o_placeholder() {
            val res = productImageResource("catan")

            // Simplemente verificamos que no sea 0, es decir, que devuelve un drawable válido
            assertNotEquals(0, res)
        }
}