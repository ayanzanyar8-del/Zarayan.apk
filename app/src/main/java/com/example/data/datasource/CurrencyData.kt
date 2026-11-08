package com.example.data.datasource

import com.example.data.model.CurrencyRate

data class GoldRateItem(
    val karat: String,
    val nameKurdish: String,
    val priceIqd: String,
    val priceNumeric: Double,
    val change: String,
    val isPositive: Boolean
)

data class CommodityItem(
    val id: String,
    val nameKurdish: String,
    val priceUsd: String,
    val change: String,
    val isPositive: Boolean,
    val unit: String
)

data class CryptoRateItem(
    val symbol: String,
    val nameKurdish: String,
    val priceUsd: String,
    val changePercent: String,
    val isPositive: Boolean
)

object CurrencyData {

    const val LIVE_INDEX_URL = "https://rudawindex.net/"

    val defaultRates: List<CurrencyRate> = listOf(
        CurrencyRate(
            code = "USD",
            nameKurdish = "١٠٠ دۆلاری ئەمریکی (سلێمانی / هەولێر)",
            buyPrice = "١٥٠,٢٥٠",
            sellPrice = "١٥٠,٧٥٠",
            unit = "دیناری عێراقی",
            change = "+٢٥٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "EUR",
            nameKurdish = "١٠٠ یۆرۆی ئەوروپی",
            buyPrice = "١٦٢,٥٠٠",
            sellPrice = "١٦٣,٥٠٠",
            unit = "دیناری عێراقی",
            change = "-٥٠٠",
            isPositive = false
        ),
        CurrencyRate(
            code = "GBP",
            nameKurdish = "١٠٠ پاوەندی بەریتانی",
            buyPrice = "١٩١,٠٠٠",
            sellPrice = "١٩٢,٢٥٠",
            unit = "دیناری عێراقی",
            change = "+٧٥٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "CAD",
            nameKurdish = "١٠٠ دۆلاری کەنەدی",
            buyPrice = "١١٠,٢٥٠",
            sellPrice = "١١١,٠٠٠",
            unit = "دیناری عێراقی",
            change = "+١٥٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "AUD",
            nameKurdish = "١٠٠ دۆلاری ئوستوڕالی",
            buyPrice = "٩٨,٥٠٠",
            sellPrice = "٩٩,٢٥٠",
            unit = "دیناری عێراقی",
            change = "-٢٠٠",
            isPositive = false
        ),
        CurrencyRate(
            code = "TRY",
            nameKurdish = "١٠٠٠ لیرەی تورکی",
            buyPrice = "٤,٣٥٠",
            sellPrice = "٤,٥٠٠",
            unit = "دیناری عێراقی",
            change = "-٥٠",
            isPositive = false
        ),
        CurrencyRate(
            code = "IRR",
            nameKurdish = "١ ملیۆن تمەنی ئێرانی",
            buyPrice = "٢٤,٧٥٠",
            sellPrice = "٢٥,٢٥٠",
            unit = "دیناری عێراقی",
            change = "+١٠٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "AED",
            nameKurdish = "١٠٠ درهەمی ئیماراتی",
            buyPrice = "٤٠,٩٠٠",
            sellPrice = "٤١,٢٠٠",
            unit = "دیناری عێراقی",
            change = "+٥٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "SAR",
            nameKurdish = "١٠٠ ڕیالی سعودی",
            buyPrice = "٤٠,١٠٠",
            sellPrice = "٤٠,٣٥٠",
            unit = "دیناری عێراقی",
            change = "+٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "KWD",
            nameKurdish = "١٠٠٠ دیناری کوەیتی",
            buyPrice = "٤,٩١٠,٠٠٠",
            sellPrice = "٤,٩٣٥,٠٠٠",
            unit = "دیناری عێراقی",
            change = "+٥,٠٠٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "JOD",
            nameKurdish = "١٠٠٠ دیناری ئوردنی",
            buyPrice = "٢,١١٥,٠٠٠",
            sellPrice = "٢,١٣٠,٠٠٠",
            unit = "دیناری عێراقی",
            change = "-٢,٠٠٠",
            isPositive = false
        ),
        CurrencyRate(
            code = "SEK",
            nameKurdish = "١٠٠٠ کڕۆنی سویدی",
            buyPrice = "١٤,٢٠٠",
            sellPrice = "١٤,٥٠٠",
            unit = "دیناری عێراقی",
            change = "+٥٠",
            isPositive = true
        ),
        CurrencyRate(
            code = "NOK",
            nameKurdish = "١٠٠٠ کڕۆنی نەرویجی",
            buyPrice = "١٣,٨٠٠",
            sellPrice = "١٤,١٠٠",
            unit = "دیناری عێراقی",
            change = "-٣٠",
            isPositive = false
        ),
        CurrencyRate(
            code = "DKK",
            nameKurdish = "١٠٠٠ کڕۆنی دانیمارکی",
            buyPrice = "٢١,٧٠٠",
            sellPrice = "٢٢,٠٠٠",
            unit = "دیناری عێراقی",
            change = "+٨٠",
            isPositive = true
        )
    )

    val goldRates: List<GoldRateItem> = listOf(
        GoldRateItem(
            karat = "24",
            nameKurdish = "مسقاڵێک زێڕی عەیار ٢٤",
            priceIqd = "٥٨٥,٠٠٠ دینار",
            priceNumeric = 585000.0,
            change = "+٣,٠٠٠",
            isPositive = true
        ),
        GoldRateItem(
            karat = "22",
            nameKurdish = "مسقاڵێک زێڕی عەیار ٢٢",
            priceIqd = "٥٣٥,٠٠٠ دینار",
            priceNumeric = 535000.0,
            change = "+٢,٥٠٠",
            isPositive = true
        ),
        GoldRateItem(
            karat = "21",
            nameKurdish = "مسقاڵێک زێڕی عەیار ٢١ (پڕفرۆشترین)",
            priceIqd = "٥١٠,٠٠٠ دینار",
            priceNumeric = 510000.0,
            change = "+٢,٠٠٠",
            isPositive = true
        ),
        GoldRateItem(
            karat = "18",
            nameKurdish = "مسقاڵێک زێڕی عەیار ١٨",
            priceIqd = "٤٤٠,٠٠٠ دینار",
            priceNumeric = 440000.0,
            change = "+١,٥٠٠",
            isPositive = true
        ),
        GoldRateItem(
            karat = "LIRA",
            nameKurdish = "لیرەی زێڕ (عەیار ٢١ - کێش ٨ گرام)",
            priceIqd = "٨٩٠,٠٠٠ دینار",
            priceNumeric = 890000.0,
            change = "+٤,٠٠٠",
            isPositive = true
        ),
        GoldRateItem(
            karat = "OUNCE",
            nameKurdish = "ئۆنسەی زێڕی جیهانی (Gold Ounce)",
            priceIqd = "$٢,٥١٤.٨٠",
            priceNumeric = 2514.80,
            change = "+١٤.٢٠$",
            isPositive = true
        ),
        GoldRateItem(
            karat = "SILVER_OUNCE",
            nameKurdish = "ئۆنسەی زیوی جیهانی (Silver Ounce)",
            priceIqd = "$٢٩.٤٥",
            priceNumeric = 29.45,
            change = "+٠.٣٥$",
            isPositive = true
        ),
        GoldRateItem(
            karat = "SILVER",
            nameKurdish = "مسقاڵێک زیوی دەستی (عەیار ٩٢٥)",
            priceIqd = "٦,٨٠٠ دینار",
            priceNumeric = 6800.0,
            change = "+١٠٠",
            isPositive = true
        )
    )

    val energyCommodities: List<CommodityItem> = listOf(
        CommodityItem(
            id = "BRENT",
            nameKurdish = "بەرمیلێک نەوتی خاوی برێنت (Brent)",
            priceUsd = "$٧٨.٦٥",
            change = "+٠.٨٥$",
            isPositive = true,
            unit = "دۆلار بۆ بەرمیل"
        ),
        CommodityItem(
            id = "WTI",
            nameKurdish = "بەرمیلێک نەوتی خاوی تێکساس (WTI)",
            priceUsd = "$٧٤.٩٠",
            change = "+٠.٧٠$",
            isPositive = true,
            unit = "دۆلار بۆ بەرمیل"
        ),
        CommodityItem(
            id = "GAS",
            nameKurdish = "گازی سروشتی (Natural Gas)",
            priceUsd = "$٢.١٨",
            change = "-٠.٠٤$",
            isPositive = false,
            unit = "دۆلار بۆ ملیۆن BTU"
        )
    )

    val cryptoRates: List<CryptoRateItem> = listOf(
        CryptoRateItem(
            symbol = "BTC",
            nameKurdish = "بیتکۆین (Bitcoin)",
            priceUsd = "$٦٤,٣٥٠",
            changePercent = "+٢.٤٪",
            isPositive = true
        ),
        CryptoRateItem(
            symbol = "ETH",
            nameKurdish = "ئیسریۆم (Ethereum)",
            priceUsd = "$٢,٧٨٥",
            changePercent = "+١.٨٪",
            isPositive = true
        ),
        CryptoRateItem(
            symbol = "USDT",
            nameKurdish = "تێزەر (Tether USD)",
            priceUsd = "$١.٠٠",
            changePercent = "٠.٠٪",
            isPositive = true
        ),
        CryptoRateItem(
            symbol = "SOL",
            nameKurdish = "سۆلانا (Solana)",
            priceUsd = "$١٤٦.٥٠",
            changePercent = "+٤.٢٪",
            isPositive = true
        ),
        CryptoRateItem(
            symbol = "BNB",
            nameKurdish = "باینانس کۆین (BNB)",
            priceUsd = "$٥٦٢.٠٠",
            changePercent = "+١.١٪",
            isPositive = true
        )
    )
}

