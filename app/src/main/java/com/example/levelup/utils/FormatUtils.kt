package com.example.levelup.utils

import java.text.NumberFormat
import java.util.Locale

fun Int.formatPrice(): String {
    val formatter = NumberFormat.getInstance(Locale("es", "CL"))
    return "$" + formatter.format(this)
}

fun Double.formatPrice(): String {
    val formatter = NumberFormat.getInstance(Locale("es", "CL"))
    return "$" + formatter.format(this)
}
