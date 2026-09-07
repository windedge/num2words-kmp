package io.github.windedge.num2words

/**
 * Mirrors num2words/utils.py splitbyx.
 * Splits string n into chunks of width x from right to left;
 * formatInt=true converts each chunk to Int.
 */
fun splitByX(n: String, x: Int, formatInt: Boolean = true): List<Any> {
    val length = n.length
    val result = mutableListOf<Any>()
    if (length > x) {
        val start = length % x
        if (start > 0) {
            result.add(if (formatInt) n.substring(0, start).toInt() else n.substring(0, start))
        }
        var i = start
        while (i < length) {
            val part = n.substring(i, i + x)
            result.add(if (formatInt) part.toInt() else part)
            i += x
        }
    } else {
        result.add(if (formatInt) n.toInt() else n)
    }
    return result
}

/** Mirrors utils.py get_digits: [ones, tens, hundreds] of n. */
fun getDigits(n: Long): List<Int> {
    checkValue(n >= 0) { "getDigits expects a non-negative value, got $n" }
    val s = n.toString().padStart(3, '0').takeLast(3)
    return s.reversed().map { it - '0' }
}
