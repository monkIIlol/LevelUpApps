package com.example.levelup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.levelup.data.model.Product
import com.example.levelup.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            val existing = repository.getAllProducts()
            if (existing.isEmpty()) {
                // Cargar productos iniciales (imágenes desde drawable)
                val demo = listOf(
                    Product(name = "Catan", description = "Juego de estrategia", price = 29990, stock = 23, imageUrl = "catan"),
                    Product(name = "Carcassonne", description = "Colocación de losetas", price = 24990, stock = 12, imageUrl = "carcasone"),
                    Product(name = "Control Xbox Series", description = "Inalámbrico", price = 59990, stock = 5, imageUrl = "xbosseries"),
                    Product(name = "HyperX Cloud II", description = "Sonido envolvente", price = 79990, stock = 35, imageUrl = "hyperxcloud"),
                    Product(name = "PlayStation 5", description = "Nueva generación", price = 549990, stock = 7, imageUrl = "pley5"),
                    Product(name = "PC Gamer ROG Strix", description = "Alto rendimiento", price = 1299990, stock = 2, imageUrl = "pcgamer"),
                    Product(name = "Silla Secretlab Titan", description = "Ergonómica", price = 349990, stock = 6, imageUrl = "sillagamer"),
                    Product(name = "Logitech G502 HERO", description = "Sensor preciso", price = 49990, stock = 37, imageUrl = "logitchg502"),
                    Product(name = "Razer Goliathus Ext.", description = "RGB", price = 29990, stock = 45, imageUrl = "mousepadrazer"),
                    Product(name = "Polera 'Level‑Up'", description = "Personalizable", price = 14990, stock = 777, imageUrl = "polera_negra")

                )
                repository.insertProducts(demo)
                _products.value = repository.getAllProducts()
            } else {
                _products.value = existing
            }
        }
    }
}
