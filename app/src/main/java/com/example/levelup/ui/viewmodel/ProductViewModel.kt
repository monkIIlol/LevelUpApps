package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.demo.DemoProducts
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
                // Siempre normalizamos a una lista no nula
                val productsFromRepo = repository.getAllProducts()
                val realProducts = productsFromRepo ?: emptyList()

                // LÓGICA MODO DEMO:
                // - Si hay 0 o pocos productos reales (< 6) y el modo demo está activo → usamos demo.
                // - Si hay 6 o más → usamos solo los reales.
                val finalList: List<Product> = when {
                    realProducts.isEmpty() && DemoProducts.ENABLE_DEMO_PRODUCTS -> {
                        DemoProducts.items
                    }

                    realProducts.size < 6 && DemoProducts.ENABLE_DEMO_PRODUCTS -> {
                        DemoProducts.items
                    }

                    else -> realProducts
                }

                if (finalList.isNotEmpty()) {
                    _state.value = ProductState.Success(finalList)
                } else {
                    _state.value = ProductState.Error("No se encontraron productos.")
                }

            } catch (e: Exception) {
                // Si hay error (API caída, etc.) → fallback demo solo si está activado
                if (DemoProducts.ENABLE_DEMO_PRODUCTS) {
                    _state.value = ProductState.Success(DemoProducts.items)
                } else {
                    _state.value = ProductState.Error(
                        e.message ?: "Error al cargar productos"
                    )
                }
            }
        }
    }

    fun retry() {
        loadProducts()
    }
}
