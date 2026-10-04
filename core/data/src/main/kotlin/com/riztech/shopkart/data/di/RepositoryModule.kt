package com.riztech.shopkart.data.di

import com.riztech.shopkart.data.repository.CartRepositoryImpl
import com.riztech.shopkart.data.repository.OrderRepositoryImpl
import com.riztech.shopkart.data.repository.ProductRepositoryImpl
import com.riztech.shopkart.domain.repository.CartRepository
import com.riztech.shopkart.domain.repository.OrderRepository
import com.riztech.shopkart.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the domain's interfaces to their data-layer implementations.
 *
 * Everything above this line depends on the interface only, which is what
 * keeps the dependency rule pointing inwards.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun productRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun cartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds @Singleton
    abstract fun orderRepository(impl: OrderRepositoryImpl): OrderRepository
}
