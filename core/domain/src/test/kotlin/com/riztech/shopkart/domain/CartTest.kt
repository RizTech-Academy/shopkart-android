package com.riztech.shopkart.domain

import com.google.common.truth.Truth.assertThat
import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.CartLine
import com.riztech.shopkart.domain.model.Money
import org.junit.Assert.assertThrows
import org.junit.Test

class CartTest {

    @Test
    fun `empty cart has zero subtotal and no items`() {
        assertThat(Cart.EMPTY.subtotal).isEqualTo(Money.ZERO)
        assertThat(Cart.EMPTY.itemCount).isEqualTo(0)
        assertThat(Cart.EMPTY.isEmpty).isTrue()
    }

    @Test
    fun `subtotal multiplies each line by its quantity`() {
        val cart = Cart(
            listOf(
                CartLine(product(id = "a", priceMinor = 1299), quantity = 2), // 2598
                CartLine(product(id = "b", priceMinor = 500), quantity = 3),  // 1500
            ),
        )

        assertThat(cart.subtotal).isEqualTo(Money.ofMinor(4098))
        assertThat(cart.itemCount).isEqualTo(5)
    }

    @Test
    fun `quantityOf returns zero for a product not in the cart`() {
        val cart = Cart(listOf(CartLine(product(id = "a"), quantity = 2)))
        assertThat(cart.quantityOf("a")).isEqualTo(2)
        assertThat(cart.quantityOf("missing")).isEqualTo(0)
    }

    @Test
    fun `a cart line cannot be created with zero quantity`() {
        assertThrows(IllegalArgumentException::class.java) {
            CartLine(product(id = "a"), quantity = 0)
        }
    }
}
