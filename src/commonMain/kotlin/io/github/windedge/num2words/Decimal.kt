package io.github.windedge.num2words

/**
 * Pure-Kotlin fixed-point decimal, mirroring the minimal decimal.Decimal semantics
 * used by this library: parse(String/Long/Double/BigInt), quantize(N, HALF_UP),
 * divmod, sign, truncation, and as_tuple().exponent.
 *
 * Arbitrary precision via [BigInt]; never touches Double except where Python
 * itself goes through float (float2tuple uses the short repr; currency uses
 * the exact binary expansion, see fromDoubleExact).
 * Not a full BigDecimal; only implements what num2words needs.
 */
class SimpleDecimal(
    val unscaled: BigInt,
    val scale: Int,
    /** Whether the source literal contained a decimal point (mirrors '.' in str(Decimal)). */
    val hasPoint: Boolean = scale > 0,
) {
    init {
        checkValue(scale >= 0)
    }

    /** Exact decimal rendering, same shape as Python str(Decimal): scale preserved, no stripping. */
    fun toPlainString(): String {
        if (scale == 0) return unscaled.toString()
        val s = unscaled.toString()
        val neg = s.startsWith("-")
        val digits = if (neg) s.substring(1) else s
        val padded = digits.padStart(scale + 1, '0')
        val intPart = padded.substring(0, padded.length - scale)
        val fracPart = padded.substring(padded.length - scale)
        return (if (neg) "-" else "") + intPart + "." + fracPart
    }

    fun isNegative(): Boolean = unscaled.sign() < 0
    fun isZero(): Boolean = unscaled.isZero()

    fun negate(): SimpleDecimal = SimpleDecimal(-unscaled, scale, hasPoint)

    fun abs(): SimpleDecimal = if (unscaled.sign() < 0) negate() else this

    /** Truncating conversion, mirroring Python int(x). */
    fun toBigInt(): BigInt = unscaled.truncateDivPow10(scale)

    fun toLong(): Long = toBigInt().toLong()

    /** Number of fractional digits (Python Decimal.as_tuple().exponent negated). */
    fun fractionDigits(): Int = scale

    fun toDouble(): Double = toPlainString().toDouble()

    /** Mirrors Decimal.quantize(scale, ROUND_HALF_UP): half rounds away from zero. */
    fun quantize(targetScale: Int): SimpleDecimal {
        if (scale <= targetScale) return rescale(targetScale)
        val drop = scale - targetScale
        val absUn = unscaled.abs()
        val (qMag, rMag) = absUn.divmodPow10(drop)
        var q = if (unscaled.sign() < 0) -qMag else qMag
        val half = BigInt.pow10(drop - 1).timesSmall(5)
        if (rMag >= half) {
            q += if (unscaled.sign() < 0) BigInt.fromLong(-1) else BigInt.ONE
        }
        return SimpleDecimal(q, targetScale, true)
    }

    fun rescale(targetScale: Int): SimpleDecimal {
        if (targetScale == scale) return this
        checkValue(targetScale >= scale)
        return SimpleDecimal(unscaled.timesPow10(targetScale - scale), targetScale, hasPoint)
    }

    /**
     * Floored divmod by 1, mirroring Python divmod(decimal, 1):
     * returns (integer floor, fractional part in [0, 1)).
     */
    fun divmodOne(): Pair<BigInt, SimpleDecimal> {
        val absUn = unscaled.abs()
        val (qMag, rMag) = absUn.divmodPow10(scale)
        return if (unscaled.sign() >= 0) {
            qMag to SimpleDecimal(rMag, scale, scale > 0)
        } else {
            if (rMag.isZero()) -qMag to SimpleDecimal(BigInt.ZERO, scale, scale > 0)
            else -(qMag + BigInt.ONE) to SimpleDecimal(BigInt.pow10(scale) - rMag, scale, scale > 0)
        }
    }

    operator fun compareTo(other: SimpleDecimal): Int {
        val target = maxOf(scale, other.scale)
        return rescale(target).unscaled.compareTo(other.rescale(target).unscaled)
    }

    operator fun minus(other: SimpleDecimal): SimpleDecimal {
        val target = maxOf(scale, other.scale)
        val c = this.rescale(target).unscaled - other.rescale(target).unscaled
        return SimpleDecimal(c, target, target > 0)
    }

    override fun equals(other: Any?): Boolean =
        other is SimpleDecimal && compareTo(other) == 0

    override fun hashCode(): Int = toPlainString().hashCode()

    override fun toString(): String = toPlainString()

    companion object {
        fun fromLong(v: Long): SimpleDecimal = SimpleDecimal(BigInt.fromLong(v), 0, false)

        fun fromBigInt(v: BigInt): SimpleDecimal = SimpleDecimal(v, 0, false)

        /** Short-repr form, mirroring Decimal(str(float)): used by float2tuple. */
        fun fromDouble(v: Double): SimpleDecimal = parsePlain(doubleRepr(v), mantissaHasPoint(v))

        /**
         * Exact binary expansion, mirroring Decimal(float): used by currency.
         * E.g. 2.675 -> 2.67499999999999982... so HALF_UP gives 2.67, same as Python.
         */
        fun fromDoubleExact(v: Double): SimpleDecimal {
            checkValue(v.isFinite()) { "Not finite: $v" }
            if (v == 0.0) return SimpleDecimal(BigInt.ZERO, 0, true)
            val bits = v.toBits()
            val neg = (bits ushr 63) != 0L
            val rawExp = ((bits ushr 52) and 0x7FFL).toInt()
            val rawMant = bits and 0xFFFFFFFFFFFFFL
            val mantLong: Long
            val e: Int
            if (rawExp == 0) {
                mantLong = rawMant
                e = -1074
            } else {
                mantLong = rawMant or 0x10000000000000L
                e = rawExp - 1075
            }
            var m = BigInt.fromLong(mantLong)
            if (neg) m = -m
            if (e >= 0) return SimpleDecimal(m.shlBits(e), 0, false)
            var t = m.abs()
            repeat(-e) { t = t.timesDigit(5) }
            return SimpleDecimal(if (neg) -t else t, -e, true)
        }

        /** Decimal string, exact, exponents allowed (mirrors Python Decimal(str)). */
        fun fromString(s: String): SimpleDecimal {
            var t = s.trim()
            var exp = 0
            val eIdx = t.indexOfFirst { it == 'e' || it == 'E' }
            if (eIdx >= 0) {
                exp = t.substring(eIdx + 1).toInt()
                t = t.substring(0, eIdx)
            }
            val point = t.contains('.')
            val (mag, scale) = parseMantissa(t, s)
            val newScale = scale - exp
            return if (newScale >= 0) SimpleDecimal(mag, newScale, point)
            else SimpleDecimal(mag.timesPow10(-newScale), 0, point)
        }

        private fun parseMantissa(t: String, original: String): Pair<BigInt, Int> {
            var body = t
            val neg = body.startsWith("-") || body.startsWith("+")
            val isNeg = body.startsWith("-")
            if (neg) body = body.substring(1)
            checkValue(body.isNotEmpty()) { "Invalid decimal literal: $original" }
            val dot = body.indexOf('.')
            checkValue(body.all { it in '0'..'9' || it == '.' } && body.count { it == '.' } <= 1) {
                "Invalid decimal literal: $original"
            }
            val intPart = if (dot < 0) body else body.substring(0, dot)
            val fracPart = if (dot < 0) "" else body.substring(dot + 1)
            var mag = BigInt.parse((intPart + fracPart).ifEmpty { "0" })
            if (isNeg && !mag.isZero()) mag = -mag
            return mag to fracPart.length
        }

        private fun parsePlain(t: String, point: Boolean): SimpleDecimal {
            val (mag, scale) = parseMantissa(t, t)
            return SimpleDecimal(mag, scale, point)
        }

        private fun mantissaHasPoint(v: Double): Boolean {
            val s = v.toString()
            val mantissa = s.substring(0, s.indexOfFirst { it == 'e' || it == 'E' }.let { if (it < 0) s.length else it })
            return mantissa.contains('.')
        }

        /** Expands platform double text (1.0E16 / 1e+21 / 12.5) to plain decimal text. */
        internal fun doubleRepr(v: Double): String {
            checkValue(v.isFinite()) { "Not finite: $v" }
            val s = v.toString()
            val eIdx = s.indexOfFirst { it == 'e' || it == 'E' }
            if (eIdx < 0) return s
            val mantissa = s.substring(0, eIdx)
            val exp = s.substring(eIdx + 1).toInt()
            val neg = mantissa.startsWith("-")
            val bare = mantissa.replace("-", "").replace("+", "")
            val dotPos = bare.indexOf('.')
            val digits0 = bare.replace(".", "")
            val digits = digits0.trimStart('0').ifEmpty { "0" }
            if (digits == "0") return "0"
            val intDigits = if (dotPos < 0) digits0.length else dotPos
            val shift = exp + intDigits
            val sign = if (neg) "-" else ""
            return if (shift >= digits.length) {
                sign + digits + "0".repeat(shift - digits.length)
            } else if (shift <= 0) {
                sign + "0." + "0".repeat(-shift) + digits
            } else {
                sign + digits.substring(0, shift) + "." + digits.substring(shift)
            }
        }
    }
}
