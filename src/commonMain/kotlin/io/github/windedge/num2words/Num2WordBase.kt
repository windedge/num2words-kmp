package io.github.windedge.num2words

/**
 * Mirrors num2words/base.py Num2Word_Base.
 *
 * Values are arbitrary-precision [NumValue] (whole / decimal / float),
 * because card keys and string inputs can exceed Long range.
 * Language classes override the open hooks (merge, pluralize, toOrdinal, ...).
 */
sealed interface NumValue {
    data class Whole(val v: BigInt) : NumValue
    data class Decimal(val v: SimpleDecimal) : NumValue
    data class FloatVal(val v: Double) : NumValue
}

/** Cardinal split tree, mirroring the nested list/tuple structure of Python splitnum. */
sealed interface SplitNode {
    data class Word(val text: String, val num: BigInt) : SplitNode
    data class Branch(val children: List<SplitNode>) : SplitNode
}

open class Num2WordBase {
    open val currencyForms: Map<String, Pair<List<String>, List<String>>> = emptyMap()
    open val currencyAdjectives: Map<String, String> = emptyMap()

    var isTitle: Boolean = false
    var precision: Int = 2
    val excludeTitle: MutableList<String> = mutableListOf()
    var negword: String = "(-) "
    var pointword: String = "(.)"
    var errmsgNonnum: String = "type(%s) not in [long, int, float]"
    var errmsgFloatord: String = "Cannot treat float %s as ordinal."
    var errmsgNegord: String = "Cannot treat negative num %s as ordinal."
    var errmsgToobig: String = "abs(%s) must be less than %s."

    val cards: MutableMap<BigInt, String> = LinkedHashMap()
    var maxVal: BigInt? = null

    protected var highWords: List<String> = emptyList()
    protected var midWords: List<Pair<BigInt, String>> = emptyList()
    protected var lowWords: List<String> = emptyList()
    private var hasNumwords: Boolean = false

    init {
        setup()
        if (hasNumwords) {
            setNumwords()
            maxVal = cards.keys.first() * BigInt.fromLong(1000)
        }
    }

    protected fun useNumwords(
        high: List<String>,
        mid: List<Pair<BigInt, String>>,
        low: List<String>,
    ) {
        highWords = high
        midWords = mid
        lowWords = low
        hasNumwords = true
    }

    open fun setNumwords() {
        setHighNumwords(highWords)
        setMidNumwords(midWords)
        setLowNumwords(lowWords)
    }

    open fun setHighNumwords(high: List<String>): Unit =
        throw Num2WordsNotImplemented("setHighNumwords")

    fun setMidNumwords(mid: List<Pair<BigInt, String>>) {
        for ((key, value) in mid) cards[key] = value
    }

    open fun setLowNumwords(numwords: List<String>) {
        for ((index, word) in numwords.withIndex()) {
            cards[BigInt.fromLong((numwords.size - 1 - index).toLong())] = word
        }
    }

    /** Mirrors EU.gen_high_numwords (also reused by other latin languages). */
    fun genHighNumwords(units: List<String>, tens: List<String>, lows: List<String>): List<String> {
        val out = mutableListOf<String>()
        for (t in tens) for (u in units) out.add(u + t)
        out.reverse()
        return out + lows
    }

    // -- splitting -----------------------------------------------------------

    fun splitNum(value: BigInt): List<SplitNode> {
        for (elem in cards.keys) {
            if (elem > value) continue
            val out = mutableListOf<SplitNode>()
            val div: BigInt
            val mod: BigInt
            if (value.isZero()) {
                div = BigInt.ONE
                mod = BigInt.ZERO
            } else {
                val (d, m) = value.divmod(elem)
                div = d
                mod = m
            }
            if (div == BigInt.ONE) {
                out.add(SplitNode.Word(requireNotNull(cards[BigInt.ONE]), BigInt.ONE))
            } else {
                if (div == value) { // tally system, e.g. Roman numerals
                    return listOf(
                        SplitNode.Word(
                            div.toString() + requireNotNull(cards[elem]),
                            div * elem,
                        ),
                    )
                }
                out.add(SplitNode.Branch(splitNum(div)))
            }
            out.add(SplitNode.Word(requireNotNull(cards[elem]), elem))
            if (!mod.isZero()) out.add(SplitNode.Branch(splitNum(mod)))
            return out
        }
        throw NoSuchElementException("No card for $value")
    }

    fun parseMinus(numStr: String): Pair<String, String> {
        if (numStr.startsWith("-")) {
            return "${negword.trim()} " to numStr.substring(1)
        }
        return "" to numStr
    }

    fun strToNumber(value: String): SimpleDecimal = SimpleDecimal.fromString(value)

    // -- cardinal ------------------------------------------------------------

    open fun toCardinal(value: NumValue): String = when (value) {
        is NumValue.Whole -> toCardinalWhole(value.v)
        is NumValue.Decimal -> {
            val w = integerIfWhole(value.v)
            if (w != null) toCardinalWhole(w) else toCardinalFloat(value.v)
        }

        is NumValue.FloatVal -> {
            // base.py: assert int(value) == value -> integral floats take the whole path.
            val d = value.v
            if (d % 1.0 == 0.0) toCardinalWhole(doubleToBigInt(d))
            else toCardinalFloat(SimpleDecimal.fromDouble(d))
        }
    }

    private fun toCardinalWhole(value: BigInt): String {
        var out = ""
        var v = value
        if (v.sign() < 0) {
            v = -v
            out = "${negword.trim()} "
        }
        val max = maxVal
        if (max != null && v >= max) {
            throw Num2WordsOverflowError(fmt(errmsgToobig, value.toString(), max.toString()))
        }
        val (words, _) = clean(SplitNode.Branch(splitNum(v)))
        return title(out + words)
    }

    /**
     * Mirrors base.py float2tuple: precision is the fraction digit count of the
     * decimal form (no strip); post is the exact truncated fraction, because
     * Python's round(post) only corrects float noise toward the exact value,
     * which exact decimal arithmetic already holds (e.g. 0.29*100 -> 29,
     * -9.99 -> pre=-9, post=99).
     */
    fun float2Tuple(value: SimpleDecimal): Pair<BigInt, BigInt> {
        val expDigits = value.fractionDigits()
        precision = expDigits
        val pre = value.toBigInt()
        if (expDigits == 0) return pre to BigInt.ZERO
        val p10 = BigInt.pow10(expDigits)
        val post = value.unscaled.abs().divmod(p10).second
        return pre to post
    }

    open fun toCardinalFloat(value: SimpleDecimal): String {
        val (pre, post) = float2Tuple(value)
        return joinCardinalFloat(pre, post, precision, value.unscaled.sign() < 0)
    }

    /** Renders float parts; languages with per-call options (e.g. ZH reading) override this. */
    protected open fun joinCardinalFloat(pre: BigInt, post: BigInt, prec: Int, negative: Boolean): String {
        var postStr = post.toString()
        postStr = "0".repeat(maxOf(0, prec - postStr.length)) + postStr
        val out = mutableListOf(toCardinal(NumValue.Whole(pre)))
        if (negative && pre.isZero()) {
            out.add(0, negword.trim())
        }
        if (prec != 0) out.add(title(pointword))
        for (i in 0 until prec) {
            out.add(toCardinal(NumValue.Whole(BigInt.fromLong((postStr[i] - '0').toLong()))).toString())
        }
        return out.joinToString(" ")
    }

    open fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> =
        throw Num2WordsNotImplemented("merge")

    fun clean(node: SplitNode): Pair<String, BigInt> {
        if (node is SplitNode.Word) return node.text to node.num
        var current = (node as SplitNode.Branch).children.toMutableList()
        while (current.size != 1) {
            val left = current[0]
            val right = current[1]
            if (left is SplitNode.Word && right is SplitNode.Word) {
                val merged = merge(left.text to left.num, right.text to right.num)
                current = (listOf(SplitNode.Word(merged.first, merged.second)) + current.drop(2))
                    .toMutableList()
                continue
            }
            val out = mutableListOf<SplitNode>()
            for (elem in current) {
                if (elem is SplitNode.Branch) {
                    out.add(
                        if (elem.children.size == 1) elem.children[0]
                        else clean(elem).let { SplitNode.Word(it.first, it.second) },
                    )
                } else {
                    out.add(elem)
                }
            }
            current = out
        }
        val last = current[0]
        return if (last is SplitNode.Word) last.text to last.num else clean(last)
    }

    fun title(value: String): String {
        if (!isTitle) return value
        return value.split(" ").joinToString(" ") { word ->
            if (word in excludeTitle || word.isEmpty()) word
            else word[0].uppercaseChar() + word.substring(1)
        }
    }

    fun verifyOrdinal(value: NumValue) {
        val whole = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> integerIfWhole(value.v)
                ?: throw Num2WordsValueError(fmt(errmsgFloatord, value.v.toPlainString()))

            is NumValue.FloatVal -> {
                val d = value.v
                if (d % 1.0 != 0.0) throw Num2WordsValueError(fmt(errmsgFloatord, trimDouble(d)))
                doubleToBigInt(d)
            }
        }
        if (whole.sign() < 0) throw Num2WordsValueError(fmt(errmsgNegord, whole.toString()))
    }

    open fun toOrdinal(value: NumValue): String = toCardinal(value)

    open fun toOrdinalNum(value: NumValue): Any = when (value) {
        is NumValue.Whole -> value.v
        is NumValue.Decimal -> value.v
        is NumValue.FloatVal -> value.v
    }

    open fun inflect(value: BigInt, text: String): String {
        val parts = text.split("/")
        return if (value == BigInt.ONE) parts[0] else parts.joinToString("")
    }

    open fun toSplitNum(
        value: Any,
        hightxt: String = "",
        lowtxt: String = "",
        jointxt: String = "",
        divisor: Long = 100,
        longval: Boolean = true,
        cents: Boolean = true,
    ): String {
        val out = mutableListOf<String>()
        val high: BigInt
        val low: BigInt
        if (value is Double && value % 1.0 != 0.0) {
            val (h, l) = float2Tuple(SimpleDecimal.fromDouble(value))
            high = h
            low = l
        } else if (value is Double) {
            val (h, l) = doubleToBigInt(value).divmod(BigInt.fromLong(divisor))
            high = h
            low = l
        } else if (value is Pair<*, *>) {
            high = value.first as BigInt
            low = value.second as BigInt
        } else {
            val (h, l) = (value as BigInt).divmod(BigInt.fromLong(divisor))
            high = h
            low = l
        }
        if (!high.isZero()) {
            val htxt = title(inflect(high, hightxt))
            out.add(toCardinal(NumValue.Whole(high)))
            if (!low.isZero()) {
                if (longval) {
                    if (htxt.isNotEmpty()) out.add(htxt)
                    if (jointxt.isNotEmpty()) out.add(title(jointxt))
                }
            } else if (htxt.isNotEmpty()) {
                out.add(htxt)
            }
        }
        if (!low.isZero()) {
            out.add(if (cents) toCardinal(NumValue.Whole(low)) else pad2(low))
            if (lowtxt.isNotEmpty() && longval) out.add(title(inflect(low, lowtxt)))
        }
        return out.joinToString(" ")
    }

    open fun toYear(value: NumValue, suffix: String? = null, longval: Boolean = true): String =
        toCardinal(value)

    open fun pluralize(n: BigInt, forms: List<String>): String =
        throw Num2WordsNotImplemented("pluralize")

    open fun moneyVerbose(number: BigInt, currency: String): String =
        toCardinal(NumValue.Whole(number))

    open fun centsVerbose(number: BigInt, currency: String): String =
        toCardinal(NumValue.Whole(number))

    open fun centsTerse(number: BigInt, currency: String): String = pad2(number)

    open fun toCurrency(
        value: NumValue,
        currency: String = "EUR",
        cents: Boolean = true,
        separator: String = ",",
        adjective: Boolean = false,
    ): String {
        val (left, right, isNegative) = when (value) {
            is NumValue.Whole -> parseCurrencyParts(value.v, true)
            is NumValue.Decimal -> parseCurrencyParts(value.v, true)
            is NumValue.FloatVal -> parseCurrencyParts(value.v, true)
        }
        var (cr1, cr2) = currencyForms[currency]
            ?: throw Num2WordsNotImplemented(
                "Currency code \"$currency\" not implemented for \"${this::class.simpleName}\"",
            )
        if (adjective) {
            currencyAdjectives[currency]?.let { adj -> cr1 = prefixCurrency(adj, cr1) }
        }
        val minusStr = if (isNegative) "${negword.trim()} " else ""
        val moneyStr = moneyVerbose(left, currency)
        // Python: isinstance(val, float) is enough (floats always show cents).
        // Kotlin/JS Double.toString() drops ".0", so use trimDouble (Python str).
        val hasDecimal = when (value) {
            is NumValue.FloatVal -> true
            is NumValue.Decimal -> value.v.hasPoint
            is NumValue.Whole -> false
        }
        val rightBig = BigInt.fromLong(right.toLong())
        return if (hasDecimal || right > 0) {
            val centsStr = if (cents) centsVerbose(rightBig, currency)
            else centsTerse(rightBig, currency)
            "$minusStr$moneyStr ${pluralize(left, cr1)}$separator $centsStr ${pluralize(rightBig, cr2)}"
        } else {
            "$minusStr$moneyStr ${pluralize(left, cr1)}"
        }
    }

    open fun setup() {}
}
