package com.example.levelup.data.remote.dto

import com.example.levelup.data.model.Product
import com.google.gson.annotations.SerializedName

data class ProductDto(
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("precio")
    val precio: Int, // si en la API viene como String "549990.00", aquí podrías usar String y castear

    @SerializedName("stock")
    val stock: Int,

    @SerializedName("imagen")
    val imagen: String,

    @SerializedName("categoria_id")
    val categoriaId: Int
)

// DTO -> modelo de dominio (Product de tu app)
fun ProductDto.toModel(): Product {

    // Convertimos "549990.00" -> 549990 (Int)
    val priceInt = try {
        precio.toDouble().toInt()
    } catch (e: Exception) {
        0
    }

    return Product(
        id = id,
        name = nombre,
        description = descripcion,
        price = precio,
        stock = stock,
        imageUrl = imagen
    )
}
