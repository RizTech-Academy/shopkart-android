package com.riztech.shopkart.data.di

import android.content.Context
import androidx.room.Room
import com.riztech.shopkart.data.local.CartDao
import com.riztech.shopkart.data.local.CategoryDao
import com.riztech.shopkart.data.local.OrderDao
import com.riztech.shopkart.data.local.ProductDao
import com.riztech.shopkart.data.local.ShopKartDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ShopKartDatabase =
        Room.databaseBuilder(context, ShopKartDatabase::class.java, "shopkart.db")
            // No fallbackToDestructiveMigration. Silently wiping a user's cart
            // on upgrade is a data-loss bug that only shows up in production;
            // a missing migration should fail loudly in development instead.
            .build()

    @Provides fun productDao(db: ShopKartDatabase): ProductDao = db.productDao()
    @Provides fun categoryDao(db: ShopKartDatabase): CategoryDao = db.categoryDao()
    @Provides fun cartDao(db: ShopKartDatabase): CartDao = db.cartDao()
    @Provides fun orderDao(db: ShopKartDatabase): OrderDao = db.orderDao()
}
