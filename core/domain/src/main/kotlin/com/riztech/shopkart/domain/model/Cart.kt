package com.riztech.shopkart.domain.model

data class CartLine(
    val product: Product,
    val quantity: Int,
) {
    init {
        require(quantity > 0) { "A cart line must have a positive quantity, was $quantity" }
    }

    val lineTotal: Money get() = product.price * quantity
}

/**
 * The cart is a value object that derives its own totals.
 *
 * Keeping the arithmetic here rather than in a ViewModel means every screen
 * showing a subtotal necessarily agrees, and the rules can be tested without
 * Android on the classpath.
 */
data class Cart(
    val lines: List<CartLine> = emptyList(),
) {
    val isEmpty: Boolean get() = lines.isEmpty()

    val itemCount: Int get() = lines.sumOf { it.quantity }

    val subtotal: Money get() = lines.fold(Money.ZERO) { total, line -> total + line.lineTotal }

    fun quantityOf(productId: String): Int =
        lines.firstOrNull { it.product.id == productId }?.quantity ?: 0

    companion object {
        val EMPTY = Cart()
    }
}
