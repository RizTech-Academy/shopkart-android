package com.riztech.shopkart.domain.usecase

import com.riztech.shopkart.domain.model.Category
import com.riztech.shopkart.domain.repository.ProductRepository
import com.riztech.shopkart.domain.util.DataResult
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(): DataResult<List<Category>> = repository.getCategories()
}
