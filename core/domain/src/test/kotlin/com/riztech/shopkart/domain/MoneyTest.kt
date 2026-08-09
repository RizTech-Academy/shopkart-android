package com.riztech.shopkart.domain

import com.google.common.truth.Truth.assertThat
import com.riztech.shopkart.domain.model.Money
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {

    @Test
    fun `repeated addition of ten cents is exact`() {
        // The reason Money exists: as a Double this sums to 0.9999999999999999.
        val total = (1..10).fold(Money.ZERO) { acc, _ -> acc + Money.ofMinor(10) }
        assertThat(total).isEqualTo(Money.ofMinor(100))
    }

    @Test
    fun `multiplication scales by quantity`() {
        assertThat(Money.ofMinor(1299) * 3).isEqualTo(Money.ofMinor(3897))
    }

    @Test
    fun `multiplying by zero yields zero`() {
        assertThat(Money.ofMinor(1299) * 0).isEqualTo(Money.ZERO)
    }

    @Test
    fun `negative amounts are rejected at construction`() {
        assertThrows(IllegalArgumentException::class.java) { Money.ofMinor(-1) }
    }

    @Test
    fun `negative quantities are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { Money.ofMinor(100) * -1 }
    }

    @Test
    fun `money is ordered by amount`() {
        assertThat(Money.ofMinor(500)).isGreaterThan(Money.ofMinor(499))
    }
}
