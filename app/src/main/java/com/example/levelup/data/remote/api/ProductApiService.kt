package com.example.levelup.data.remote.api

import com.example.levelup.data.remote.dto.ProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {

    @GET("api/gaming/productos")
    suspend fun getAllProducts(): Response<List<ProductDto>>

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): Response<ProductDto>

    // Ejercicio opcional: productos por categoría
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String
    ): Response<List<ProductDto>>

    @GET("products")
    suspend fun getProductsWithLimit(
        @Query("limit") limit: Int
    ): Response<List<ProductDto>>
}
