package com.example.core.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Category(
    val id: Int,
    val name: String,
    val image: String
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: Category,
    val images: List<String>
) {
    // Computed Properties for the Fashion UX
    val rating: Float
        get() = (3.5f + (id % 16) * 0.1f).coerceAtMost(5.0f)

    val reviewCount: Int
        get() = 12 + (id * 7) % 180

    val originalPrice: Double
        get() = price * 1.4

    val discountedPrice: Double
        get() = price * 0.8
}
