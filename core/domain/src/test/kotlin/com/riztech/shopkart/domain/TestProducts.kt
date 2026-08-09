package com.riztech.shopkart.domain

import com.riztech.shopkart.domain.model.Product
import com.riztech.shopkart.domain.fake.product as fakeProduct

internal fun product(
    id: String = "p-1",
    priceMinor: Long = 1000,
    inStock: Boolean = true,
): Product = fakeProduct(id = id, priceMinor = priceMinor, inStock = inStock)
