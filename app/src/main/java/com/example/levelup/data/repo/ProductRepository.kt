package com.example.levelup.data.repo

import android.util.Log
import com.example.levelup.data.local.dao.ProductDao
import com.example.levelup.data.model.Product
import com.example.levelup.data.remote.api.ProductApiService
import com.example.levelup.data.remote.dto.toModel
import java.io.IOException

class ProductRepository(
    private val dao: ProductDao,
    private val api: ProductApiService
) {

    companion object {
        private const val TAG = "ProductRepository"
    }

    suspend fun getAllProducts(): List<Product> {
        return try {
            Log.d(TAG, "Intentando obtener productos desde API...")

            val response = api.getAllProducts()

            if (response.isSuccessful) {
                val body = response.body()

                if (!body.isNullOrEmpty()) {
                    val products = body.map { it.toModel() }

                    dao.insertAll(products) // refresca caché local

                    return products
                }
            }

            // Si API falla o viene vacía…
            dao.getAllProducts()

        } catch (e: IOException) {
            Log.e(TAG, "Error red: ${e.message}, usando Room")
            dao.getAllProducts()
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}, usando Room")
            dao.getAllProducts()
        }
    }

    suspend fun getProductById(id: Int): Product? {
        return dao.getProductById(id)
    }

    suspend fun insertProducts(products: List<Product>) {
        dao.insertAll(products)
    }

    suspend fun upsertProduct(product: Product) {
        dao.upsertProduct(product)
    }

    suspend fun deleteProduct(id: Int) {
        dao.deleteProduct(id)
    }

    suspend fun getAllLocal(): List<Product> {
        return dao.getAllProducts()
    }
}
