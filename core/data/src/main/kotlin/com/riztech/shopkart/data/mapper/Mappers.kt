package com.riztech.shopkart.data.mapper

import com.riztech.shopkart.data.local.CartLineView
import com.riztech.shopkart.data.local.OrderWithLines
import com.riztech.shopkart.data.local.entity.CategoryEntity
import com.riztech.shopkart.data.local.entity.OrderEntity
import com.riztech.shopkart.data.local.entity.OrderLineEntity
import com.riztech.shopkart.data.local.entity.ProductEntity
import com.riztech.shopkart.data.remote.dto.CategoryDto
import com.riztech.shopkart.data.remote.dto.ProductDto
import com.riztech.shopkart.domain.model.*

/**
 * The only place that knows both shapes.
 *
 * Wire types, database rows and domain models are deliberately three separate
 * things. The API can rename a field, or the schema can denormalise, without
 * either change reaching the use cases or the UI - the edit lands here.
 */

fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    priceMinor = price.amountMinor,
    currency = price.currency,
    imageUrl = imageUrl,
    ratingAverage = rating.average,
    ratingCount = rating.count,
    inStock = inStock,
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    price = Money.ofMinor(priceMinor),
    imageUrl = imageUrl,
    rating = Rating(average = ratingAverage, count = ratingCount),
    inStock = inStock,
)

fun ProductDto.toDomain(): Product = toEntity().toDomain()

fun CategoryDto.toEntity(): CategoryEntity =
    CategoryEntity(slug = slug, name = name, productCount = productCount)

fun CategoryEntity.toDomain(): Category =
    Category(slug = slug, name = name, productCount = productCount)

fun CartLineView.toDomain(): CartLine =
    CartLine(product = product.toDomain(), quantity = quantity)

fun OrderWithLines.toDomain(): Order = Order(
    reference = order.reference,
    lines = lines.map { it.toDomain() },
    total = Money.ofMinor(order.totalMinor),
    placedAtEpochMillis = order.placedAtEpochMillis,
)

fun OrderLineEntity.toDomain(): CartLine = CartLine(
    product = Product(
        id = productId,
        title = title,
        description = description,
        category = category,
        price = Money.ofMinor(unitPriceMinor),
        imageUrl = imageUrl,
        rating = Rating(average = ratingAverage, count = ratingCount),
        // An ordered product is a historical record; stock at the time is not
        // meaningful and must never drive UI on an order screen.
        inStock = true,
    ),
    quantity = quantity,
)

fun CartLine.toOrderLineEntity(orderReference: String, currency: String): OrderLineEntity =
    OrderLineEntity(
        orderReference = orderReference,
        productId = product.id,
        title = product.title,
        description = product.description,
        category = product.category,
        imageUrl = product.imageUrl,
        unitPriceMinor = product.price.amountMinor,
        currency = currency,
        ratingAverage = product.rating.average,
        ratingCount = product.rating.count,
        quantity = quantity,
    )

