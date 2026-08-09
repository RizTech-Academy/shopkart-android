package com.riztech.shopkart.domain.usecase

import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.Order
import com.riztech.shopkart.domain.repository.CartRepository
import com.riztech.shopkart.domain.repository.OrderRepository
import javax.inject.Inject

sealed interface PlaceOrderResult {
    data class Placed(val order: Order) : PlaceOrderResult
    data object CartEmpty : PlaceOrderResult
}

/**
 * Places an order and empties the cart.
 *
 * The two steps belong together — an order that leaves the cart populated
 * would let a customer buy twice — so they are expressed as one operation
 * rather than two calls a screen has to remember to make in order.
 */
class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(cart: Cart): PlaceOrderResult {
        if (cart.isEmpty) return PlaceOrderResult.CartEmpty
        val order = orderRepository.place(cart)
        cartRepository.clear()
        return PlaceOrderResult.Placed(order)
    }
}
