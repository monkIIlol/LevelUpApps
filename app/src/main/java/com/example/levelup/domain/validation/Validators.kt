package com.example.levelup.domain.validation

object Validators {
    fun email(value: String): Boolean =
        value.contains("@") && value.contains(".")

    fun password(value: String): Boolean =
        value.length >= 6

    fun nonEmpty(value: String): Boolean =
        value.isNotBlank()
}
