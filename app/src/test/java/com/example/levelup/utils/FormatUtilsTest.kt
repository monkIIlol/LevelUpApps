package com.example.levelup.utils

import com.example.levelup.utils.formatPrice
import org.junit.Assert.*
import org.junit.Test

class FormatUtilsTest {
    @Test
    fun int_formatPrice_agrega_simbolo_y_separador_de_miles() {
        val price = 10000
        val formatted = price.formatPrice()
        assertTrue(formatted.startsWith("$"))
        assertTrue(formatted.contains("10") && formatted.contains("000"))
    }

    @Test
    fun double_formatPrice_agrega_simbolo_y_formato_correcto() {
        val price = 9999.0
        val formatted = price.formatPrice()
        assertTrue(formatted.startsWith("$"))
        assertTrue(formatted.contains("9"))
    }
}