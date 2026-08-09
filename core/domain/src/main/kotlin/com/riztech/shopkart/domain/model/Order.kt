package com.riztech.shopkart.domain.model

/**
 * A placed order.
 *
 * Payment is intentionally out of scope for this sample, so an order captures
 * what was bought and what it came to, and stops there.
 */
data class Order(
    val reference: String,
    val lines: List<CartLine>,
    val total: Money,
    val placedAtEpochMillis: Long,
) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
}
