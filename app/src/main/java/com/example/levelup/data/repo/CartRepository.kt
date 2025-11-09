package com.example.levelup.data.repo

import com.example.levelup.data.local.dao.CartDao
import com.example.levelup.data.model.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val dao: CartDao) {

    fun getCartItems(): Flow<List<CartItem>> {
        return dao.getAllCartItems()
    }

    suspend fun addToCart(cartItem: CartItem) {
        val existing = dao.getItemByProductId(cartItem.productId)
        if (existing != null) {
            dao.updateQuantity(cartItem.productId, existing.quantity + cartItem.quantity)
        } else {
            dao.addToCart(cartItem)
        }
    }


    suspend fun removeFromCart(productId: Int) {
        dao.removeFromCart(productId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }
}
