package com.riztech.shopkart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.riztech.shopkart.data.local.entity.CartItemEntity
import com.riztech.shopkart.data.local.entity.CategoryEntity
import com.riztech.shopkart.data.local.entity.OrderEntity
import com.riztech.shopkart.data.local.entity.OrderLineEntity
import com.riztech.shopkart.data.local.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderLineEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ShopKartDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
}
