package com.riztech.shopkart.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.riztech.shopkart.domain.fake.FakeCartRepository
import com.riztech.shopkart.domain.fake.product
import com.riztech.shopkart.domain.usecase.AddToCartResult
import com.riztech.shopkart.domain.usecase.AddToCartUseCase
import com.riztech.shopkart.domain.usecase.ObserveCartUseCase
import com.riztech.shopkart.domain.usecase.RemoveFromCartUseCase
import com.riztech.shopkart.domain.usecase.UpdateCartQuantityUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CartUseCaseTest {

    private val repository = FakeCartRepository()

    @Test
    fun `adding an in-stock product puts it in the cart`() = runTest {
        val result = AddToCartUseCase(repository)(product(id = "a"))

        assertThat(result).isEqualTo(AddToCartResult.Added)
        assertThat(repository.current.quantityOf("a")).isEqualTo(1)
    }

    @Test
    fun `adding the same product again increments rather than duplicating`() = runTest {
        val addToCart = AddToCartUseCase(repository)
        val item = product(id = "a")

        addToCart(item)
        addToCart(item, quantity = 2)

        assertThat(repository.current.lines).hasSize(1)
        assertThat(repository.current.quantityOf("a")).isEqualTo(3)
    }

    @Test
    fun `an out-of-stock product is refused and the cart is untouched`() = runTest {
        val result = AddToCartUseCase(repository)(product(id = "a", inStock = false))

        assertThat(result).isEqualTo(AddToCartResult.OutOfStock)
        assertThat(repository.current.isEmpty).isTrue()
    }

    @Test
    fun `a non-positive quantity is refused`() = runTest {
        val result = AddToCartUseCase(repository)(product(id = "a"), quantity = 0)

        assertThat(result).isEqualTo(AddToCartResult.InvalidQuantity)
        assertThat(repository.current.isEmpty).isTrue()
    }

    @Test
    fun `setting a quantity to zero removes the line`() = runTest {
        AddToCartUseCase(repository)(product(id = "a"), quantity = 3)

        UpdateCartQuantityUseCase(repository)("a", 0)

        assertThat(repository.current.isEmpty).isTrue()
    }

    @Test
    fun `setting a negative quantity also removes rather than throwing`() = runTest {
        AddToCartUseCase(repository)(product(id = "a"), quantity = 3)

        UpdateCartQuantityUseCase(repository)("a", -5)

        assertThat(repository.current.isEmpty).isTrue()
    }

    @Test
    fun `removing a product that is not present is a no-op`() = runTest {
        AddToCartUseCase(repository)(product(id = "a"))

        RemoveFromCartUseCase(repository)("does-not-exist")

        assertThat(repository.current.quantityOf("a")).isEqualTo(1)
    }

    @Test
    fun `observers see the cart change`() = runTest {
        ObserveCartUseCase(repository)().test {
            assertThat(awaitItem().isEmpty).isTrue()

            AddToCartUseCase(repository)(product(id = "a", priceMinor = 250), quantity = 2)

            val updated = awaitItem()
            assertThat(updated.itemCount).isEqualTo(2)
            assertThat(updated.subtotal.amountMinor).isEqualTo(500)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
