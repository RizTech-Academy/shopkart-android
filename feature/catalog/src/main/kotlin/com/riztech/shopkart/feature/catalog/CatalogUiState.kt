package com.riztech.shopkart.feature.catalog

import com.riztech.shopkart.domain.model.Category
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.ProductSort

/**
 * Everything the catalogue screen needs, in one value.
 *
 * A single state object rather than several independent flags. Separate
 * `isLoading` / `error` / `items` booleans permit nonsense combinations -
 * loading and errored at once - and the compiler cannot stop you rendering
 * them.
 */
data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val search: String = "",
    val selectedCategory: String? = null,
    val sort: ProductSort = ProductSort.Relevance,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val cartItemCount: Int = 0,
) {
    /** True only when a finished, successful load produced nothing. */
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && products.isEmpty()

    val hasActiveFilter: Boolean get() = search.isNotBlank() || selectedCategory != null

    /** The best-reviewed products in stock, for the carousel on the unfiltered home screen. */
    val topRated: List<Product>
        get() = if (hasActiveFilter) {
            emptyList()
        } else {
            products.filter { it.inStock && it.rating.count >= 100 }
                .sortedByDescending { it.rating.average }
                .take(6)
        }
}
