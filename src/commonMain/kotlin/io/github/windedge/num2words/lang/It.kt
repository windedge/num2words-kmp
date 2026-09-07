package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.checkValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

private const val IT_ZERO = "zero"

private val IT_CARDINAL_WORDS = listOf(
    IT_ZERO, "uno", "due", "tre", "quattro", "cinque", "sei", "sette", "otto",
    "nove", "dieci", "undici", "dodici", "tredici", "quattordici", "quindici",
    "sedici", "diciassette", "diciotto", "diciannove",
)

private val IT_ORDINAL_WORDS = listOf(
    IT_ZERO, "primo", "secondo", "terzo", "quarto", "quinto", "sesto", "settimo",
    "ottavo", "nono", "decimo", "undicesimo", "dodicesimo", "tredicesimo",
    "quattordicesimo", "quindicesimo", "sedicesimo", "diciassettesimo",
    "diciottesimo", "diciannovesimo",
)

private val IT_STR_TENS = mapOf(2 to "venti", 3 to "trenta", 4 to "quaranta", 6 to "sessanta")

private val IT_EXPONENT_PREFIXES = listOf(
    IT_ZERO, "m", "b", "tr", "quadr", "quint", "sest", "sett", "ott", "nov", "dec",
)

private val IT_GENERIC_DOLLARS = listOf("dollaro", "dollari")
private val IT_GENERIC_CENTS = listOf("centesimo", "centesimi")

/** Mirrors num2words/lang_IT.py Num2Word_IT (self-contained, bypasses EU cards). */
class Num2WordIt : Num2WordEU() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "EUR" to (listOf("euro", "euro") to IT_GENERIC_CENTS),
        "USD" to (IT_GENERIC_DOLLARS to IT_GENERIC_CENTS),
        "GBP" to (listOf("sterlina", "sterline") to listOf("penny", "penny")),
        "CNY" to (listOf("yuan", "yuan") to listOf("fen", "fen")),
    )

    companion object {
        const val MINUS_PREFIX_WORD = "meno "
        const val FLOAT_INFIX_WORD = " virgola "
    }

    override fun setup() {
        super.setup()
    }

    fun floatToWords(value: SimpleDecimal, ordinal: Boolean = false): String {
        val intPart = value.toBigInt()
        val prefix = if (ordinal) toOrdinalIt(NumValue.Whole(intPart)) else toCardinalIt(NumValue.Whole(intPart))
        val plain = value.toPlainString()
        val floatPart = plain.substring(plain.indexOf('.') + 1)
        val postfix = floatPart.map { toCardinalIt(NumValue.Whole(BigInt.fromLong((it - '0').toLong()))) }
            .joinToString(" ")
        return prefix + FLOAT_INFIX_WORD + postfix
    }

    fun tensToCardinal(number: Long): String {
        val tens = (number / 10).toInt()
        val units = (number % 10).toInt()
        val prefix = IT_STR_TENS[tens] ?: (IT_CARDINAL_WORDS[tens].dropLast(1) + "anta")
        val postfix = if (IT_CARDINAL_WORDS[units] == IT_ZERO) "" else IT_CARDINAL_WORDS[units]
        return phoneticContraction(prefix + postfix)
    }

    fun hundredsToCardinal(number: Long): String {
        val hundreds = number / 100
        val prefix = if (hundreds == 1L) "cento" else IT_CARDINAL_WORDS[hundreds.toInt()] + "cento"
        val rest = toCardinalIt(NumValue.Whole(BigInt.fromLong(number % 100)))
        val postfix = if (rest == IT_ZERO) "" else rest
        return phoneticContraction(prefix + postfix)
    }

    fun thousandsToCardinal(number: Long): String {
        val thousands = number / 1000
        val prefix = if (thousands == 1L) "mille"
        else toCardinalIt(NumValue.Whole(BigInt.fromLong(thousands))) + "mila"
        val rest = toCardinalIt(NumValue.Whole(BigInt.fromLong(number % 1000)))
        val postfix = if (rest == IT_ZERO) "" else rest
        return prefix + postfix
    }

    fun bigNumberToCardinal(number: BigInt): String {
        val digits = number.toString().toList()
        val length = digits.size
        checkValue(length < 66) { "The given number is too large." }
        val predigits = if (length % 3 == 0) 3 else length % 3
        val multiplier = digits.take(predigits)
        val exponent = digits.drop(predigits)
        var infix = exponentLengthToString(exponent.size)
        val prefix: String
        if (multiplier == listOf('1')) {
            prefix = "un "
        } else {
            prefix = toCardinalIt(NumValue.Whole(BigInt.parse(multiplier.joinToString(""))))
            infix = " " + infix.dropLast(1) + "i"
        }
        val postfix: String
        if (exponent.toSet() != setOf('0')) {
            postfix = toCardinalIt(NumValue.Whole(BigInt.parse(exponent.joinToString(""))))
            infix += if (" e " in postfix) ", " else " e "
        } else {
            postfix = ""
        }
        return prefix + infix + postfix
    }

    fun toCardinalIt(value: NumValue): String {
        when (value) {
            is NumValue.Whole -> {
                val v = value.v
                if (v.sign() < 0) return MINUS_PREFIX_WORD + toCardinalIt(NumValue.Whole(-v))
                return accentuate(wholeToCardinalIt(v))
            }

            is NumValue.Decimal -> {
                if (value.v.isNegative()) return MINUS_PREFIX_WORD + toCardinalIt(
                    NumValue.Decimal(value.v.negate()),
                )
                val w = integerIfWhole(value.v)
                if (w != null) return toCardinalIt(NumValue.Whole(w))
                return accentuate(floatToWords(value.v))
            }

            is NumValue.FloatVal -> {
                val d = value.v
                if (d < 0) return MINUS_PREFIX_WORD + toCardinalIt(NumValue.FloatVal(-d))
                if (d % 1.0 == 0.0) return toCardinalIt(NumValue.Whole(doubleToBigInt(d)))
                return accentuate(floatToWords(SimpleDecimal.fromDouble(d)))
            }
        }
    }

    private fun wholeToCardinalIt(v: BigInt): String {
        if (v <= BigInt.fromLong(19)) return IT_CARDINAL_WORDS[v.toLong().toInt()]
        if (v < BigInt.fromLong(100)) return tensToCardinal(v.toLong())
        if (v < BigInt.fromLong(1000)) return hundredsToCardinal(v.toLong())
        if (v < BigInt.fromLong(1000000)) return thousandsToCardinal(v.toLong())
        return bigNumberToCardinal(v)
    }

    override fun toCardinal(value: NumValue): String = toCardinalIt(value)

    fun toOrdinalIt(value: NumValue): String {
        when (value) {
            is NumValue.Whole -> {
                val v = value.v
                if (v.sign() < 0) return MINUS_PREFIX_WORD + toOrdinalIt(NumValue.Whole(-v))
                if (v <= BigInt.fromLong(19)) return IT_ORDINAL_WORDS[v.toLong().toInt()]
                val tens = (v % BigInt.fromLong(100)).toLong()
                val isOutsideTeens = !(10 < tens && tens < 20)
                if (isOutsideTeens && tens % 10 == 3L) {
                    return toCardinalIt(value).dropLast(1) + "eesimo"
                }
                if (isOutsideTeens && tens % 10 == 6L) {
                    return toCardinalIt(value) + "esimo"
                }
                var string = toCardinalIt(value).dropLast(1)
                if (string.takeLast(3) == "mil") string += "l"
                return string + "esimo"
            }

            is NumValue.Decimal -> {
                if (value.v.isNegative()) return MINUS_PREFIX_WORD + toOrdinalIt(
                    NumValue.Decimal(value.v.negate()),
                )
                val w = integerIfWhole(value.v)
                if (w != null) return toOrdinalIt(NumValue.Whole(w))
                return floatToWords(value.v, ordinal = true)
            }

            is NumValue.FloatVal -> {
                val d = value.v
                if (d < 0) return MINUS_PREFIX_WORD + toOrdinalIt(NumValue.FloatVal(-d))
                if (d % 1.0 == 0.0) return toOrdinalIt(NumValue.Whole(doubleToBigInt(d)))
                return floatToWords(SimpleDecimal.fromDouble(d), ordinal = true)
            }
        }
    }

    override fun toOrdinal(value: NumValue): String = toOrdinalIt(value)

    fun toCurrencyIt(
        value: NumValue,
        currency: String = "EUR",
        cents: Boolean = true,
        separator: String = " e",
        adjective: Boolean = false,
    ): String {
        var result = super.toCurrency(value, currency, cents, separator, adjective)
        if (currency == "GBP") {
            val parts = result.split(" ")
            if (parts[0] == "uno") result = "una " + parts.drop(1).joinToString(" ")
        }
        result = result.replace("uno", "un")
        return result
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyIt(value, currency, cents, separator, adjective)
}

private fun phoneticContraction(s: String): String = s
    .replace("oo", "o")
    .replace("ao", "o")
    .replace("io", "o")
    .replace("au", "u")
    .replace("iu", "u")

private fun exponentLengthToString(exponentLength: Int): String {
    val prefix = IT_EXPONENT_PREFIXES[exponentLength / 6]
    return if (exponentLength % 6 == 0) prefix + "ilione" else prefix + "iliardo"
}

private fun accentuate(s: String): String = s.split(" ").joinToString(" ") { w ->
    if (w.takeLast(3) == "tre" && w.length > 3) w.replace("tré", "tre").dropLast(3) + "tré"
    else w.replace("tré", "tre")
}
