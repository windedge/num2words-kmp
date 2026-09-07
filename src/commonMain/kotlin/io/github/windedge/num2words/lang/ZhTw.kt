package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordsNotImplemented
import io.github.windedge.num2words.Num2WordsOverflowError
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.SplitNode
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

private fun alt(hanzi: String, reading: String): ZhText.ReadAlt =
    ZhText.ReadAlt(listOf(hanzi), listOf(reading))

private fun altMany(vararg pairs: Pair<String, String>): ZhText.ReadAlt =
    ZhText.ReadAlt(pairs.map { it.first }, pairs.map { it.second })

/** Mirrors num2words/lang_ZH_TW.py Num2Word_ZH_TW. */
class Num2WordZhTw : Num2WordZh() {
    override val chequeSuffix: ZhText = alt("正", "ㄓㄥˋ")
    override val yearWord: ZhText = alt("年", "ㄋㄧㄢˊ")
    override val yearPrefixAlt: List<String> = listOf("公元", "西元")
    override val yearBce: ZhText = alt("前", "ㄑㄧㄢˊ")
    override val ordPrefix: ZhText = alt("第", "ㄉㄧˋ")
    private val rocEra: ZhText = alt("民國", "ㄇㄧㄣˊㄍㄨㄛˊ")

    val counters: Map<String, String> = mapOf(
        "個" to "˙ㄍㄜ",
        "名" to "ㄇㄧㄥˊ",
        "位" to "ㄨㄟˋ",
    )

    // Setup assigns the same values again; defaults here only guard init order.
    private var negwordText: ZhText = alt("負", "ㄈㄨˋ")
    private var pointwordText: ZhText = alt("點", "ㄉㄧㄢˇ")

    /** Phonetic cards, mirroring the tuple values Python keeps in cards. Fixed table, no init-order risk. */
    private val readingCards: Map<BigInt, String> = buildReadingCards()
    private var readingZero: String = "ㄌㄧㄥˊ"

    companion object {
        private val HIGH_READING: List<String> = listOf(
            "ㄨㄢˋ", "ㄧˋ", "ㄓㄠˋ", "ㄐㄧㄥ", "ㄍㄞ", "ㄗˇ",
            "ㄖㄤ", "ㄍㄡ", "ㄐㄧㄢˋ", "ㄓㄥˋ", "ㄗㄞˇ",
            "ㄐㄧˊ", "ㄏㄥˊㄏㄜˊㄕㄚ", "ㄚㄙㄥㄑㄧˊ",
            "ㄋㄚˋㄧㄡˊㄊㄚ", "ㄅㄨˋㄎㄜˇㄙㄧˋ",
            "ㄨˊㄌㄧㄤˋ", "ㄅㄨˋㄎㄜˇㄕㄨㄛ",
        ).reversed()

        private val LOW_READING: Map<Int, String> = mapOf(
            10 to "ㄕˊ", 9 to "ㄐㄧㄡˇ", 8 to "ㄅㄚ", 7 to "ㄑㄧ",
            6 to "ㄌㄧㄡˋ", 5 to "ㄨˇ", 4 to "ㄙˋ", 3 to "ㄙㄢ",
            2 to "ㄦˋ", 1 to "ㄧ",
        )

        private fun buildReadingCards(): Map<BigInt, String> {
            val map = LinkedHashMap<BigInt, String>()
            var n = 4 * HIGH_READING.size
            for (word in HIGH_READING) {
                map[BigInt.pow10(n)] = word
                n -= 4
            }
            map[BigInt.fromLong(1000)] = "ㄑㄧㄢ"
            map[BigInt.fromLong(100)] = "ㄅㄞˇ"
            for ((d, r) in LOW_READING) map[BigInt.fromLong(d.toLong())] = r
            map[BigInt.ZERO] = "ㄌㄧㄥˊ"
            return map
        }
    }

    override fun setup() {
        super.setup()
        negword = "負"
        pointword = "點"
        negwordText = alt("負", "ㄈㄨˋ")
        pointwordText = alt("點", "ㄉㄧㄢˇ")
        excludeTitle.clear()
        excludeTitle.addAll(listOf(negword, pointword))

        val highHanzi = listOf(
            "萬", "億", "兆", "京", "垓", "秭", "穣", "溝",
            "澗", "正", "載", "極", "恆河沙", "阿僧祇",
            "那由他", "不可思議", "無量", "不可說",
        ).reversed()
        useNumwords(
            high = highHanzi,
            mid = listOf(
                BigInt.fromLong(1000) to "千",
                BigInt.fromLong(100) to "百",
            ),
            low = listOf("十", "九", "八", "七", "六", "五", "四", "三", "二", "一", "零"),
        )
        negwordText = alt("負", "ㄈㄨˋ")
        pointwordText = alt("點", "ㄉㄧㄢˇ")
        readingZero = "ㄌㄧㄥˊ"
    }

    private fun buildReadingCards(highReading: List<String>): MutableMap<BigInt, String> {
        val map = LinkedHashMap<BigInt, String>()
        var n = 4 * highReading.size
        for (word in highReading) {
            map[BigInt.pow10(n)] = word
            n -= 4
        }
        map[BigInt.fromLong(1000)] = "ㄑㄧㄢ"
        map[BigInt.fromLong(100)] = "ㄅㄞˇ"
        val lowReading = mapOf(
            10 to "ㄕˊ", 9 to "ㄐㄧㄡˇ", 8 to "ㄅㄚ", 7 to "ㄑㄧ",
            6 to "ㄌㄧㄡˋ", 5 to "ㄨˇ", 4 to "ㄙˋ", 3 to "ㄙㄢ",
            2 to "ㄦˋ", 1 to "ㄧ",
        )
        for ((d, r) in lowReading) map[BigInt.fromLong(d.toLong())] = r
        return map
    }

    private fun cardReading(num: BigInt): String? = readingCards[num]

    override fun zhCurrencyForms(): Map<String, Any> {
        val base = super.zhCurrencyForms().toMutableMap()
        base["XXX"] = alt("元", "ㄩㄢˊ")
        return base
    }

    // -- cardinal (reading/prefer threaded through float digits, like Python) --

    fun toCardinalTw(value: NumValue, options: ZhOptions = ZhOptions()): String {
        stuffZero = options.stuffZero
        setStrSelection(options.reading, options.prefer)
        val whole = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> integerIfWhole(value.v)
            is NumValue.FloatVal -> {
                val d = value.v
                if (d % 1.0 == 0.0) doubleToBigInt(d) else null
            }
        }
        if (whole == null) {
            val dec = when (value) {
                is NumValue.Decimal -> value.v
                is NumValue.FloatVal -> SimpleDecimal.fromDouble(value.v)
                else -> throw Num2WordsValueError(fmt(errmsgNonnum, value.toString()))
            }
            return toCardinalFloatTw(dec, options)
        }
        var out = ""
        var v = whole
        if (v.sign() < 0) {
            v = -v
            out = "${selectText(negwordText).trim()} "
        }
        val max = maxVal
        if (max != null && v >= max) {
            throw Num2WordsOverflowError(fmt(errmsgToobig, whole.toString(), max.toString()))
        }
        val (words, _) = clean(SplitNode.Branch(splitNum(v)))
        out = title(out + words)
        return zhToCap(out, options.reading == "capital").replace(" ", "")
    }

    fun toCardinalFloatTw(value: SimpleDecimal, options: ZhOptions = ZhOptions()): String {
        setStrSelection(options.reading, options.prefer)
        val (pre, post) = float2Tuple(value)
        var postStr = post.toString()
        postStr = "0".repeat(maxOf(0, precision - postStr.length)) + postStr
        val out = mutableListOf(toCardinalTw(NumValue.Whole(pre), options))
        if (value.unscaled.sign() < 0 && pre.isZero()) {
            out.add(0, selectText(negwordText).trim())
        }
        if (precision != 0) out.add(selectText(ZhText.Plain(title(selectText(pointwordText)))))
        for (i in 0 until precision) {
            out.add(toCardinalTw(NumValue.Whole(BigInt.fromLong((postStr[i] - '0').toLong())), options))
        }
        return zhToCap(out.joinToString(" "), capital).replace(" ", "")
    }

    override fun toCardinal(value: NumValue): String = toCardinalTw(value)

    // convertZh dispatches here; route into the TW pipeline so reading/prefer apply.
    override fun toCardinalZh(value: NumValue, options: ZhOptions): String = toCardinalTw(value, options)

    override fun toCardinalFloat(value: SimpleDecimal): String = toCardinalFloatTw(value)

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        if (reading != true) return super.merge(left, right)
        // Only atomic card text maps by key; merged compounds already read phonetic.
        val l = if (left.first == cards[left.second]) cardReading(left.second) ?: left.first else left.first
        val r = if (right.first == cards[right.second]) cardReading(right.second) ?: right.first else right.first
        return super.merge(l to left.second, r to right.second)
    }

    override fun digitCard(d: Int): String {
        if (d == 0) return zeroCard()
        if (reading == true) return cardReading(BigInt.fromLong(d.toLong())) ?: super.digitCard(d)
        return super.digitCard(d)
    }

    override fun zeroCard(): String =
        if (reading == true) readingZero else selectText(zeroAlt)

    // -- ordinal -------------------------------------------------------------

    fun toOrdinalTw(value: NumValue, counter: String = "", options: ZhOptions = ZhOptions()): String {
        setStrSelection(options.reading, options.prefer)
        var c = counter
        if (options.reading == true) {
            if (c.isNotEmpty() && !counters.containsKey(c)) {
                throw Num2WordsNotImplemented("Reading not implemented for $c")
            }
            c = counters[c] ?: ""
        }
        return super.toOrdinalZh(value, c, options)
    }

    override fun toOrdinal(value: NumValue): String = toOrdinalTw(value)

    override fun toOrdinalZh(value: NumValue, counter: String, options: ZhOptions): String =
        toOrdinalTw(value, counter, options)

    fun toOrdinalNumTw(value: NumValue, counter: String = "", options: ZhOptions = ZhOptions()): Any {
        setStrSelection(options.reading, options.prefer)
        var c = counter
        if (reading == true) {
            if (c.isNotEmpty() && !counters.containsKey(c)) {
                throw Num2WordsNotImplemented("Reading not implemented for $c")
            }
            c = counters[c] ?: ""
        }
        return super.toOrdinalNumZh(value, c, options)
    }

    override fun toOrdinalNum(value: NumValue): Any = toOrdinalNumTw(value)

    override fun toOrdinalNumZh(value: NumValue, counter: String, options: ZhOptions): Any =
        toOrdinalNumTw(value, counter, options)

    // -- year ----------------------------------------------------------------

    fun toYearTw(value: NumValue, era: Boolean = false, options: ZhOptions = ZhOptions()): String {
        setStrSelection(options.reading, options.prefer)
        if (!era) return super.toYearZh(value, options)
        val whole = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> integerIfWhole(value.v)
                ?: throw Num2WordsValueError(fmt(errmsgFloatyear, value.v.toPlainString()))

            is NumValue.FloatVal -> {
                if (value.v % 1.0 != 0.0) throw Num2WordsValueError(fmt(errmsgFloatyear, trimDouble(value.v)))
                doubleToBigInt(value.v)
            }
        }
        val minYear = BigInt.fromLong(1912)
        if (whole < minYear) {
            throw Num2WordsValueError("Can't convert years less than 1912 to ROC era")
        }
        val eraYear = (whole - minYear + BigInt.ONE).abs()
        val eraYearWords: Any = if (options.reading == "arabic") {
            eraYear.toString()
        } else if (eraYear == BigInt.ONE) {
            selectText(alt("元", "ㄩㄢˊ"))
        } else if (eraYear < BigInt.fromLong(101)) {
            toCardinalTw(NumValue.Whole(eraYear), options)
        } else {
            eraYear.toString().map { digitCard(it - '0') }.joinToString("")
        }
        return selectText(rocEra) + eraYearWords.toString() + selectText(yearWord)
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearTw(value)

    override fun toYearZh(value: NumValue, options: ZhOptions): String =
        toYearTw(value, era = false, options = options)
}
