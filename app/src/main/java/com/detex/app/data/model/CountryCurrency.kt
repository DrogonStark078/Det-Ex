package com.detex.app.data.model

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class CountryCurrency(
    val countryCode: String,
    val countryName: String,
    val dialCode: String,
    val flagEmoji: String,
    val currencyCode: String,
    val currencySymbol: String,
    val locale: Locale,
    val phoneLength: Int = 10
) {
    fun formatAmount(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(locale)
            format.currency = Currency.getInstance(currencyCode)
            format.format(amount)
        } catch (_: Exception) {
            "$currencySymbol${String.format(Locale.US, "%,.2f", amount)}"
        }
    }
}

object SupportedCountries {
    val list = listOf(
        CountryCurrency("IN", "India", "+91", "🇮🇳", "INR", "₹", Locale("en", "IN"), 10),
        CountryCurrency("US", "United States", "+1", "🇺🇸", "USD", "$", Locale.US, 10),
        CountryCurrency("GB", "United Kingdom", "+44", "🇬🇧", "GBP", "£", Locale.UK, 10),
        CountryCurrency("AE", "United Arab Emirates", "+971", "🇦🇪", "AED", "AED", Locale("ar", "AE"), 9),
        CountryCurrency("SG", "Singapore", "+65", "🇸🇬", "SGD", "S$", Locale("en", "SG"), 8),
        CountryCurrency("CA", "Canada", "+1", "🇨🇦", "CAD", "CA$", Locale.CANADA, 10),
        CountryCurrency("AU", "Australia", "+61", "🇦🇺", "AUD", "A$", Locale("en", "AU"), 9),
        CountryCurrency("DE", "Germany", "+49", "🇩🇪", "EUR", "€", Locale.GERMANY, 11),
        CountryCurrency("FR", "France", "+33", "🇫🇷", "EUR", "€", Locale.FRANCE, 9),
        CountryCurrency("JP", "Japan", "+81", "🇯🇵", "JPY", "¥", Locale.JAPAN, 10),
        CountryCurrency("SA", "Saudi Arabia", "+966", "🇸🇦", "SAR", "SAR", Locale("ar", "SA"), 9)
    )

    val DEFAULT = list.first()
}
