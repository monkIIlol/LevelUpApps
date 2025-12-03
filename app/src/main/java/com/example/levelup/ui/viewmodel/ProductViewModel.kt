package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.Product
import com.example.levelup.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados posibles de la pantalla de productos
sealed class ProductState {
    object Initial : ProductState()
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class Error(val message: String) : ProductState()
}

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _state = MutableStateFlow<ProductState>(ProductState.Initial)
    val state: StateFlow<ProductState> = _state

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = ProductState.Loading
            try {
                val products = repository.getAllProducts()

                if (products.isNotEmpty()) {
                    _state.value = ProductState.Success(products)
                } else {
                    _state.value = ProductState.Error("No se encontraron productos.")
                }
            } catch (e: Exception) {
                _state.value = ProductState.Error(
                    e.message ?: "Error al cargar productos"
                )
            }
        }
    }

    fun retry() {
        loadProducts()
    }
}
