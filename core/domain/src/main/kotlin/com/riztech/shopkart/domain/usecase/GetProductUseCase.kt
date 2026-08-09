package com.riztech.shopkart.domain.usecase

import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.ProductRepository
import com.riztech.shopkart.domain.util.DataResult
import javax.inject.Inject

class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(id: String): DataResult<Product> = repository.getProduct(id)
}
