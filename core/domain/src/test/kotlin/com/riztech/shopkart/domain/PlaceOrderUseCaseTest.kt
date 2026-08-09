package com.riztech.shopkart.domain

import com.google.common.truth.Truth.assertThat
import com.riztech.shopkart.domain.fake.FakeCartRepository
import com.riztech.shopkart.domain.fake.FakeOrderRepository
import com.riztech.shopkart.domain.fake.product
import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.CartLine
import com.riztech.shopkart.domain.usecase.PlaceOrderResult
import com.riztech.shopkart.domain.usecase.PlaceOrderUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlaceOrderUseCaseTest {

    private val cartRepository = FakeCartRepository()
    private val orderRepository = FakeOrderRepository()
    private val placeOrder = PlaceOrderUseCase(orderRepository, cartRepository)

    private val cart = Cart(
        listOf(
            CartLine(product(id = "a", priceMinor = 1299), quantity = 2),
            CartLine(product(id = "b", priceMinor = 500), quantity = 1),
        ),
    )

    @Test
    fun `placing an order captures the lines and total`() = runTest {
        val result = placeOrder(cart)

        assertThat(result).isInstanceOf(PlaceOrderResult.Placed::class.java)
        val order = (result as PlaceOrderResult.Placed).order
        assertThat(order.total.amountMinor).isEqualTo(3098)
        assertThat(order.itemCount).isEqualTo(3)
        assertThat(order.reference).isNotEmpty()
    }

    @Test
    fun `placing an order empties the cart so it cannot be bought twice`() = runTest {
        placeOrder(cart)

        assertThat(cartRepository.clearCount).isEqualTo(1)
        assertThat(cartRepository.current.isEmpty).isTrue()
    }

    @Test
    fun `an empty cart is rejected and no order is created`() = runTest {
        val result = placeOrder(Cart.EMPTY)

        assertThat(result).isEqualTo(PlaceOrderResult.CartEmpty)
        assertThat(orderRepository.placeCount).isEqualTo(0)
        assertThat(cartRepository.clearCount).isEqualTo(0)
    }
}
