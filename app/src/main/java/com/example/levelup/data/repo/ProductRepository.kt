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

            Log.d(TAG, "HTTP code: ${response.code()}")
            Log.d(TAG, "raw body: ${response.raw()}")


            if (response.isSuccessful) {
                val body = response.body()

                if (!body.isNullOrEmpty()) {
                    val remoteProducts = body.map { it.toModel() }

                    Log.d(TAG, "Productos obtenidos de API: ${remoteProducts.size}")

                    // Cacheamos en Room
                    dao.insertAll(remoteProducts)

                    remoteProducts
                } else {
                    Log.w(TAG, "Respuesta exitosa pero cuerpo vacío, usando datos locales")
                    dao.getAllProducts()
                }
            } else {
                Log.w(TAG, "Error HTTP ${response.code()}, usando datos locales")
                dao.getAllProducts()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red: ${e.message}, usando datos locales")
            dao.getAllProducts()
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}, usando datos locales")
            dao.getAllProducts()
        }
    }

    suspend fun getProductById(id: Int): Product? {
        return dao.getProductById(id)
    }

    suspend fun insertProducts(products: List<Product>) {
        dao.insertAll(products)
    }
}
