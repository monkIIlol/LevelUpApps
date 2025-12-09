package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.User
import com.example.levelup.data.repo.UserRepository
import com.example.levelup.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val current = SessionManager.currentUser.value
            if (current != null) {
                val dbUser = userRepository.getUser(current.email) ?: current
                _uiState.value = ProfileUiState(user = dbUser, isLoading = false)
            } else {
                _uiState.value = ProfileUiState(
                    user = null,
                    isLoading = false,
                    error = "No hay usuario en sesión"
                )
            }
        }
    }

    fun updatePhoto(uri: String) {
        val current = _uiState.value.user ?: return
        val updated = current.copy(photoUri = uri.ifBlank { null })
        saveUser(updated)
    }

    fun updateLocation(label: String, lat: Double?, lng: Double?) {
        val current = _uiState.value.user ?: return
        val updated = current.copy(
            location = label,
            locationLat = lat,
            locationLng = lng
        )
        saveUser(updated)
    }

    fun clearLocation() {
        val current = _uiState.value.user ?: return
        val updated = current.copy(
            location = null,
            locationLat = null,
            locationLng = null
        )
        saveUser(updated)
    }

    fun updatePassword(newPassword: String) {
        val current = _uiState.value.user ?: return
        val updated = current.copy(password = newPassword)
        saveUser(updated)
    }

    fun updateUserData(
        newName: String,
        newEmail: String,
        newPassword: String?
    ) {
        val current = _uiState.value.user ?: return
        val updated = current.copy(
            name = newName,
            email = newEmail,
            password = newPassword ?: current.password
        )
        saveUser(updated)
    }

    private fun saveUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
            SessionManager.login(user)
            _uiState.value = _uiState.value.copy(user = user)
        }
    }
}
