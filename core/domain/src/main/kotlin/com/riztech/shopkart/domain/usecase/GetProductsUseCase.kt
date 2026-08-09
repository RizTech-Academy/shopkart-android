package com.riztech.shopkart.domain.usecase

import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.ProductFilter
import com.riztech.shopkart.domain.repository.ProductRepository
import com.riztech.shopkart.domain.util.DataResult
import javax.inject.Inject

/**
 * One use case, one reason to change.
 *
 * These read as thin wrappers today, and that is the point: when a rule
 * arrives — hiding out-of-stock items, applying a promotion, merging a
 * recommendation feed — it lands here rather than in a ViewModel, and every
 * caller inherits it.
 */
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(filter: ProductFilter = ProductFilter()): DataResult<List<Product>> =
        repository.getProducts(filter)
}
