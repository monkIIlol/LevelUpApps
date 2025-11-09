package com.example.levelup.data.repo

import com.example.levelup.data.local.dao.UserDao
import com.example.levelup.data.model.User

class UserRepository(private val dao: UserDao) {

    suspend fun register(user: User) {
        dao.registerUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return dao.loginUser(email, password)
    }

    suspend fun getUser(email: String): User? {
        return dao.getUser(email)
    }
}
