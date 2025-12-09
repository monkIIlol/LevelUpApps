package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.User
import com.example.levelup.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState(isLoading = true))
    val uiState: StateFlow<AdminUiState> = _uiState

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val users = userRepository.getAllUsers()

            _uiState.value = AdminUiState(
                users = users,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    fun createUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val newUser = User(
                    name = name,
                    email = email,
                    password = password
                )
                userRepository.register(newUser)
                loadUsers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al crear usuario (¿correo duplicado?)"
                )
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
            loadUsers()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
            loadUsers()
        }
    }
}
