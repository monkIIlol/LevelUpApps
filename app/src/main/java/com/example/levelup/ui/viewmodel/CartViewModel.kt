package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.CartItem
import com.example.levelup.data.repo.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val repository: CartRepository,
    private val userEmail: String
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCartItems(userEmail).collect { items ->
                _cartItems.value = items
            }
        }
    }

    fun addProduct(productId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(userEmail, productId, quantity)
        }
    }

    fun increaseQuantity(item: CartItem) {
        addProduct(item.productId, 1)
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.addToCart(userEmail, item.productId, -1)
        }
    }

    fun removeItem(productId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(userEmail, productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart(userEmail)
        }
    }}
