package com.riztech.shopkart.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.riztech.shopkart.data.local.ShopKartDatabase
import com.riztech.shopkart.data.remote.ShopKartApi
import com.riztech.shopkart.data.repository.ProductRepositoryImpl
import com.riztech.shopkart.domain.repository.ProductFilter
import com.riztech.shopkart.domain.repository.ProductSort
import com.riztech.shopkart.domain.util.DataError
import com.riztech.shopkart.domain.util.DataResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * The repository against a real HTTP server and a real database.
 *
 * MockWebServer rather than a mocked Retrofit interface, because the thing most
 * likely to break is the JSON contract, and a mocked interface cannot catch
 * that. Room in memory rather than a mocked DAO, for the same reason.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var db: ShopKartDatabase
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ShopKartApi::class.java)

        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShopKartDatabase::class.java,
        ).allowMainThreadQueries().build()

        repository = ProductRepositoryImpl(api, db.productDao(), db.categoryDao())
    }

    @After
    fun tearDown() {
        server.shutdown()
        db.close()
    }

    private fun respondWith(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun `maps the API response into domain products`() = runTest {
        respondWith(ApiResponses.PRODUCTS)

        val result = repository.getProducts(ProductFilter())

        assertThat(result).isInstanceOf(DataResult.Success::class.java)
        val products = (result as DataResult.Success).data
        assertThat(products).hasSize(2)
        assertThat(products[0].title).isEqualTo("Noise-cancelling headphones")
        assertThat(products[0].price.amountMinor).isEqualTo(24999)
        assertThat(products[1].inStock).isFalse()
    }

    @Test
    fun `an unknown field from a newer server does not break the client`() = runTest {
        respondWith(ApiResponses.PRODUCT_WITH_UNKNOWN_FIELD)

        val result = repository.getProduct("p1")

        assertThat(result).isInstanceOf(DataResult.Success::class.java)
        assertThat((result as DataResult.Success).data.id).isEqualTo("p1")
    }

    @Test
    fun `an unfiltered fetch is cached and served when the network is gone`() = runTest {
        respondWith(ApiResponses.PRODUCTS)
        repository.getProducts(ProductFilter())

        server.shutdown() // the network disappears

        val result = repository.getProducts(ProductFilter())

        assertThat(result).isInstanceOf(DataResult.Success::class.java)
        assertThat((result as DataResult.Success).data).hasSize(2)
    }

    @Test
    fun `with no network and an empty cache the failure is reported as Network`() = runTest {
        server.shutdown()

        val result = repository.getProducts(ProductFilter())

        assertThat(result).isInstanceOf(DataResult.Failure::class.java)
        assertThat((result as DataResult.Failure).error).isEqualTo(DataError.Network)
    }

    @Test
    fun `a 404 is reported as NotFound, not as a generic server error`() = runTest {
        respondWith("", code = 404)

        val result = repository.getProduct("missing")

        assertThat(result).isInstanceOf(DataResult.Failure::class.java)
        assertThat((result as DataResult.Failure).error).isEqualTo(DataError.NotFound)
    }

    @Test
    fun `a 500 is reported as Server`() = runTest {
        respondWith("", code = 500)

        val result = repository.getProducts(ProductFilter())

        assertThat(result).isInstanceOf(DataResult.Failure::class.java)
        assertThat((result as DataResult.Failure).error).isEqualTo(DataError.Server)
    }

    @Test
    fun `offline results are still filtered and sorted locally`() = runTest {
        respondWith(ApiResponses.PRODUCTS)
        repository.getProducts(ProductFilter())
        server.shutdown()

        val cheapestFirst = repository.getProducts(ProductFilter(sort = ProductSort.PriceAsc))
        val titles = (cheapestFirst as DataResult.Success).data.map { it.title }
        assertThat(titles).containsExactly("Mechanical keyboard", "Noise-cancelling headphones")
            .inOrder()

        val searched = repository.getProducts(ProductFilter(search = "keyboard"))
        assertThat((searched as DataResult.Success).data.map { it.id }).containsExactly("p2")
    }

    @Test
    fun `a search is not allowed to overwrite the cached catalogue`() = runTest {
        respondWith(ApiResponses.PRODUCTS)
        repository.getProducts(ProductFilter())

        // A filtered response containing a single product must not become the
        // whole offline catalogue.
        respondWith("""{"data":[{"id":"p2","title":"Mechanical keyboard","description":"Tactile switches","category":"peripherals","price":{"amountMinor":10999,"currency":"USD"},"imageUrl":"https://example.test/p2.jpg","rating":{"average":4.2,"count":64},"inStock":false}]}""")
        repository.getProducts(ProductFilter(search = "keyboard"))

        server.shutdown()
        val offline = repository.getProducts(ProductFilter())

        assertThat((offline as DataResult.Success).data).hasSize(2)
    }
}
