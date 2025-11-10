package com.example.levelup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.levelup.data.model.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE userEmail = :userEmail")
    fun getCartItems(userEmail: String): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE userEmail = :userEmail AND productId = :productId")
    suspend fun removeFromCart(userEmail: String, productId: Int)

    @Query("DELETE FROM cart_items WHERE userEmail = :userEmail")
    suspend fun clearCart(userEmail: String)

    @Query("SELECT * FROM cart_items WHERE userEmail = :userEmail AND productId = :productId LIMIT 1")
    suspend fun getItemByProductId(userEmail: String, productId: Int): CartItem?

    @Query("UPDATE cart_items SET quantity = :newQuantity WHERE userEmail = :userEmail AND productId = :productId")
    suspend fun updateQuantity(userEmail: String, productId: Int, newQuantity: Int)

}
