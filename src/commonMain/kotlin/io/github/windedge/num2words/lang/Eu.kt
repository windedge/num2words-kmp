package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordBase

private val GENERIC_DOLLARS = listOf("dollar", "dollars")
private val GENERIC_CENTS = listOf("cent", "cents")

/** Mirrors num2words/lang_EU.py Num2Word_EU. */
open class Num2WordEU : Num2WordBase() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "AUD" to (GENERIC_DOLLARS to GENERIC_CENTS),
        "BYN" to (listOf("rouble", "roubles") to listOf("kopek", "kopeks")),
        "CAD" to (GENERIC_DOLLARS to GENERIC_CENTS),
        // replaced by EUR
        "EEK" to (listOf("kroon", "kroons") to listOf("sent", "senti")),
        "EUR" to (listOf("euro", "euro") to GENERIC_CENTS),
        "GBP" to (listOf("pound sterling", "pounds sterling") to listOf("penny", "pence")),
        // replaced by EUR
        "LTL" to (listOf("litas", "litas") to GENERIC_CENTS),
        // replaced by EUR
        "LVL" to (listOf("lat", "lats") to listOf("santim", "santims")),
        "USD" to (GENERIC_DOLLARS to GENERIC_CENTS),
        "RUB" to (listOf("rouble", "roubles") to listOf("kopek", "kopeks")),
        "SEK" to (listOf("krona", "kronor") to listOf("öre", "öre")),
        "NOK" to (listOf("krone", "kroner") to listOf("øre", "øre")),
        "PLN" to (listOf("zloty", "zlotys", "zlotu") to listOf("grosz", "groszy")),
        "MXN" to (listOf("peso", "pesos") to GENERIC_CENTS),
        "RON" to (listOf("leu", "lei", "de lei") to listOf("ban", "bani", "de bani")),
        "INR" to (listOf("rupee", "rupees") to listOf("paisa", "paise")),
        "HUF" to (listOf("forint", "forint") to listOf("fillér", "fillér")),
        "ISK" to (listOf("króna", "krónur") to listOf("aur", "aurar")),
        "UZS" to (listOf("sum", "sums") to listOf("tiyin", "tiyins")),
        "SAR" to (listOf("saudi riyal", "saudi riyals") to listOf("halalah", "halalas")),
        "JPY" to (listOf("yen", "yen") to listOf("sen", "sen")),
        "KRW" to (listOf("won", "won") to listOf("jeon", "jeon")),
    )

    override val currencyAdjectives: Map<String, String> = mapOf(
        "AUD" to "Australian",
        "BYN" to "Belarusian",
        "CAD" to "Canadian",
        "EEK" to "Estonian",
        "USD" to "US",
        "RUB" to "Russian",
        "NOK" to "Norwegian",
        "MXN" to "Mexican",
        "RON" to "Romanian",
        "INR" to "Indian",
        "HUF" to "Hungarian",
        "ISK" to "íslenskar",
        "UZS" to "Uzbekistan",
        "SAR" to "Saudi",
        "JPY" to "Japanese",
        "KRW" to "Korean",
    )

    var gigaSuffix: String = "illiard"
    var megaSuffix: String = "illion"

    override fun setHighNumwords(high: List<String>) {
        val cap = 3 + 6 * high.size
        var n = cap
        for (word in high) {
            if (gigaSuffix.isNotEmpty()) cards[BigInt.pow10(n)] = word + gigaSuffix
            if (megaSuffix.isNotEmpty()) cards[BigInt.pow10(n - 3)] = word + megaSuffix
            n -= 6
        }
    }

    override fun pluralize(n: BigInt, forms: List<String>): String =
        forms[if (n == BigInt.ONE) 0 else 1]

    override fun setup() {
        gigaSuffix = "illiard"
        megaSuffix = "illion"
        val lows = listOf("non", "oct", "sept", "sext", "quint", "quadr", "tr", "b", "m")
        val units = listOf("", "un", "duo", "tre", "quattuor", "quin", "sex", "sept", "octo", "novem")
        val tens = listOf(
            "dec", "vigint", "trigint", "quadragint", "quinquagint",
            "sexagint", "septuagint", "octogint", "nonagint",
        )
        useNumwords(
            high = listOf("cent") + genHighNumwords(units, tens, lows),
            mid = emptyList(),
            low = emptyList(),
        )
    }
}
