package com.riztech.shopkart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val priceMinor: Long,
    val currency: String,
    val imageUrl: String,
    val ratingAverage: Double,
    val ratingCount: Int,
    val inStock: Boolean,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val slug: String,
    val name: String,
    val productCount: Int,
)

/**
 * Cart lines store only the product id plus quantity.
 *
 * Price deliberately is not copied here — a cached price would silently go
 * stale and the customer would see one number in the cart and another at
 * checkout. Lines are joined against the product table on read instead.
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val addedAtEpochMillis: Long,
)
