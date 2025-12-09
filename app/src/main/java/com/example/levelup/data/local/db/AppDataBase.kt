package com.example.levelup.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.levelup.data.local.dao.CartDao
import com.example.levelup.data.local.dao.ProductDao
import com.example.levelup.data.local.dao.UserDao
import com.example.levelup.data.model.CartItem
import com.example.levelup.data.model.Product
import com.example.levelup.data.model.User

@Database(
    entities = [Product::class, User::class, CartItem::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
}
