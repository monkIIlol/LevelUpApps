package com.example.levelup.data.repo

import com.example.levelup.data.local.dao.ProductDao
import com.example.levelup.data.model.Product

class ProductRepository(private val dao: ProductDao) {

    suspend fun getAllProducts(): List<Product> {
        return dao.getAllProducts()
    }

    suspend fun getProductById(id: Int): Product? {
        return dao.getProductById(id)
    }

    suspend fun insertProducts(products: List<Product>) {
        dao.insertAll(products)
    }
}
