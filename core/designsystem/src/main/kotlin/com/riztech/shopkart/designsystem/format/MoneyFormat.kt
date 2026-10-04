package com.riztech.shopkart.designsystem.format

import com.riztech.shopkart.domain.model.Money
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formats minor units for display.
 *
 * Deliberately the only place money becomes a string. Scattering
 * "$${amount / 100.0}" through the UI is how two screens end up disagreeing
 * about rounding, and it ignores locales that do not put the symbol first or
 * use two decimal places.
 */
fun Money.format(locale: Locale = Locale.getDefault(), currencyCode: String = "USD"): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }
        .getOrElse { Currency.getInstance("USD") }

    val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currency
        minimumFractionDigits = currency.defaultFractionDigits
        maximumFractionDigits = currency.defaultFractionDigits
    }

    val divisor = Math.pow(10.0, currency.defaultFractionDigits.toDouble())
    return formatter.format(amountMinor / divisor)
}
