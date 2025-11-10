package com.example.levelup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val productId: Int,
    val quantity: Int
)
