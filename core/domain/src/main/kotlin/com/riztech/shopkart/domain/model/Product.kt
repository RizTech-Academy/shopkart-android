package com.riztech.shopkart.domain.model

data class Rating(
    val average: Double,
    val count: Int,
)

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Money,
    val imageUrl: String,
    val rating: Rating,
    val inStock: Boolean,
)

data class Category(
    val slug: String,
    val name: String,
    val productCount: Int,
)
