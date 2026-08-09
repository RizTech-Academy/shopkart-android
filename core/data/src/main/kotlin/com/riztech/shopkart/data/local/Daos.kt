package com.riztech.shopkart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Embedded
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.riztech.shopkart.data.local.entity.CartItemEntity
import com.riztech.shopkart.data.local.entity.CategoryEntity
import com.riztech.shopkart.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/** A cart row joined to its product. */
data class CartLineView(
    @Embedded val product: ProductEntity,
    val quantity: Int,
)

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: String): ProductEntity?

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()

    /** Replaces the cached catalogue atomically so readers never see a partial list. */
    @Transaction
    suspend fun replaceAll(products: List<ProductEntity>) {
        clear()
        upsertAll(products)
    }
}

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(categories: List<CategoryEntity>) {
        clear()
        upsertAll(categories)
    }
}

@Dao
interface CartDao {

    /**
     * Cart lines joined to their products, ordered by when they were added.
     *
     * The join is why prices are never duplicated into the cart table: the
     * price rendered is always the current one.
     */
    @Query(
        """
        SELECT p.*, c.quantity AS quantity
        FROM cart_items c
        INNER JOIN products p ON p.id = c.productId
        ORDER BY c.addedAtEpochMillis ASC
        """,
    )
    fun observeCartLines(): Flow<List<CartLineView>>

    @Query("SELECT * FROM cart_items ORDER BY addedAtEpochMillis ASC")
    fun observeCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getItem(productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun setQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun remove(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
