package com.riztech.shopkart.data.repository

import com.riztech.shopkart.data.local.OrderDao
import com.riztech.shopkart.data.local.entity.OrderEntity
import com.riztech.shopkart.data.mapper.toDomain
import com.riztech.shopkart.data.mapper.toOrderLineEntity
import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.Order
import com.riztech.shopkart.domain.repository.OrderRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
) : OrderRepository {

    override suspend fun place(cart: Cart): Order {
        require(!cart.isEmpty) { "Cannot place an order for an empty cart" }

        val reference = "SK-" + UUID.randomUUID().toString().take(8).uppercase()
        val placedAt = System.currentTimeMillis()

        orderDao.insert(
            order = OrderEntity(
                reference = reference,
                totalMinor = cart.subtotal.amountMinor,
                currency = CURRENCY,
                placedAtEpochMillis = placedAt,
            ),
            lines = cart.lines.map { it.toOrderLineEntity(reference, CURRENCY) },
        )

        return Order(
            reference = reference,
            lines = cart.lines,
            total = cart.subtotal,
            placedAtEpochMillis = placedAt,
        )
    }

    override fun observeOrders(): Flow<List<Order>> =
        orderDao.observeOrders().map { orders -> orders.map { it.toDomain() } }

    private companion object {
        const val CURRENCY = "USD"
    }
}
