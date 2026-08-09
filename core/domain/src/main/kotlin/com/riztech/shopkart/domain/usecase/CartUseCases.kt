package com.riztech.shopkart.domain.usecase

import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    operator fun invoke(): Flow<Cart> = repository.observeCart()
}

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    /** Adding an out-of-stock product is rejected here so no caller can bypass it. */
    suspend operator fun invoke(product: Product, quantity: Int = 1): AddToCartResult {
        if (!product.inStock) return AddToCartResult.OutOfStock
        if (quantity <= 0) return AddToCartResult.InvalidQuantity
        repository.addItem(product, quantity)
        return AddToCartResult.Added
    }
}

enum class AddToCartResult { Added, OutOfStock, InvalidQuantity }

/**
 * Setting a quantity to zero is a removal, not an error. Encoding that here
 * keeps every caller — stepper, swipe, deep link — behaving identically.
 */
class UpdateCartQuantityUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(productId: String, quantity: Int) {
        if (quantity <= 0) repository.removeItem(productId) else repository.setQuantity(productId, quantity)
    }
}

class RemoveFromCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(productId: String) = repository.removeItem(productId)
}
