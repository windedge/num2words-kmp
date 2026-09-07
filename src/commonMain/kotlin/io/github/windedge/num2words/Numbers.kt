package io.github.windedge.num2words

/** Exact conversion of an integral Double to BigInt (mirrors Python int(float)). */
fun doubleToBigInt(d: Double): BigInt {
    checkValue(d.isFinite()) { "Not finite: $d" }
    checkValue(d % 1.0 == 0.0) { "Not integral: $d" }
    if (d == 0.0) return BigInt.ZERO
    val bits = d.toBits()
    val neg = (bits ushr 63) != 0L
    val rawExp = ((bits ushr 52) and 0x7FFL).toInt()
    val rawMant = bits and 0xFFFFFFFFFFFFFL
    val mant: Long
    val e: Int
    if (rawExp == 0) {
        mant = rawMant
        e = -1074
    } else {
        mant = rawMant or 0x10000000000000L
        e = rawExp - 1075
    }
    var v = BigInt.fromLong(mant)
    v = if (e >= 0) v.shlBits(e) else v.divPow2Trunc(-e)
    return if (neg) -v else v
}

/** Python-style str(float): "12.0", "1e+16", "-0.0", "1e-05". Used for error messages. */
fun trimDouble(d: Double): String {
    checkValue(d.isFinite()) { "Not finite: $d" }
    if (d == 0.0) return if ((d.toBits() ushr 63) != 0L) "-0.0" else "0.0"
    val plain = SimpleDecimal.doubleRepr(d)
    val neg = plain.startsWith("-")
    val body = if (neg) plain.substring(1) else plain
    val dot = body.indexOf('.')
    val intPart = if (dot < 0) body else body.substring(0, dot)
    val fracPart = if (dot < 0) "" else body.substring(dot + 1)
    val allDigits = intPart + fracPart
    val leadingZeros = allDigits.takeWhile { it == '0' }.length
    val digits = allDigits.drop(leadingZeros).ifEmpty { "0" }
    val sign = if (neg) "-" else ""
    if (digits == "0") return sign + "0.0"
    val point = if (dot < 0) intPart.length else dot
    val exp = point - leadingZeros - 1
    val sig = digits.trimEnd('0')
    if (exp in -4..15) {
        val pos = exp + 1
        return sign + when {
            pos <= 0 -> "0." + "0".repeat(-pos) + sig
            pos >= sig.length -> sig + "0".repeat(pos - sig.length) + ".0"
            else -> sig.substring(0, pos) + "." + sig.substring(pos)
        }
    }
    return sign + scientific(sig, exp)
}

private fun scientific(sig: String, exp: Int): String {
    val mantissa = if (sig.length == 1) sig else "${sig[0]}.${sig.substring(1)}"
    val ae = if (exp < 0) -exp else exp
    val expStr = (if (exp < 0) "e-" else "e+") + (if (ae < 10) "0$ae" else "$ae")
    return "$mantissa$expStr"
}

/** Mirrors Python "%02d" for non-negative values. */
fun pad2(n: BigInt): String {
    val s = n.toString()
    return if (s.length >= 2) s else "0$s"
}

/** Returns the whole value when the decimal has no fractional part, else null. */
fun integerIfWhole(dec: SimpleDecimal): BigInt? {
    val (_, rem) = dec.unscaled.abs().divmod(BigInt.pow10(dec.scale))
    return if (rem.isZero()) dec.toBigInt() else null
}
