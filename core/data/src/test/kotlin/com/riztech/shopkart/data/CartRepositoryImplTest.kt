package com.riztech.shopkart.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.riztech.shopkart.data.local.ShopKartDatabase
import com.riztech.shopkart.data.repository.CartRepositoryImpl
import com.riztech.shopkart.data.repository.OrderRepositoryImpl
import com.riztech.shopkart.domain.model.Money
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.model.Rating
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CartRepositoryImplTest {

    private lateinit var db: ShopKartDatabase
    private lateinit var cart: CartRepositoryImpl
    private lateinit var orders: OrderRepositoryImpl

    private fun product(id: String, priceMinor: Long, title: String = "Thing") = Product(
        id = id,
        title = title,
        description = "A thing",
        category = "misc",
        price = Money.ofMinor(priceMinor),
        imageUrl = "https://example.test/$id.jpg",
        rating = Rating(4.0, 10),
        inStock = true,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopKartDatabase::class.java,
        ).allowMainThreadQueries().build()
        cart = CartRepositoryImpl(db, db.cartDao(), db.productDao())
        orders = OrderRepositoryImpl(db.orderDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `adding the same product twice increases quantity rather than duplicating the line`() =
        runTest {
            val p = product("p1", 1000)
            cart.addItem(p, 1)
            cart.addItem(p, 2)

            cart.observeCart().test {
                val current = awaitItem()
                assertThat(current.lines).hasSize(1)
                assertThat(current.lines.first().quantity).isEqualTo(3)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setting a quantity of zero removes the line`() = runTest {
        cart.addItem(product("p1", 1000), 2)
        cart.setQuantity("p1", 0)

        cart.observeCart().test {
            assertThat(awaitItem().isEmpty).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the cart subtotal is exact across many small amounts`() = runTest {
        // Ten 10-cent items. As a Double this sums to 0.9999999999999999.
        cart.addItem(product("p1", 10), 10)

        cart.observeCart().test {
            assertThat(awaitItem().subtotal).isEqualTo(Money.ofMinor(100))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observers see a change made elsewhere without re-reading`() = runTest {
        cart.observeCart().test {
            assertThat(awaitItem().isEmpty).isTrue()

            cart.addItem(product("p1", 500), 1)
            assertThat(awaitItem().itemCount).isEqualTo(1)

            cart.addItem(product("p2", 250), 2)
            assertThat(awaitItem().itemCount).isEqualTo(3)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a placed order keeps the price it was bought at`() = runTest {
        cart.addItem(product("p1", 1999, "Headphones"), 2)

        val snapshot = run {
            var captured: com.riztech.shopkart.domain.model.Cart? = null
            cart.observeCart().test {
                captured = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            requireNotNull(captured)
        }

        val order = orders.place(snapshot)

        assertThat(order.reference).startsWith("SK-")
        assertThat(order.total).isEqualTo(Money.ofMinor(3998))
        assertThat(order.lines.single().product.title).isEqualTo("Headphones")
    }

    @Test
    fun `placing an order for an empty cart is refused`() = runTest {
        try {
            orders.place(com.riztech.shopkart.domain.model.Cart.EMPTY)
            throw AssertionError("expected an empty cart to be refused")
        } catch (expected: IllegalArgumentException) {
            assertThat(expected).hasMessageThat().contains("empty cart")
        }
    }
}
