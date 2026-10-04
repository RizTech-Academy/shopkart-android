package com.riztech.shopkart.data

/** Canned API payloads, shaped exactly like the real ShopKart API. */
object ApiResponses {

    const val PRODUCTS = """
    {
      "data": [
        {
          "id": "p1",
          "title": "Noise-cancelling headphones",
          "description": "Over-ear, 30h battery",
          "category": "audio",
          "price": { "amountMinor": 24999, "currency": "USD" },
          "imageUrl": "https://example.test/p1.jpg",
          "rating": { "average": 4.6, "count": 128 },
          "inStock": true
        },
        {
          "id": "p2",
          "title": "Mechanical keyboard",
          "description": "Tactile switches",
          "category": "peripherals",
          "price": { "amountMinor": 10999, "currency": "USD" },
          "imageUrl": "https://example.test/p2.jpg",
          "rating": { "average": 4.2, "count": 64 },
          "inStock": false
        }
      ],
      "meta": { "page": 1, "pageSize": 20, "totalItems": 2, "totalPages": 1 }
    }
    """

    /** Carries a field the client has never heard of, on purpose. */
    const val PRODUCT_WITH_UNKNOWN_FIELD = """
    {
      "data": {
        "id": "p1",
        "title": "Noise-cancelling headphones",
        "description": "Over-ear, 30h battery",
        "category": "audio",
        "price": { "amountMinor": 24999, "currency": "USD" },
        "imageUrl": "https://example.test/p1.jpg",
        "rating": { "average": 4.6, "count": 128 },
        "inStock": true,
        "loyaltyPointsEarned": 250
      }
    }
    """

    const val CATEGORIES = """
    { "data": [ { "slug": "audio", "name": "Audio", "productCount": 12 } ] }
    """
}
