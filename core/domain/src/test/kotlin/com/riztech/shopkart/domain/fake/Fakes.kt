package com.riztech.shopkart.domain.fake

import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.CartLine
import com.riztech.shopkart.domain.model.Money
import com.riztech.shopkart.domain.model.Order
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.model.Rating
import com.riztech.shopkart.domain.repository.CartRepository
import com.riztech.shopkart.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hand-written fakes rather than a mocking framework.
 *
 * They are real implementations, so tests assert on observable behaviour
 * instead of on which methods were called — which keeps the tests from
 * breaking every time the implementation is refactored.
 */
class FakeCartRepository(initial: Cart = Cart.EMPTY) : CartRepository {
    private val state = MutableStateFlow(initial)
    val current: Cart get() = state.value
    var clearCount: Int = 0
        private set

    override fun observeCart(): Flow<Cart> = state.asStateFlow()

    override suspend fun addItem(product: Product, quantity: Int) {
        state.value = state.value.upsert(product, state.value.quantityOf(product.id) + quantity)
    }

    override suspend fun setQuantity(productId: String, quantity: Int) {
        val product = state.value.lines.firstOrNull { it.product.id == productId }?.product ?: return
        state.value = state.value.upsert(product, quantity)
    }

    override suspend fun removeItem(productId: String) {
        state.value = Cart(state.value.lines.filterNot { it.product.id == productId })
    }

    override suspend fun clear() {
        clearCount++
        state.value = Cart.EMPTY
    }

    private fun Cart.upsert(product: Product, quantity: Int): Cart {
        val without = lines.filterNot { it.product.id == product.id }
        return if (quantity <= 0) Cart(without) else Cart(without + CartLine(product, quantity))
    }
}

class FakeOrderRepository : OrderRepository {
    private val orders = MutableStateFlow<List<Order>>(emptyList())
    var placeCount: Int = 0
        private set

    override suspend fun place(cart: Cart): Order {
        placeCount++
        val order = Order(
            reference = "ORD-${placeCount.toString().padStart(4, '0')}",
            lines = cart.lines,
            total = cart.subtotal,
            placedAtEpochMillis = 0L,
        )
        orders.value = orders.value + order
        return order
    }

    override fun observeOrders(): Flow<List<Order>> = orders.asStateFlow()
}

fun product(
    id: String = "p-1",
    priceMinor: Long = 1000,
    inStock: Boolean = true,
): Product = Product(
    id = id,
    title = "Product $id",
    description = "Description",
    category = "audio",
    price = Money.ofMinor(priceMinor),
    imageUrl = "https://example.com/$id.png",
    rating = Rating(average = 4.5, count = 10),
    inStock = inStock,
)
