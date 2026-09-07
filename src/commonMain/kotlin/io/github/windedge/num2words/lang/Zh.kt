package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordBase
import io.github.windedge.num2words.Num2WordsNotImplemented
import io.github.windedge.num2words.Num2WordsOverflowError
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.checkValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.parseCurrencyParts
import io.github.windedge.num2words.trimDouble

/** Chinese text alternatives: plain string, hanzi multi-pick, or (hanzi, reading) pair. */
sealed interface ZhText {
    data class Plain(val s: String) : ZhText
    /** Multiple hanzi forms, e.g. ("零", "〇"): prefer wins, else the first. */
    data class Alt(val options: List<String>) : ZhText
    /** (hanzi forms, reading forms), e.g. (("十", "拾"), ("ㄕˊ",)): reading picks the side. */
    data class ReadAlt(val hanzi: List<String>, val reading: List<String>) : ZhText
}

/** Per-call options, mirroring the (stuff_zero, reading, prefer) kwargs. */
data class ZhOptions(
    val stuffZero: Int = 2,
    val reading: Any? = false,
    val prefer: Set<String>? = null,
)

/** Mirrors num2words/lang_ZH.py Num2Word_ZH. */
open class Num2WordZh : Num2WordBase() {
    open val currencyFloats: List<String> = listOf("角", "分")

    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "XXX" to (listOf("元") to listOf("元")),
        "CNY" to (listOf("人民幣") to listOf("人民幣")),
        "NTD" to (listOf("新台幣") to listOf("新台幣")),
        "HKD" to (listOf("港幣") to listOf("港幣")),
        "MOP" to (listOf("澳門幣") to listOf("澳門幣")),
        "SGD" to (listOf("新加坡元") to listOf("新加坡元")),
        "MYR" to (listOf("馬來西亞令吉") to listOf("馬來西亞令吉")),
        "USD" to (listOf("美元") to listOf("美元")),
        "EUR" to (listOf("歐元") to listOf("歐元")),
        "GBP" to (listOf("英鎊") to listOf("英鎊")),
        "JPY" to (listOf("日元") to listOf("日元")),
        "CHF" to (listOf("瑞士法郎") to listOf("瑞士法郎")),
        "CAD" to (listOf("加元") to listOf("加元")),
        "AUD" to (listOf("澳幣") to listOf("澳幣")),
        "NZD" to (listOf("紐西蘭元") to listOf("紐西蘭元")),
        "THB" to (listOf("泰銖") to listOf("泰銖")),
        "KRW" to (listOf("韓元") to listOf("韓元")),
    )

    open val chequeSuffix: ZhText = ZhText.Plain("正")
    open val yearWord: ZhText = ZhText.Plain("年")
    open val yearPrefix: ZhText = ZhText.Plain("")
    open val yearPrefixAlt: List<String> = listOf("公元", "西元")
    open val yearBce: ZhText = ZhText.Plain("前")
    open val ordPrefix: ZhText = ZhText.Plain("第")

    open val capMap: List<Pair<String, String>> = listOf(
        "千" to "仟", "百" to "佰", "十" to "拾",
        "九" to "玖", "八" to "捌", "七" to "柒",
        "六" to "陸", "五" to "伍", "四" to "肆",
        "三" to "叁", "二" to "貳", "一" to "壹",
        "元" to "圓", "正" to "整",
    )

    var stuffZero: Int = 2
    var reading: Any? = false
    var prefer: Set<String>? = null
    var capital: Boolean = false
    var errmsgFloatyear: String = "Cannot treat float %s as year."

    override fun setup() {
        precision = 2
        negword = "負"
        pointword = "點"
        excludeTitle.clear()
        excludeTitle.addAll(listOf(negword, pointword))
        reading = null
        prefer = null
        val high = listOf(
            "萬", "億", "兆", "京", "垓", "秭", "穣", "溝",
            "澗", "正", "載", "極", "恆河沙", "阿僧祇",
            "那由他", "不可思議", "無量", "不可說",
        ).reversed()
        useNumwords(
            high = high,
            mid = listOf(
                BigInt.fromLong(1000) to "千",
                BigInt.fromLong(100) to "百",
                BigInt.fromLong(10) to "十",
            ),
            // low 0..9 in descending construction order; zero is registered
            // separately in setLowNumwords to keep its (hanzi, alt) pair.
            low = listOf("九", "八", "七", "六", "五", "四", "三", "二", "一", "零"),
        )
        // Zero is a (hanzi, alt) pair: ("零", "〇"); select_text picks per reading/prefer.
        zeroAlt = ZhText.Alt(listOf("零", "〇"))
    }

    override fun setLowNumwords(numwords: List<String>) {
        super.setLowNumwords(numwords)
        // Cards keep the default hanzi form; live selection happens via zeroCard()/digitCard().
        cards[BigInt.ZERO] = "零"
    }

    /** Zero text honoring the current reading/prefer selection. */
    open fun zeroCard(): String = selectText(zeroAlt)

    /** Single digit card honoring the current reading/prefer selection. */
    open fun digitCard(d: Int): String =
        if (d == 0) zeroCard() else selectTextRaw(requireNotNull(cards[BigInt.fromLong(d.toLong())]))

    var zeroAlt: ZhText = ZhText.Alt(listOf("零", "〇"))

    override fun setHighNumwords(high: List<String>) {
        val max = 4 * high.size
        var n = max
        for (word in high) {
            cards[BigInt.pow10(n)] = word
            n -= 4
        }
    }

    // -- text selection ------------------------------------------------------

    fun selectText(text: ZhText): String = when (text) {
        is ZhText.Plain -> text.s
        is ZhText.Alt -> pickOne(text.options)
        is ZhText.ReadAlt -> pickOne(if (reading == true) text.reading else text.hanzi)
    }

    private fun pickOne(options: List<String>): String {
        if (options.isEmpty()) return ""
        if (options.size == 1) return options[0]
        val common = options.filter { prefer?.contains(it) == true }
        return if (common.size == 1) common[0] else options[0]
    }

    fun selectTextRaw(text: Any?): String = when (text) {
        is String -> text
        is ZhText -> selectText(text)
        is Pair<*, *> -> pickOne(listOf(text.first.toString(), text.second.toString()))
        is List<*> -> {
            @Suppress("UNCHECKED_CAST")
            val opts = (text as List<String>)
            if (opts.isEmpty()) "" else pickOne(opts)
        }

        else -> text.toString()
    }

    fun setStrSelection(reading: Any?, prefer: Set<String>?) {
        this.reading = reading
        this.prefer = prefer
        this.capital = reading == "capital"
    }

    // -- cardinal ------------------------------------------------------------

    open fun toCardinalZh(value: NumValue, options: ZhOptions = ZhOptions()): String {
        stuffZero = options.stuffZero
        setStrSelection(options.reading, options.prefer)
        val out = super.toCardinal(value).replace(" ", "")
        return zhToCap(out, options.reading == "capital")
    }

    override fun toCardinal(value: NumValue): String =
        toCardinalZh(value, ZhOptions())

    override fun toCardinalFloat(value: SimpleDecimal): String {
        val (pre, post) = float2Tuple(value)
        var postStr = post.toString()
        postStr = "0".repeat(maxOf(0, precision - postStr.length)) + postStr
        val out = mutableListOf(toCardinal(NumValue.Whole(pre)))
        if (value.unscaled.sign() < 0 && pre.isZero()) {
            out.add(0, selectTextRaw(negword).trim())
        }
        if (precision != 0) out.add(title(selectTextRaw(pointword)))
        for (i in 0 until precision) {
            out.add(toCardinal(NumValue.Whole(BigInt.fromLong((postStr[i] - '0').toLong()))).toString())
        }
        return zhToCap(out.joinToString(" "), capital).replace(" ", "")
    }

    override fun joinCardinalFloat(pre: BigInt, post: BigInt, prec: Int, negative: Boolean): String =
        throw Num2WordsNotImplemented("Use toCardinalFloat(SimpleDecimal) for ZH")

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        var (ltext, lnum) = left
        var (rtext, rnum) = right
        ltext = selectTextRaw(ltext)
        rtext = selectTextRaw(rtext)
        if (lnum == BigInt.ONE && rnum < BigInt.fromLong(10)) return rtext to rnum
        val zero = zeroCard()
        val withZero = "$ltext$zero$rtext" to lnum + rnum
        val noZero = "$ltext$rtext" to lnum + rnum
        val ten = BigInt.fromLong(10)
        if (digitLen(lnum) - digitLen(rnum) > 1) {
            return when (stuffZero) {
                1 -> withZero
                2 -> {
                    if (digitLen(lnum) - digitLen(rnum) > 1 && digitLen(rnum) % 4 != 0) withZero
                    else noZero
                }

                3 -> noZero
                else -> noZero
            }
        } else if (rnum > lnum) {
            return "$ltext$rtext" to lnum * rnum
        }
        return noZero
    }

    private fun digitLen(v: BigInt): Int = v.abs().toString().length

    // -- ordinal / year ------------------------------------------------------

    open fun toOrdinalZh(value: NumValue, counter: String = "", options: ZhOptions = ZhOptions()): String {
        setStrSelection(options.reading, options.prefer)
        verifyOrdinal(value)
        val base = toCardinalZh(value, options)
        return selectText(ordPrefix) + base + selectTextRaw(counter)
    }

    override fun toOrdinal(value: NumValue): String = toOrdinalZh(value)

    open fun toOrdinalNumZh(value: NumValue, counter: String = "", options: ZhOptions = ZhOptions()): Any {
        setStrSelection(options.reading, options.prefer)
        return selectText(ordPrefix) + wholeTextOf(value) + selectTextRaw(counter)
    }

    override fun toOrdinalNum(value: NumValue): Any = toOrdinalNumZh(value)

    open fun toYearZh(value: NumValue, options: ZhOptions = ZhOptions()): String {
        setStrSelection(options.reading, options.prefer)
        val whole = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> integerIfWhole(value.v)
                ?: throw Num2WordsValueError(fmt(errmsgFloatyear, value.v.toPlainString()))

            is NumValue.FloatVal -> {
                if (value.v % 1.0 != 0.0) throw Num2WordsValueError(fmt(errmsgFloatyear, trimDouble(value.v)))
                doubleToBigInt(value.v)
            }
        }
        val out = mutableListOf<ZhText>()
        if (whole.sign() < 0) {
            out.add(ZhText.Plain(selectYearPrefix()))
            out.add(yearBce)
        } else if (options.reading == "capital") {
            out.add(ZhText.Plain(selectYearPrefix()))
        }
        for (ch in whole.abs().toString()) {
            out.add(ZhText.Plain(digitCard(ch - '0')))
        }
        out.add(yearWord)
        return out.joinToString("") { selectText(it) }
    }

    private fun selectYearPrefix(): String {
        val common = yearPrefixAlt.filter { prefer?.contains(it) == true }
        return if (common.size == 1) common[0] else yearPrefixAlt[0]
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearZh(value)

    // -- currency ------------------------------------------------------------

    open fun toCurrencyZh(
        value: NumValue,
        currency: String = "XXX",
        options: ZhOptions = ZhOptions(),
    ): String {
        setStrSelection(options.reading, options.prefer)
        val raw: Any = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> value.v
            is NumValue.FloatVal -> value.v
        }
        val (left, right, isNegative) = parseCurrencyParts(raw, isIntWithCents = false)
        val cr = zhCurrencyForms()[currency]
            ?: throw Num2WordsNotImplemented(
                "Currency code \"$currency\" not implemented for \"${this::class.simpleName}\"",
            )
        val minusStr = if (isNegative) selectTextRaw(negword) else ""
        val moneyStr = toCardinalZh(NumValue.Whole(left), options)
        val (crPre, crPost) = if (currency == "XXX") "" to selectTextRaw(cr) else selectTextRaw(cr) to zhBaseUnit()
        val centsStr = toCurrencyFloatZh(right.toLong(), options)
        val cheque = if (centsStr.isEmpty() && options.reading == "capital") selectText(chequeSuffix) else ""
        var out = crPre
        for (c in listOf(minusStr, moneyStr, crPost) + centsStr + listOf(cheque)) {
            out += zhToCap(selectTextRaw(c), options.reading == "capital")
        }
        return out
    }

    protected open fun zhCurrencyForms(): Map<String, Any> =
        currencyForms.mapValues { it.value.first.first() as Any }

    protected open fun zhBaseUnit(): String = selectTextRaw(
        (currencyForms["XXX"]?.first?.firstOrNull() ?: "元"),
    )

    open fun toCurrencyFloatZh(value: Long, options: ZhOptions = ZhOptions()): List<String> {
        val cents = value.toString().padStart(2, '0').takeLast(2)
        checkValue(cents.length == 2 && cents.all { it in '0'..'9' }) { "Bad cents: $value" }
        val out = mutableListOf<String>()
        if (cents.toInt() > 0) {
            if (!(cents[0] == '0' && options.reading == "capital")) {
                out.add(digitCard(cents[0] - '0'))
            }
            if (cents[0] != '0') out.add(currencyFloats[0])
            if (cents[1] != '0') {
                out.add(digitCard(cents[1] - '0'))
                out.add(currencyFloats[1])
            }
        }
        return out
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyZh(value, currency)

    // -- capital -------------------------------------------------------------

    fun zhToCap(value: String, capital: Boolean): String {
        var out = value
        val one = digitCard(1)
        val ten = digitCard(10)
        if (capital) {
            for ((plain, cap) in capMap) {
                if (plain in out) out = out.replace(plain, cap)
            }
            return out
        } else if (out.startsWith(one + ten)) {
            out = out.substring(one.length)
        }
        return out
    }

    private fun wholeTextOf(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }
}
