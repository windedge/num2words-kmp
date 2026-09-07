package io.github.windedge.num2words

/**
 * Minimal signed big integer backed by a canonical decimal digit string.
 *
 * Kotlin common has no BigInteger, but num2words needs arbitrary precision:
 * card keys reach 10^303 (EU high numwords) and string inputs can be hundreds
 * of digits long (overflow test). Only the operations used by the port exist:
 * compare, add, subtract, multiply, floored divmod by small Long or by 10^n,
 * truncation, and Long conversion.
 */
class BigInt private constructor(
    /** Canonical magnitude: no leading zeros, "0" for zero. */
    val digits: String,
    val negative: Boolean,
) : Comparable<BigInt> {

    init {
        checkValue(digits.isNotEmpty() && digits.all { it in '0'..'9' })
        checkValue(digits == "0" || digits[0] != '0')
        checkValue(!negative || digits != "0")
    }

    companion object {
        val ZERO = BigInt("0", false)
        val ONE = BigInt("1", false)

        internal fun ofDigits(digits: String, negative: Boolean): BigInt = BigInt(digits, negative)

        fun fromLong(v: Long): BigInt =
            if (v == Long.MIN_VALUE) parse("-9223372036854775808")
            else if (v < 0) BigInt((-v).toString(), true)
            else BigInt(v.toString(), false)

        fun parse(s: String): BigInt {
            var t = s.trim()
            var neg = false
            if (t.startsWith("-")) {
                neg = true
                t = t.substring(1)
            } else if (t.startsWith("+")) {
                t = t.substring(1)
            }
            checkValue(t.isNotEmpty() && t.all { it in '0'..'9' }) { "Invalid integer literal: $s" }
            t = t.trimStart('0')
            if (t.isEmpty()) t = "0"
            return if (t == "0") ZERO else BigInt(t, neg)
        }

        fun pow10(n: Int): BigInt {
            checkValue(n >= 0)
            return if (n == 0) ONE else BigInt("1" + "0".repeat(n), false)
        }
    }

    fun isZero(): Boolean = digits == "0"

    fun sign(): Int = if (isZero()) 0 else if (negative) -1 else 1

    fun abs(): BigInt = if (negative) BigInt(digits, false) else this

    operator fun unaryMinus(): BigInt = if (isZero()) this else BigInt(digits, !negative)

    operator fun compareTo(other: Long): Int = compareTo(fromLong(other))

    override fun compareTo(other: BigInt): Int {
        if (negative != other.negative) return if (negative) -1 else 1
        val c = compareMagnitude(other)
        return if (negative) -c else c
    }

    private fun compareMagnitude(other: BigInt): Int {
        if (digits.length != other.digits.length) return digits.length.compareTo(other.digits.length)
        return digits.compareTo(other.digits)
    }

    operator fun plus(other: BigInt): BigInt {
        if (negative == other.negative) {
            if (isZero()) return other
            if (other.isZero()) return this
            return BigInt(addMagnitude(digits, other.digits), negative)
        }
        val c = compareMagnitude(other)
        if (c == 0) return ZERO
        return if (c > 0) BigInt(subMagnitude(digits, other.digits), negative)
        else BigInt(subMagnitude(other.digits, digits), other.negative)
    }

    operator fun minus(other: BigInt): BigInt = plus(-other)

    operator fun times(other: BigInt): BigInt {
        if (isZero() || other.isZero()) return ZERO
        return BigInt(mulMagnitude(digits, other.digits), negative != other.negative)
    }

    fun timesSmall(m: Long): BigInt = times(fromLong(m))

    /** Multiply magnitude by a single digit 0..9. */
    fun timesDigit(d: Int): BigInt {
        checkValue(d in 0..9)
        if (d == 0 || isZero()) return ZERO
        if (d == 1) return this
        val m = mulSmall(digits, d)
        return if (m == "0") ZERO else BigInt(m, negative)
    }

    fun timesPow10(n: Int): BigInt {
        checkValue(n >= 0)
        if (isZero() || n == 0) return this
        return BigInt(digits + "0".repeat(n), negative)
    }

    /**
     * Floored divmod by a positive BigInt divisor, mirroring Python divmod.
     * (Divisors in this library are always positive card keys or powers of ten.)
     */
    fun divmod(other: BigInt): Pair<BigInt, BigInt> {
        checkValue(!other.isZero()) { "Division by zero" }
        checkValue(other.sign() > 0) { "Divisor must be positive" }
        if (isZero()) return ZERO to ZERO
        val (qm, rm) = divmodMagnitude(abs(), other.abs())
        if (negative == other.negative) {
            return (if (qm == "0") ZERO else BigInt(qm, false)) to
                (if (rm == "0") ZERO else BigInt(rm, false))
        }
        if (rm == "0") return (if (qm == "0") ZERO else BigInt(qm, true)) to ZERO
        return BigInt(addMagnitude(qm, "1"), true) to
            BigInt(subMagnitude(other.abs().digits, rm), false)
    }

    operator fun div(other: BigInt): BigInt = divmod(other).first

    operator fun rem(other: BigInt): BigInt = divmod(other).second

    /** Halving/doubling helpers for exact Double conversion. */
    fun shlBits(k: Int): BigInt {
        checkValue(k >= 0)
        var r = this
        repeat(k) { r += r }
        return r
    }

    fun divPow2Trunc(k: Int): BigInt {
        checkValue(k >= 0)
        var m = abs()
        repeat(k) { m = m.divmodSmall(2).first }
        return if (negative && !m.isZero()) -m else m
    }

    /** Nearest Double (correctly rounded on JVM/JS/Native string parsing). */
    fun toDouble(): Double = toString().toDouble()

    /**
     * Floored divmod by a positive Long divisor, mirroring Python divmod
     * (remainder always in 0..d-1).
     */
    fun divmodSmall(d: Long): Pair<BigInt, Long> {
        checkValue(d > 0)
        var rem = 0L
        val q = StringBuilder()
        for (ch in digits) {
            rem = rem * 10 + (ch - '0')
            val qd = rem / d
            rem %= d
            if (q.isNotEmpty() || qd != 0L) q.append('0' + qd.toInt())
        }
        val qMag = if (q.isEmpty()) "0" else q.toString()
        if (!negative || rem == 0L) {
            return Pair(if (qMag == "0") ZERO else BigInt(qMag, negative && qMag != "0"), rem)
        }
        return Pair(-(parse(qMag) + ONE), d - rem)
    }

    /**
     * Floored divmod by 10^n, mirroring Python divmod(v, 10**n).
     */
    fun divmodPow10(n: Int): Pair<BigInt, BigInt> {
        checkValue(n >= 0)
        if (n == 0) return Pair(this, ZERO)
        if (!negative) {
            if (digits.length <= n) return Pair(ZERO, this)
            return Pair(parse(digits.dropLast(n)), parse(digits.takeLast(n)))
        }
        if (digits.length <= n) return Pair(-ONE, pow10(n) - abs())
        val qm = digits.dropLast(n)
        val rm = digits.takeLast(n)
        if (rm.trimStart('0').isEmpty()) return Pair(-parse(qm), ZERO)
        return Pair(-(parse(qm) + ONE), pow10(n) - parse(rm))
    }

    /** Truncating integer division by 10^n (toward zero), for decimal truncation. */
    fun truncateDivPow10(n: Int): BigInt {
        if (n <= 0) return this
        if (digits.length <= n) return ZERO
        val q = parse(digits.dropLast(n))
        return if (negative) -q else q
    }

    fun toLong(): Long {
        // String.toLong() loses precision on JS past 2^53; stay in BigInt arithmetic instead.
        if (digits.length < 19) {
            var v = 0L
            for (ch in digits) v = v * 10 + (ch - '0')
            return if (negative) -v else v
        }
        if (digits.length > 19) throw Num2WordsOverflowError("BigInt out of Long range: $this")
        if (!negative) {
            if (digits > "9223372036854775807") throw Num2WordsOverflowError("BigInt out of Long range: $this")
            var v = 0L
            for (ch in digits) v = v * 10 + (ch - '0')
            return v
        }
        if (digits == "9223372036854775808") return Long.MIN_VALUE
        if (digits > "9223372036854775808") throw Num2WordsOverflowError("BigInt out of Long range: $this")
        var v = 0L
        for (ch in digits) v = v * 10 + (ch - '0')
        return -v
    }

    override fun equals(other: Any?): Boolean =
        other is BigInt && digits == other.digits && negative == other.negative

    override fun hashCode(): Int = 31 * digits.hashCode() + negative.hashCode()

    override fun toString(): String = if (negative) "-$digits" else digits
}

private fun mulSmall(a: String, m: Int): String {
    checkValue(m in 0..10)
    if (m == 0 || a == "0") return "0"
    if (m == 1) return a
    var carry = 0
    val sb = StringBuilder()
    for (i in a.length - 1 downTo 0) {
        val t = (a[i] - '0') * m + carry
        sb.append('0' + (t % 10))
        carry = t / 10
    }
    while (carry > 0) {
        sb.append('0' + (carry % 10))
        carry /= 10
    }
    return sb.reverse().toString()
}

/** Schoolbook long division on magnitudes; both inputs non-negative, divisor non-zero. */
private fun divmodMagnitude(a: BigInt, b: BigInt): Pair<String, String> {
    if (a < b) return "0" to a.digits
    val q = StringBuilder()
    var rem = "0"
    for (ch in a.digits) {
        rem = if (rem == "0") ch.toString() else rem + ch
        val remBig = BigInt.ofDigits(rem, false)
        var lo = 0
        var hi = 9
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (BigInt.ofDigits(mulSmall(b.digits, mid), false) <= remBig) lo = mid else hi = mid - 1
        }
        q.append('0' + lo)
        if (lo != 0) rem = subMagnitude(rem, mulSmall(b.digits, lo))
    }
    val qs = q.toString().trimStart('0').ifEmpty { "0" }
    return qs to rem
}

private fun addMagnitude(a: String, b: String): String {
    var i = a.length - 1
    var j = b.length - 1
    var carry = 0
    val sb = StringBuilder()
    while (i >= 0 || j >= 0 || carry != 0) {
        val s = (if (i >= 0) a[i--] - '0' else 0) + (if (j >= 0) b[j--] - '0' else 0) + carry
        sb.append('0' + (s % 10))
        carry = s / 10
    }
    return sb.reverse().toString()
}

/** Magnitude subtraction, requires a >= b. */
private fun subMagnitude(a: String, b: String): String {
    var i = a.length - 1
    var j = b.length - 1
    var borrow = 0
    val sb = StringBuilder()
    while (i >= 0) {
        var d = (a[i--] - '0') - borrow - (if (j >= 0) b[j--] - '0' else 0)
        if (d < 0) {
            d += 10
            borrow = 1
        } else {
            borrow = 0
        }
        sb.append('0' + d)
    }
    val r = sb.reverse().toString().trimStart('0')
    return if (r.isEmpty()) "0" else r
}

private fun mulMagnitude(a: String, b: String): String {
    if (a == "0" || b == "0") return "0"
    val x = a.reversed()
    val y = b.reversed()
    val r = IntArray(x.length + y.length)
    for (i in x.indices) {
        val dx = x[i] - '0'
        if (dx == 0) continue
        for (j in y.indices) {
            r[i + j] += dx * (y[j] - '0')
        }
    }
    var carry = 0
    for (k in r.indices) {
        val t = r[k] + carry
        r[k] = t % 10
        carry = t / 10
    }
    val sb = StringBuilder()
    for (k in r.indices.reversed()) sb.append('0' + r[k])
    val s = sb.toString().trimStart('0')
    return if (s.isEmpty()) "0" else s
}
