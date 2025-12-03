package com.example.levelup.data.session

import com.example.levelup.data.model.User
import com.example.levelup.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SessionManagerTest {
    @Test
    fun login_guarda_usuario_en_currentUser() = runBlocking {
        val user = User(
            id = 1,
            name = "Cristian",
            email = "test@correo.com",
            password = "123456"
        )

        SessionManager.login(user)

        val current = SessionManager.currentUser.value
        assertEquals(user, current)
    }

    @Test
    fun logout_deja_currentUser_en_null() = runBlocking {
        val user = User(
            id = 1,
            name = "Cristian",
            email = "test@correo.com",
            password = "123456"
        )

        SessionManager.login(user)
        SessionManager.logout()

        val current = SessionManager.currentUser.value
        assertNull(current)
    }
}