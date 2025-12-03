package com.example.levelup.validators

import com.example.levelup.domain.validation.Validators
import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {
    @Test
    fun email_valido_devuelve_true() {
        val result = Validators.email("usuario@correo.com")
        assertTrue(result)
    }

    @Test
    fun email_invalido_devuelve_false() {
        val result = Validators.email("usuario-correo.com")
        assertFalse(result)
    }

    @Test
    fun password_corta_devuelve_false_y_password_larga_true() {
        assertFalse(Validators.password("123"))
        assertTrue(Validators.password("123456"))
    }
}