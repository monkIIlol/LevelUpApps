package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.CartItem
import com.example.levelup.data.repo.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCartItems().collect { items ->
                _cartItems.value = items
            }
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.addToCart(CartItem(productId = item.productId, quantity = 1))
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                repository.addToCart(CartItem(productId = item.productId, quantity = -1))
            } else {
                repository.removeFromCart(item.productId)
            }
        }
    }

    fun clearCart() = viewModelScope.launch { repository.clearCart() }
}
