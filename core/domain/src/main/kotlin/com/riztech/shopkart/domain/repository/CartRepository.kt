package com.riztech.shopkart.domain.repository

import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.Order
import com.riztech.shopkart.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    /** Emits on every change, so any observing screen stays consistent. */
    fun observeCart(): Flow<Cart>

    suspend fun addItem(product: Product, quantity: Int = 1)
    suspend fun setQuantity(productId: String, quantity: Int)
    suspend fun removeItem(productId: String)
    suspend fun clear()
}

interface OrderRepository {
    suspend fun place(cart: Cart): Order
    fun observeOrders(): Flow<List<Order>>
}
