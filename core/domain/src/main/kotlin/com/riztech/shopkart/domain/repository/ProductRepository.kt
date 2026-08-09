package com.riztech.shopkart.domain.repository

import com.riztech.shopkart.domain.model.Category
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.util.DataResult

/** How products are sorted. Mirrors the values the catalogue API accepts. */
enum class ProductSort { Relevance, PriceAsc, PriceDesc, RatingDesc, TitleAsc }

data class ProductFilter(
    val search: String? = null,
    val category: String? = null,
    val sort: ProductSort = ProductSort.Relevance,
)

/**
 * Declared by the domain, implemented by the data layer.
 *
 * This is the dependency inversion that lets `:core:domain` know nothing about
 * Retrofit or Room, and lets use cases be tested against a hand-written fake.
 */
interface ProductRepository {
    suspend fun getProducts(filter: ProductFilter): DataResult<List<Product>>
    suspend fun getProduct(id: String): DataResult<Product>
    suspend fun getCategories(): DataResult<List<Category>>
}
