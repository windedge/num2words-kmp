package io.github.windedge.num2words

/**
 * Mirrors num2words/currency.py parse_currency_parts.
 * Returns (integer, cents, isNegative); all in arbitrary precision via [BigInt].
 *
 * Key semantics:
 * - int input: treated as cents by default (the integer counts cents);
 *   divmod 100 yields whole and cents; isIntWithCents=false treats it as whole.
 * - non-int (float/str/Decimal): convert to SimpleDecimal first, quantize to
 *   cents (ROUND_HALF_UP), then divmod(1).
 * - 0.005 -> (0, 1); 0.004 -> (0, 0); 0.999 -> (1, 0), same as Python.
 */
fun parseCurrencyParts(
    value: Any,
    isIntWithCents: Boolean = true,
): Triple<BigInt, Int, Boolean> {
    // NOTE: on Kotlin/JS every Number is a Double, so `x is Int` matches 1.01.
    // The non-integral Double/Float check must come first; only integral values
    // reach the integer branch (mirrors Python isinstance(1.01, int) == False).
    // Integral floats take the decimal path too: Python currency treats float
    // 1.0 via Decimal(float) -> (1, 0), not as 100 cents.
    // Decimal(float) funnels the exact binary value, so HALF_UP on 2.675 -> 2.67 like Python.
    if (value is Double || value is Float) {
        val dec = if (value is Double) SimpleDecimal.fromDoubleExact(value)
        else SimpleDecimal.fromDoubleExact((value as Float).toDouble())
        return quantizeParts(dec)
    }
    if (value is Long || value is Int || value is Short || value is Byte) {
        val v = when (value) {
            is Long -> BigInt.fromLong(value)
            is Int -> BigInt.fromLong(value.toLong())
            is Short -> BigInt.fromLong(value.toLong())
            else -> BigInt.fromLong((value as Byte).toLong())
        }
        return if (isIntWithCents) {
            val negative = v.sign() < 0
            val (q, r) = v.abs().divmodSmall(100)
            Triple(q, r.toInt(), negative)
        } else {
            val negative = v.sign() < 0
            Triple(v.abs(), 0, negative)
        }
    }

    val dec = when (value) {
        is String -> SimpleDecimal.fromString(value)
        is SimpleDecimal -> value
        is BigInt -> SimpleDecimal.fromBigInt(value)
        else -> throw Num2WordsValueError("Unsupported currency value type: ${value::class}")
    }
    return quantizeParts(dec)
}

private fun quantizeParts(dec: SimpleDecimal): Triple<BigInt, Int, Boolean> {
    val q = dec.quantize(2)
    val negative = q.isNegative()
    val (integer, fraction) = q.abs().divmodOne()
    val cents = fraction.unscaled.truncateDivPow10(0).toLong().toInt() // 0..99
    return Triple(integer, cents, negative)
}

/** Mirrors currency.py prefix_currency: prefix an adjective to every currency plural form. */
fun prefixCurrency(prefix: String, base: List<String>): List<String> =
    base.map { "$prefix $it" }
