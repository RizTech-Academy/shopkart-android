package com.riztech.shopkart.data.repository

import com.riztech.shopkart.data.local.CategoryDao
import com.riztech.shopkart.data.local.ProductDao
import com.riztech.shopkart.data.mapper.toDomain
import com.riztech.shopkart.data.mapper.toEntity
import com.riztech.shopkart.data.remote.ShopKartApi
import com.riztech.shopkart.domain.model.Category
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.ProductFilter
import com.riztech.shopkart.domain.repository.ProductRepository
import com.riztech.shopkart.domain.repository.ProductSort
import com.riztech.shopkart.domain.util.DataError
import com.riztech.shopkart.domain.util.DataResult
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

/**
 * Network first, cache as a fallback.
 *
 * The cache exists so the app still shows something on a train, not to save a
 * request. That distinction matters: a stale catalogue beats an empty screen,
 * but a successful fetch always wins.
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val api: ShopKartApi,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
) : ProductRepository {

    override suspend fun getProducts(filter: ProductFilter): DataResult<List<Product>> =
        try {
            val response = api.getProducts(
                search = filter.search?.takeIf { it.isNotBlank() },
                category = filter.category,
                sort = filter.sort.toApiValue(),
            )
            val entities = response.data.map { it.toEntity() }

            // Only replace the cache on an unfiltered fetch. Caching the results
            // of a search would leave the user offline holding whatever they
            // last searched for and nothing else.
            if (filter.isUnfiltered) productDao.replaceAll(entities)

            DataResult.Success(entities.map { it.toDomain() })
        } catch (e: IOException) {
            val cached = productDao.getAll().map { it.toDomain() }.applyLocally(filter)
            if (cached.isNotEmpty()) DataResult.Success(cached)
            else DataResult.Failure(DataError.Network)
        } catch (e: HttpException) {
            DataResult.Failure(if (e.code() == 404) DataError.NotFound else DataError.Server)
        } catch (e: Exception) {
            DataResult.Failure(DataError.Unknown)
        }

    override suspend fun getProduct(id: String): DataResult<Product> =
        try {
            DataResult.Success(api.getProduct(id).data.toEntity().toDomain())
        } catch (e: IOException) {
            productDao.getById(id)?.let { DataResult.Success(it.toDomain()) }
                ?: DataResult.Failure(DataError.Network)
        } catch (e: HttpException) {
            DataResult.Failure(if (e.code() == 404) DataError.NotFound else DataError.Server)
        } catch (e: Exception) {
            DataResult.Failure(DataError.Unknown)
        }

    override suspend fun getCategories(): DataResult<List<Category>> =
        try {
            val entities = api.getCategories().data.map { it.toEntity() }
            categoryDao.replaceAll(entities)
            DataResult.Success(entities.map { it.toDomain() })
        } catch (e: IOException) {
            val cached = categoryDao.getAll().map { it.toDomain() }
            if (cached.isNotEmpty()) DataResult.Success(cached)
            else DataResult.Failure(DataError.Network)
        } catch (e: HttpException) {
            DataResult.Failure(DataError.Server)
        } catch (e: Exception) {
            DataResult.Failure(DataError.Unknown)
        }

    /**
     * Filtering and sorting applied to cached rows.
     *
     * The server is the authority on ordering; this only runs when the server
     * could not be reached, so an offline user still gets a sensible list
     * rather than the raw insertion order.
     */
    private fun List<Product>.applyLocally(filter: ProductFilter): List<Product> {
        val search = filter.search?.trim()?.lowercase()
        return asSequence()
            .filter { filter.category == null || it.category == filter.category }
            .filter {
                search.isNullOrEmpty() ||
                    it.title.lowercase().contains(search) ||
                    it.description.lowercase().contains(search)
            }
            .sortedWith(filter.sort.comparator())
            .toList()
    }

    private fun ProductSort.comparator(): Comparator<Product> = when (this) {
        ProductSort.PriceAsc -> compareBy { it.price.amountMinor }
        ProductSort.PriceDesc -> compareByDescending { it.price.amountMinor }
        ProductSort.RatingDesc -> compareByDescending { it.rating.average }
        ProductSort.TitleAsc -> compareBy { it.title.lowercase() }
        ProductSort.Relevance -> compareBy { it.title.lowercase() }
    }

    private val ProductFilter.isUnfiltered: Boolean
        get() = search.isNullOrBlank() && category == null

    private fun ProductSort.toApiValue(): String? = when (this) {
        ProductSort.Relevance -> null
        ProductSort.PriceAsc -> "price_asc"
        ProductSort.PriceDesc -> "price_desc"
        ProductSort.RatingDesc -> "rating_desc"
        ProductSort.TitleAsc -> "title_asc"
    }
}
