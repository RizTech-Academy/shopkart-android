package com.riztech.shopkart.data.repository

import androidx.room.withTransaction
import com.riztech.shopkart.data.local.CartDao
import com.riztech.shopkart.data.local.ShopKartDatabase
import com.riztech.shopkart.data.local.ProductDao
import com.riztech.shopkart.data.local.entity.CartItemEntity
import com.riztech.shopkart.data.mapper.toDomain
import com.riztech.shopkart.data.mapper.toEntity
import com.riztech.shopkart.domain.model.Cart
import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.repository.CartRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The cart lives only on the device.
 *
 * It is a Flow all the way from Room, so a change made on the detail screen is
 * already visible on the cart badge before the user navigates - no manual
 * refresh and no chance of two screens disagreeing.
 */
@Singleton
class CartRepositoryImpl @Inject constructor(
    private val db: ShopKartDatabase,
    private val cartDao: CartDao,
    private val productDao: ProductDao,
) : CartRepository {

    override fun observeCart(): Flow<Cart> =
        cartDao.observeCartLines().map { lines -> Cart(lines.map { it.toDomain() }) }

    /**
     * One transaction, deliberately.
     *
     * This writes twice - the product row, then the cart row - and the cart
     * query joins the two. Without a transaction Room invalidates after the
     * first write and emits an intermediate cart that does not yet contain the
     * item just added, so every observer briefly renders a stale basket. Room
     * fires invalidation once per transaction, so one logical change becomes
     * exactly one emission.
     */
    override suspend fun addItem(product: Product, quantity: Int) {
        require(quantity > 0) { "Quantity must be positive, was $quantity" }

        db.withTransaction {
            // The cart joins to the product table, so a product that was never
            // cached would produce a line that silently vanishes on read.
            // Writing it here makes the join total.
            productDao.upsertAll(listOf(product.toEntityForCart()))

            val existing = cartDao.getItem(product.id)
            if (existing == null) {
                cartDao.upsert(
                    CartItemEntity(
                        productId = product.id,
                        quantity = quantity,
                        addedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            } else {
                // Adding again increases the quantity rather than resetting it,
                // and keeps the original position in the cart.
                cartDao.setQuantity(product.id, existing.quantity + quantity)
            }
        }
    }

    override suspend fun setQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) cartDao.remove(productId) else cartDao.setQuantity(productId, quantity)
    }

    override suspend fun removeItem(productId: String) = cartDao.remove(productId)

    override suspend fun clear() = cartDao.clear()
}

private fun Product.toEntityForCart() = com.riztech.shopkart.data.local.entity.ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    priceMinor = price.amountMinor,
    currency = "USD",
    imageUrl = imageUrl,
    ratingAverage = rating.average,
    ratingCount = rating.count,
    inStock = inStock,
)
