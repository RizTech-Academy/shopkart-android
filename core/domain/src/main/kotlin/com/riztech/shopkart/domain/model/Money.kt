package com.riztech.shopkart.domain.model

/**
 * An amount of money in integer minor units.
 *
 * Money is never a [Double]. Binary floating point cannot represent 0.10
 * exactly, so repeated addition drifts — a cart of ten $0.10 items should be
 * $1.00, not $0.9999999999999999. Storing cents as a [Long] makes arithmetic
 * exact and overflow implausible.
 */
@JvmInline
value class Money private constructor(val amountMinor: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(amountMinor + other.amountMinor)

    operator fun times(quantity: Int): Money {
        require(quantity >= 0) { "Quantity cannot be negative, was $quantity" }
        return Money(amountMinor * quantity)
    }

    override fun compareTo(other: Money): Int = amountMinor.compareTo(other.amountMinor)

    companion object {
        val ZERO: Money = Money(0)

        fun ofMinor(amountMinor: Long): Money {
            require(amountMinor >= 0) { "Money cannot be negative, was $amountMinor" }
            return Money(amountMinor)
        }
    }
}
