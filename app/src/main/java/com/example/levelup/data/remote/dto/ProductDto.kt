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
    val precio: Int,

    @SerializedName("stock")
    val stock: Int,

    @SerializedName("imagen")
    val imagen: String,

    @SerializedName("categoriaId")
    val categoriaId: Int
)

// DTO -> modelo de dominio (Product de tu app)
fun ProductDto.toModel(): Product {

    val imageKey = when {
        nombre.contains("PlayStation 5", ignoreCase = true) -> "pley5"
        nombre.contains("Xbox Series X", ignoreCase = true) -> "xbosseries"
        nombre.contains("Zelda", ignoreCase = true) -> "zelda"
        nombre.contains("DualSense", ignoreCase = true) -> "dualsense"
        nombre.contains("4070", ignoreCase = true) -> "rtx4070"
        nombre.contains("Switch OLED", ignoreCase = true) -> "switcholed"
        nombre.contains("HyperX Cloud", ignoreCase = true) -> "hyperxcloud"
        nombre.contains("Elden Ring", ignoreCase = true) -> "eldenring"

        else -> "setup"   // imagen genérica por defecto
    }

    return Product(
        id = id,
        name = nombre,
        description = descripcion,
        price = precio,
        stock = stock,
        imageUrl = imageKey
    )
}
