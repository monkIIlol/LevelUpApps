package com.example.levelup.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.User
import com.example.levelup.data.repo.UserRepository
import com.example.levelup.data.session.SessionManager
import com.example.levelup.domain.validation.Validators
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(
            email = value,
            emailError = !Validators.email(value),
            errorMessage = null,
            successMessage = null
        )
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(
            password = value,
            passwordError = !Validators.password(value),
            errorMessage = null,
            successMessage = null
        )
    }

    fun onNameChange(value: String) {
        uiState = uiState.copy(
            name = value,
            nameError = !Validators.nonEmpty(value),
            errorMessage = null,
            successMessage = null
        )
    }

    fun register() {
        if (uiState.hasErrors()) return

        viewModelScope.launch {
            // limpiar mensajes anteriores
            uiState = uiState.copy(successMessage = null, errorMessage = null)

            // 👉 Comprobar si el correo ya existe
            val existing = userRepository.getUser(uiState.email)
            if (existing != null) {
                uiState = uiState.copy(
                    errorMessage = "Ya existe una cuenta con ese correo",
                    successMessage = null
                )
                return@launch
            }

            // Registrar usuario nuevo
            userRepository.register(
                User(
                    email = uiState.email,
                    name = uiState.name,
                    password = uiState.password
                )
            )
            uiState = uiState.copy(
                successMessage = "Registro exitoso",
                errorMessage = null
            )
        }
    }

    fun login() {
        if (uiState.emailError || uiState.passwordError) return

        uiState = uiState.copy(successMessage = null, errorMessage = null)

        viewModelScope.launch {
            val user = userRepository.login(uiState.email, uiState.password)
            if (user != null) {
                SessionManager.login(user)
                uiState = uiState.copy(
                    successMessage = "Bienvenido ${user.name}",
                    errorMessage = null
                )
            } else {
                uiState = uiState.copy(
                    errorMessage = "Credenciales inválidas",
                    successMessage = null
                )
            }
        }
    }
}

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    fun hasErrors() = nameError || emailError || passwordError
}
