package com.example.levelup.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,

    val photoUri: String? = null,
    val location: String? = null,
    val locationLat: Double? = null,
    val locationLng: Double? = null
)
