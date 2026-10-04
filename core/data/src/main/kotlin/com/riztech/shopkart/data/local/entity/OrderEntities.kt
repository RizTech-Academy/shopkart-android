package com.riztech.shopkart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val reference: String,
    val totalMinor: Long,
    val currency: String,
    val placedAtEpochMillis: Long,
)

/**
 * An order line snapshots the product.
 *
 * Unlike the cart, which joins to the live product row, an order copies title
 * and price at the moment it was placed. A past order must keep showing what
 * the customer actually paid, even after the catalogue changes or the product
 * is withdrawn entirely.
 */
@Entity(
    tableName = "order_lines",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["reference"],
            childColumns = ["orderReference"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("orderReference")],
)
data class OrderLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderReference: String,
    val productId: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val unitPriceMinor: Long,
    val currency: String,
    val ratingAverage: Double,
    val ratingCount: Int,
    val quantity: Int,
)
