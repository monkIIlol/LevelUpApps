package com.example.levelup.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.levelup.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private const val PREF_NAME = "levelup_session_prefs"
    private const val KEY_ID = "user_id"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PASSWORD = "user_password"
    private const val KEY_PHOTO_URI = "user_photo_uri"
    private const val KEY_LOCATION = "user_location"

    private lateinit var prefs: SharedPreferences

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadUserFromPrefs()
    }

    private fun loadUserFromPrefs() {
        val email = prefs.getString(KEY_EMAIL, null)
        val name = prefs.getString(KEY_NAME, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        val id = prefs.getInt(KEY_ID, 0)
        val photoUri = prefs.getString(KEY_PHOTO_URI, null)
        val location = prefs.getString(KEY_LOCATION, null)

        if (email != null && name != null && password != null) {
            _currentUser.value = User(
                id = id,
                name = name,
                email = email,
                password = password,
                photoUri = photoUri,
                location = location
            )
        } else {
            _currentUser.value = null
        }
    }

    fun login(user: User) {
        _currentUser.value = user
        if (::prefs.isInitialized) {
            prefs.edit()
                .putInt(KEY_ID, user.id)
                .putString(KEY_NAME, user.name)
                .putString(KEY_EMAIL, user.email)
                .putString(KEY_PASSWORD, user.password)
                .putString(KEY_PHOTO_URI, user.photoUri)
                .putString(KEY_LOCATION, user.location)
                .apply()
        }
    }

    fun logout() {
        _currentUser.value = null
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }
}
