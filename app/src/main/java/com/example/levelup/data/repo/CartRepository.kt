package com.example.levelup.data.repo

import com.example.levelup.data.local.dao.CartDao
import com.example.levelup.data.model.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val dao: CartDao) {

    fun getCartItems(userEmail: String): Flow<List<CartItem>> {
        return dao.getCartItems(userEmail)
    }

    suspend fun addToCart(userEmail: String, productId: Int, quantityDelta: Int) {
        val existing = dao.getItemByProductId(userEmail, productId)
        if (existing != null) {
            val newQuantity = existing.quantity + quantityDelta
            when {
                newQuantity > 0 -> dao.updateQuantity(userEmail, productId, newQuantity)
                else -> dao.removeFromCart(userEmail, productId)
            }
        } else if (quantityDelta > 0) {
            dao.addToCart(
                CartItem(
                    userEmail = userEmail,
                    productId = productId,
                    quantity = quantityDelta
                )
            )
        }
    }

    suspend fun removeFromCart(userEmail: String, productId: Int) {
        dao.removeFromCart(userEmail, productId)
    }

    suspend fun clearCart(userEmail: String) {
        dao.clearCart(userEmail)
    }
}
