package com.riztech.shopkart.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Wire types.
 *
 * Kept separate from the domain models on purpose: the API is free to add,
 * rename or nest fields without that change rippling into business logic or
 * the UI. The mapper is the only place that has to care.
 */
@Serializable
data class ApiEnvelope<T>(
    val data: T,
    val meta: PageMetaDto? = null,
)

@Serializable
data class PageMetaDto(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
)

@Serializable
data class MoneyDto(
    val amountMinor: Long,
    val currency: String,
)

@Serializable
data class RatingDto(
    val average: Double,
    val count: Int,
)

@Serializable
data class ProductDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val price: MoneyDto,
    val imageUrl: String,
    val rating: RatingDto,
    val inStock: Boolean,
)

@Serializable
data class CategoryDto(
    val slug: String,
    val name: String,
    val productCount: Int,
)
