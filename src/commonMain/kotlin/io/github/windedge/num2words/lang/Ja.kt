package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordBase
import io.github.windedge.num2words.Num2WordsNotImplemented
import io.github.windedge.num2words.Num2WordsOverflowError
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.SplitNode
import io.github.windedge.num2words.checkValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.parseCurrencyParts
import io.github.windedge.num2words.trimDouble

/** Japanese text: (kanji forms, reading forms), mirroring the tuple cards. */
data class JaText(val kanji: List<String>, val reading: List<String>)

data class JaOptions(
    val reading: Any? = false,
    val prefer: Set<String>? = null,
    val counter: String = "番",
    val era: Boolean = true,
)

/** Mirrors num2words/lang_JA.py Num2Word_JA. */
open class Num2WordJa : Num2WordBase() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "JPY" to (listOf("円") to emptyList()),
    )

    var reading: Any? = false
    var prefer: Set<String>? = null

    override fun setup() {
        negword = "マイナス"
        pointword = "点"
        excludeTitle.clear()
        excludeTitle.addAll(listOf("点", "マイナス"))
        reading = false
        prefer = null
        useNumwords(
            high = listOf(
                "万", "億", "兆", "京", "垓", "秭", "穣", "溝",
                "澗", "正", "載", "極",
            ).reversed(),
            mid = listOf(
                BigInt.fromLong(1000) to "千",
                BigInt.fromLong(100) to "百",
            ),
            low = listOf("十", "九", "八", "七", "六", "五", "四", "三", "二", "一", "零"),
        )
    }

    override fun setHighNumwords(high: List<String>) {
        val max = 4 * high.size
        var n = max
        for (word in high) {
            cards[BigInt.pow10(n)] = word
            n -= 4
        }
    }

    // -- text tables (fixed, no init-order risk) --------------------------------

    companion object {
        private fun jt(vararg kanji: String, reading: String): JaText =
            JaText(kanji.toList(), listOf(reading))

        private fun jtMulti(kanji: List<String>, reading: List<String>): JaText =
            JaText(kanji, reading)

        val jaKanjiReading: Map<BigInt, String> = buildMap {
            put(BigInt.fromLong(1000), "千")
            put(BigInt.fromLong(100), "百")
            put(BigInt.fromLong(10), "十")
            put(BigInt.fromLong(9), "九")
            put(BigInt.fromLong(8), "八")
            put(BigInt.fromLong(7), "七")
            put(BigInt.fromLong(6), "六")
            put(BigInt.fromLong(5), "五")
            put(BigInt.fromLong(4), "四")
            put(BigInt.fromLong(3), "三")
            put(BigInt.fromLong(2), "二")
            put(BigInt.fromLong(1), "一")
        }

        val jaReading: Map<BigInt, String> = buildMap {
            put(BigInt.fromLong(1000), "せん")
            put(BigInt.fromLong(100), "ひゃく")
            put(BigInt.fromLong(10), "じゅう")
            put(BigInt.fromLong(9), "きゅう")
            put(BigInt.fromLong(8), "はち")
            put(BigInt.fromLong(7), "なな")
            put(BigInt.fromLong(6), "ろく")
            put(BigInt.fromLong(5), "ご")
            put(BigInt.fromLong(4), "よん")
            put(BigInt.fromLong(3), "さん")
            put(BigInt.fromLong(2), "に")
            put(BigInt.fromLong(1), "いち")
        }

        val jaHighReading: Map<BigInt, String> = buildMap {
            val kanji = listOf("万", "億", "兆", "京", "垓", "秭", "穣", "溝", "澗", "正", "載", "極")
            val reading = listOf(
                "まん", "おく", "ちょう", "けい", "がい", "し",
                "じょう", "こう", "かん", "せい", "さい", "ごく",
            )
            var n = 4
            for ((k, r) in kanji.zip(reading)) {
                put(BigInt.pow10(n), r)
                n += 4
            }
        }
    }

    // -- selection ---------------------------------------------------------------

    fun selectJa(text: JaText): String {
        val side = if (reading == true) text.reading else text.kanji
        if (side.size == 1) return side[0]
        val common = side.filter { prefer?.contains(it) == true }
        return if (common.size == 1) common[0] else side[0]
    }

    fun selectJaRaw(text: Any?): String = when (text) {
        is String -> text
        is JaText -> selectJa(text)
        is Pair<*, *> -> {
            val a = text.first.toString()
            val b = text.second.toString()
            val common = listOf(a, b).filter { prefer?.contains(it) == true }
            if (common.size == 1) common[0] else a
        }

        is List<*> -> {
            @Suppress("UNCHECKED_CAST")
            val opts = text as List<String>
            if (opts.isEmpty()) "" else {
                val common = opts.filter { prefer?.contains(it) == true }
                if (common.size == 1) common[0] else opts[0]
            }
        }

        else -> text.toString()
    }

    fun setSelection(reading: Any?, prefer: Set<String>?) {
        this.reading = reading
        this.prefer = prefer
    }

    private fun digitText(num: BigInt): JaText = when (num) {
        BigInt.ZERO -> JaText(listOf("零", "〇"), listOf("ゼロ", "れい"))
        BigInt.ONE -> JaText(listOf("一"), listOf("いち"))
        BigInt.fromLong(2) -> JaText(listOf("二"), listOf("に"))
        BigInt.fromLong(3) -> JaText(listOf("三"), listOf("さん"))
        BigInt.fromLong(4) -> JaText(listOf("四"), listOf("よん", "し"))
        BigInt.fromLong(5) -> JaText(listOf("五"), listOf("ご"))
        BigInt.fromLong(6) -> JaText(listOf("六"), listOf("ろく"))
        BigInt.fromLong(7) -> JaText(listOf("七"), listOf("なな", "しち"))
        BigInt.fromLong(8) -> JaText(listOf("八"), listOf("はち"))
        BigInt.fromLong(9) -> JaText(listOf("九"), listOf("きゅう"))
        BigInt.fromLong(10) -> JaText(listOf("十"), listOf("じゅう"))
        BigInt.fromLong(100) -> JaText(listOf("百"), listOf("ひゃく"))
        BigInt.fromLong(1000) -> JaText(listOf("千"), listOf("せん"))
        else -> JaText(listOf(requireNotNull(cards[num])), listOf(requireNotNull(jaHighReading[num])))
    }

    fun cardJa(num: BigInt): String = selectJa(digitText(num))

    // -- merge ---------------------------------------------------------------------

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        val (ltext, lnum) = left
        val (rtext, rnum) = right
        if (lnum == BigInt.ONE && rnum < BigInt.fromLong(10000)) return right
        if (lnum > rnum) return "$ltext$rtext" to lnum + rnum
        checkValue(lnum < rnum) { "rendaku merge requires lnum < rnum" }
        return rendakuMergePairs(ltext to lnum, rtext to rnum)
    }

    // -- cardinal --------------------------------------------------------------------

    fun toCardinalJa(value: NumValue, options: JaOptions = JaOptions()): String {
        setSelection(options.reading, options.prefer)
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
            return toCardinalFloatJa(dec, options)
        }
        var out = ""
        var v = whole
        if (v.sign() < 0) {
            v = -v
            out = negword
        }
        val max = maxVal
        if (max != null && v >= max) {
            throw Num2WordsOverflowError(fmt(errmsgToobig, whole.toString(), max.toString()))
        }
        val (words, _) = clean(SplitNode.Branch(splitNumJa(v, options)))
        return title(out + words)
    }

    fun splitNumJa(value: BigInt, options: JaOptions): List<SplitNode> {
        val out = mutableListOf<SplitNode>()
        for (elem in cards.keys) {
            if (elem > value) continue
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
                out.add(SplitNode.Word(selectJa(digitText(BigInt.ONE)), BigInt.ONE))
            } else {
                if (div == value) {
                    return listOf(
                        SplitNode.Word(
                            div.toString() + selectJa(digitText(elem)),
                            div * elem,
                        ),
                    )
                }
                out.add(SplitNode.Branch(splitNumJa(div, options)))
            }
            out.add(SplitNode.Word(selectJa(digitText(elem)), elem))
            if (!mod.isZero()) out.add(SplitNode.Branch(splitNumJa(mod, options)))
            return out
        }
        throw Num2WordsValueError("No card for $value")
    }

    fun toCardinalFloatJa(value: SimpleDecimal, options: JaOptions = JaOptions()): String {
        // Python: prefer = prefer or ["れい"] (unconditional).
        val opts = if (options.prefer == null) options.copy(prefer = setOf("れい")) else options
        setSelection(opts.reading, opts.prefer)
        val (pre, post) = float2Tuple(value)
        var postStr = post.toString()
        postStr = "0".repeat(maxOf(0, precision - postStr.length)) + postStr
        val out = mutableListOf(toCardinalJa(NumValue.Whole(pre), opts))
        if (precision != 0) out.add(title(selectJa(JaText(listOf("点"), listOf("てん")))))
        for (i in 0 until precision) {
            out.add(toCardinalJa(NumValue.Whole(BigInt.fromLong((postStr[i] - '0').toLong())), opts))
        }
        return out.joinToString("")
    }

    override fun toCardinal(value: NumValue): String = toCardinalJa(value)

    override fun toCardinalFloat(value: SimpleDecimal): String = toCardinalFloatJa(value)

    // -- ordinal -----------------------------------------------------------------------

    private fun ordinalSuffix(counter: String): String {
        if (reading == true) {
            if (counter == "番") return "ばんめ"
            throw Num2WordsNotImplemented("Reading not implemented for $counter")
        }
        return counter + "目"
    }

    fun toOrdinalJa(value: NumValue, options: JaOptions = JaOptions()): String {
        setSelection(options.reading, options.prefer)
        verifyOrdinal(value)
        val base = toCardinalJa(value, options)
        return base + ordinalSuffix(options.counter)
    }

    override fun toOrdinal(value: NumValue): String = toOrdinalJa(value)

    fun toOrdinalNumJa(value: NumValue, options: JaOptions = JaOptions()): Any {
        setSelection(options.reading, options.prefer)
        return wholeTextOf(value) + ordinalSuffix(options.counter)
    }

    override fun toOrdinalNum(value: NumValue): Any = toOrdinalNumJa(value)

    // -- year ----------------------------------------------------------------------------

    fun toYearJa(value: NumValue, options: JaOptions = JaOptions()): String {
        setSelection(options.reading, options.prefer)
        val whole = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> integerIfWhole(value.v)
                ?: throw Num2WordsValueError(fmt(errmsgFloatord, value.v.toPlainString()))

            is NumValue.FloatVal -> {
                if (value.v % 1.0 != 0.0) throw Num2WordsValueError(fmt(errmsgFloatord, trimDouble(value.v)))
                doubleToBigInt(value.v)
            }
        }
        if (!options.era) {
            var year = whole
            var prefix = ""
            if (year.sign() < 0) {
                year = -year
                prefix = if (options.reading == true) "きげんぜん" else "紀元前"
            }
            var yearWords = toCardinalJa(NumValue.Whole(year), options)
            if (options.reading == true && year % BigInt.fromLong(10) == BigInt.fromLong(9)) {
                yearWords = yearWords.dropLast(3) + "く"
            }
            return prefix + yearWords + (if (options.reading == true) "ねん" else "年")
        }
        val yearLong = whole.toLong()
        val minYear = JA_ERA_START.first().first
        if (whole < BigInt.fromLong(minYear)) {
            throw Num2WordsValueError("Can't convert years less than $minYear to era")
        }
        var first = 0
        var last = JA_ERA_START.size - 1
        var eraIdx = last
        while (true) {
            val mid = (first + last) / 2
            if (mid == JA_ERA_START.size - 1 ||
                (JA_ERA_START[mid].first <= yearLong && JA_ERA_START[mid + 1].first > yearLong)
            ) {
                eraIdx = mid
                if (options.prefer != null) {
                    var i = mid - 1
                    while (i >= 0 && JA_ERA_START[i].first == yearLong) {
                        if (JA_ERA_START[i].second.toList().any { options.prefer.contains(it) }) {
                            eraIdx = i
                            break
                        }
                        i--
                    }
                }
                break
            }
            if (yearLong < JA_ERA_START[mid].first) last = mid - 1
            else first = mid + 1
        }
        val era = JA_ERA_START[eraIdx]
        val eraYear = yearLong - era.first + 1
        return if (options.reading == "arabic") {
            "${era.second.first}${eraYear}年"
        } else if (options.reading == true) {
            var words = if (eraYear == 1L) "がん" else toCardinalJa(NumValue.Whole(BigInt.fromLong(eraYear)), options)
            if (eraYear % 10 == 9L) words = words.dropLast(3) + "く"
            "${era.second.second}${words}ねん"
        } else {
            val words = if (eraYear == 1L) "元" else toCardinalJa(NumValue.Whole(BigInt.fromLong(eraYear)), options)
            "${era.second.first}${words}年"
        }
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearJa(value)

    // -- currency --------------------------------------------------------------------------

    fun toCurrencyJa(
        value: NumValue,
        currency: String = "JPY",
        cents: Boolean = false,
        separator: String = "",
        adjective: Boolean = false,
        options: JaOptions = JaOptions(),
    ): String {
        setSelection(options.reading, options.prefer)
        val raw: Any = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> value.v
            is NumValue.FloatVal -> value.v
        }
        val (left, right, isNegative) = parseCurrencyParts(raw, isIntWithCents = cents)
        val (cr1, cr2) = currencyForms[currency]
            ?: throw Num2WordsNotImplemented(
                "Currency code \"$currency\" not implemented for \"${this::class.simpleName}\"",
            )
        val absVal: SimpleDecimal = when (value) {
            is NumValue.Whole -> SimpleDecimal.fromBigInt(value.v.abs())
            is NumValue.Decimal -> value.v.abs()
            is NumValue.FloatVal -> SimpleDecimal.fromDouble(if (value.v < 0) -value.v else value.v)
        }
        if ((cents || absVal.compareTo(SimpleDecimal.fromBigInt(left)) != 0) && cr2.isEmpty()) {
            throw Num2WordsValueError("Decimals not supported for \"$currency\"")
        }
        if (adjective) {
            currencyAdjectives[currency]?.let { adj ->
                throw Num2WordsNotImplemented("Adjective not implemented for JA")
            }
        }
        val minusStr = if (isNegative) negword else ""
        val rightBig = BigInt.fromLong(right.toLong())
        return minusStr +
            toCardinalJa(NumValue.Whole(left), options) +
            (if (options.reading == true) "えん" else cr1[0]) +
            (if (cr2.isNotEmpty()) toCardinalJa(NumValue.Whole(rightBig), options) else "") +
            (if (cr2.isNotEmpty()) (if (options.reading == true) cr2[1] else cr2[0]) else "")
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyJa(value, currency, cents, separator, adjective)

    private fun wholeTextOf(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }
}

/** Rendaku merge for lnum < rnum, mirroring rendaku_merge_pairs. */
fun rendakuMergePairs(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
    var (ltext, lnum) = left
    var (rtext, rnum) = right
    checkValue(lnum <= rnum) { "rendaku merge requires lnum <= rnum" }
    val p100 = BigInt.fromLong(100)
    val p1000 = BigInt.fromLong(1000)
    val p1e12 = BigInt.pow10(12)
    val p1e16 = BigInt.pow10(16)
    if (rnum == p100 && ltext == "さん" && lnum == BigInt.fromLong(3)) {
        if (rtext == "ひゃく") rtext = "びゃく"
    } else if (rnum == p100) {
        if (lnum == BigInt.fromLong(6) && ltext == "ろく") {
            ltext = "ろっ"
            if (rtext == "ひゃく") rtext = "ぴゃく"
        } else if (lnum == BigInt.fromLong(8) && ltext == "はち") {
            ltext = "はっ"
            if (rtext == "ひゃく") rtext = "ぴゃく"
        }
    } else if (rnum == p1000) {
        if (lnum == BigInt.fromLong(3) && ltext == "さん" && rtext == "せん") rtext = "ぜん"
        else if (lnum == BigInt.fromLong(8) && ltext == "はち" && rtext == "せん") ltext = "はっ"
    } else if (rnum == p1e12) {
        if (lnum == BigInt.ONE && ltext == "いち" && rtext == "ちょう") ltext = "いっ"
        else if (lnum == BigInt.fromLong(8) && ltext == "はち" && rtext == "ちょう") ltext = "はっ"
        else if (lnum == BigInt.fromLong(10) && ltext == "じゅう" && rtext == "ちょう") ltext = "じゅっ"
    } else if (rnum == p1e16) {
        if (lnum == BigInt.ONE && ltext == "いち" && rtext == "けい") ltext = "いっ"
        else if (lnum == BigInt.fromLong(6) && ltext == "ろく" && rtext == "けい") ltext = "ろっ"
        else if (lnum == BigInt.fromLong(8) && ltext == "はち" && rtext == "けい") ltext = "はっ"
        else if (lnum == BigInt.fromLong(10) && ltext == "じゅう" && rtext == "けい") ltext = "じゅっ"
        else if (lnum == p100 && ltext == "ひゃく" && rtext == "けい") ltext = "ひゃっ"
    }
    // Python compares full (text, num) pairs; text arrives already selected.
    if (rnum == p100 && rtext != "ひゃく" && rtext != "びゃく" && rtext != "ぴゃく") {
        // kanji-sideひゃくvariants keep kanji merge simple: no-op
    }
    return "$ltext$rtext" to lnum * rnum
}

private val JA_ERA_START: List<Pair<Long, Pair<String, String>>> = listOf(
    645L to ("大化" to "たいか"),
    650L to ("白雉" to "はくち"),
    686L to ("朱鳥" to "しゅちょう"),
    701L to ("大宝" to "たいほう"),
    704L to ("慶雲" to "けいうん"),
    708L to ("和銅" to "わどう"),
    715L to ("霊亀" to "れいき"),
    717L to ("養老" to "ようろう"),
    724L to ("神亀" to "じんき"),
    729L to ("天平" to "てんぴょう"),
    749L to ("天平感宝" to "てんぴょうかんぽう"),
    749L to ("天平勝宝" to "てんぴょうしょうほう"),
    757L to ("天平宝字" to "てんぴょうじょうじ"),
    765L to ("天平神護" to "てんぴょうじんご"),
    767L to ("神護景雲" to "じんごけいうん"),
    770L to ("宝亀" to "ほうき"),
    781L to ("天応" to "てんおう"),
    782L to ("延暦" to "えんりゃく"),
    806L to ("大同" to "だいどう"),
    810L to ("弘仁" to "こうにん"),
    823L to ("天長" to "てんちょう"),
    834L to ("承和" to "じょうわ"),
    848L to ("嘉祥" to "かしょう"),
    851L to ("仁寿" to "にんじゅ"),
    855L to ("斉衡" to "さいこう"),
    857L to ("天安" to "てんあん"),
    859L to ("貞観" to "じょうがん"),
    877L to ("元慶" to "がんぎょう"),
    885L to ("仁和" to "にんな"),
    889L to ("寛平" to "かんぴょう"),
    898L to ("昌泰" to "しょうたい"),
    901L to ("延喜" to "えんぎ"),
    923L to ("延長" to "えんちょう"),
    931L to ("承平" to "じょうへい"),
    938L to ("天慶" to "てんぎょう"),
    947L to ("天暦" to "てんりゃく"),
    957L to ("天徳" to "てんとく"),
    961L to ("応和" to "おうわ"),
    964L to ("康保" to "こうほう"),
    968L to ("安和" to "あんな"),
    970L to ("天禄" to "てんろく"),
    974L to ("天延" to "てんえん"),
    976L to ("貞元" to "じょうげん"),
    979L to ("天元" to "てんげん"),
    983L to ("永観" to "えいかん"),
    985L to ("寛和" to "かんな"),
    987L to ("永延" to "えいえん"),
    989L to ("永祚" to "えいそ"),
    990L to ("正暦" to "しょうりゃく"),
    995L to ("長徳" to "ちょうとく"),
    999L to ("長保" to "ちょうほう"),
    1004L to ("寛弘" to "かんこう"),
    1013L to ("長和" to "ちょうわ"),
    1017L to ("寛仁" to "かんにん"),
    1021L to ("治安" to "じあん"),
    1024L to ("万寿" to "まんじゅ"),
    1028L to ("長元" to "ちょうげん"),
    1037L to ("長暦" to "ちょうりゃく"),
    1040L to ("長久" to "ちょうきゅう"),
    1045L to ("寛徳" to "かんとく"),
    1046L to ("永承" to "えいしょう"),
    1053L to ("天喜" to "てんぎ"),
    1058L to ("康平" to "こうへい"),
    1065L to ("治暦" to "じりゃく"),
    1069L to ("延久" to "えんきゅう"),
    1074L to ("承保" to "じょうほう"),
    1078L to ("承暦" to "じょうりゃく"),
    1081L to ("永保" to "えいほう"),
    1084L to ("応徳" to "おうとく"),
    1087L to ("寛治" to "かんじ"),
    1095L to ("嘉保" to "かほう"),
    1097L to ("永長" to "えいちょう"),
    1098L to ("承徳" to "じょうとく"),
    1099L to ("康和" to "こうわ"),
    1104L to ("長治" to "ちょうじ"),
    1106L to ("嘉承" to "かじょう"),
    1108L to ("天仁" to "てんにん"),
    1110L to ("天永" to "てんねい"),
    1113L to ("永久" to "えいきゅう"),
    1118L to ("元永" to "げんえい"),
    1120L to ("保安" to "ほうあん"),
    1124L to ("天治" to "てんじ"),
    1126L to ("大治" to "だいじ"),
    1131L to ("天承" to "てんしょう"),
    1132L to ("長承" to "ちょうしょう"),
    1135L to ("保延" to "ほうえん"),
    1141L to ("永治" to "えいじ"),
    1142L to ("康治" to "こうじ"),
    1144L to ("天養" to "てんよう"),
    1145L to ("久安" to "きゅうあん"),
    1151L to ("仁平" to "にんぺい"),
    1154L to ("久寿" to "きゅうじゅ"),
    1156L to ("保元" to "ほうげん"),
    1159L to ("平治" to "へいじ"),
    1160L to ("永暦" to "えいりゃく"),
    1161L to ("応保" to "おうほう"),
    1163L to ("長寛" to "ちょうかん"),
    1165L to ("永万" to "えいまん"),
    1166L to ("仁安" to "にんあん"),
    1169L to ("嘉応" to "かおう"),
    1171L to ("承安" to "しょうあん"),
    1175L to ("安元" to "あんげん"),
    1177L to ("治承" to "じしょう"),
    1181L to ("養和" to "ようわ"),
    1182L to ("寿永" to "じゅえい"),
    1184L to ("元暦" to "げんりゃく"),
    1185L to ("文治" to "ぶんじ"),
    1190L to ("建久" to "けんきゅう"),
    1199L to ("正治" to "しょうじ"),
    1201L to ("建仁" to "けんにん"),
    1204L to ("元久" to "げんきゅう"),
    1206L to ("建永" to "けんえい"),
    1207L to ("承元" to "じょうげん"),
    1211L to ("建暦" to "けんりゃく"),
    1214L to ("建保" to "けんぽう"),
    1219L to ("承久" to "じょうきゅう"),
    1222L to ("貞応" to "じょうおう"),
    1225L to ("元仁" to "げんにん"),
    1225L to ("嘉禄" to "かろく"),
    1228L to ("安貞" to "あんてい"),
    1229L to ("寛喜" to "かんき"),
    1232L to ("貞永" to "じょうえい"),
    1233L to ("天福" to "てんぷく"),
    1235L to ("文暦" to "ぶんりゃく"),
    1235L to ("嘉禎" to "かてい"),
    1239L to ("暦仁" to "りゃくにん"),
    1239L to ("延応" to "えんおう"),
    1240L to ("仁治" to "にんじ"),
    1243L to ("寛元" to "かんげん"),
    1247L to ("宝治" to "ほうじ"),
    1249L to ("建長" to "けんちょう"),
    1256L to ("康元" to "こうげん"),
    1257L to ("正嘉" to "しょうか"),
    1259L to ("正元" to "しょうげん"),
    1260L to ("文応" to "ぶんおう"),
    1261L to ("弘長" to "こうちょう"),
    1264L to ("文永" to "ぶんえい"),
    1275L to ("健治" to "けんじ"),
    1278L to ("弘安" to "こうあん"),
    1288L to ("正応" to "しょうおう"),
    1293L to ("永仁" to "えいにん"),
    1299L to ("正安" to "しょうあん"),
    1303L to ("乾元" to "けんげん"),
    1303L to ("嘉元" to "かげん"),
    1307L to ("徳治" to "とくじ"),
    1308L to ("延慶" to "えんきょう"),
    1311L to ("応長" to "おうちょう"),
    1312L to ("正和" to "しょうわ"),
    1317L to ("文保" to "ぶんぽう"),
    1319L to ("元応" to "げんおう"),
    1321L to ("元亨" to "げんこう"),
    1325L to ("正中" to "しょうちゅう"),
    1326L to ("嘉暦" to "かりゃく"),
    1329L to ("元徳" to "げんとく"),
    1331L to ("元弘" to "げんこう"),
    1332L to ("正慶" to "しょうけい"),
    1334L to ("建武" to "けんむ"),
    1336L to ("延元" to "えいげん"),
    1338L to ("暦応" to "りゃくおう"),
    1340L to ("興国" to "こうこく"),
    1342L to ("康永" to "こうえい"),
    1345L to ("貞和" to "じょうわ"),
    1347L to ("正平" to "しょうへい"),
    1350L to ("観応" to "かんおう"),
    1352L to ("文和" to "ぶんな"),
    1356L to ("延文" to "えんぶん"),
    1361L to ("康安" to "こうあん"),
    1362L to ("貞治" to "じょうじ"),
    1368L to ("応安" to "おうあん"),
    1370L to ("建徳" to "けんとく"),
    1372L to ("文中" to "ぶんちゅう"),
    1375L to ("永和" to "えいわ"),
    1375L to ("天授" to "てんじゅ"),
    1379L to ("康暦" to "こうりゃく"),
    1381L to ("永徳" to "えいとく"),
    1381L to ("弘和" to "こうわ"),
    1384L to ("至徳" to "しとく"),
    1384L to ("元中" to "げんちゅう"),
    1387L to ("嘉慶" to "かけい"),
    1389L to ("康応" to "こうおう"),
    1390L to ("明徳" to "めいとく"),
    1394L to ("応永" to "おうえい"),
    1428L to ("正長" to "しょうちょう"),
    1429L to ("永享" to "えいきょう"),
    1441L to ("嘉吉" to "かきつ"),
    1444L to ("文安" to "ぶんあん"),
    1449L to ("宝徳" to "ほうとく"),
    1452L to ("享徳" to "きょうとく"),
    1455L to ("康正" to "こうしょう"),
    1457L to ("長禄" to "ちょうろく"),
    1461L to ("寛正" to "かんしょう"),
    1466L to ("文正" to "ぶんしょう"),
    1467L to ("応仁" to "おうにん"),
    1469L to ("文明" to "ぶんめい"),
    1487L to ("長享" to "ちょうきょう"),
    1489L to ("延徳" to "えんとく"),
    1492L to ("明応" to "めいおう"),
    1501L to ("文亀" to "ぶんき"),
    1504L to ("永正" to "えいしょう"),
    1521L to ("大永" to "だいえい"),
    1528L to ("享禄" to "きょうろく"),
    1532L to ("天文" to "てんぶん"),
    1555L to ("弘治" to "こうじ"),
    1558L to ("永禄" to "えいろく"),
    1570L to ("元亀" to "げんき"),
    1573L to ("天正" to "てんしょう"),
    1593L to ("文禄" to "ぶんろく"),
    1596L to ("慶長" to "けいちょう"),
    1615L to ("元和" to "げんな"),
    1624L to ("寛永" to "かんえい"),
    1645L to ("正保" to "しょうほう"),
    1648L to ("慶安" to "けいあん"),
    1652L to ("承応" to "じょうおう"),
    1655L to ("明暦" to "めいれき"),
    1658L to ("万治" to "まんじ"),
    1661L to ("寛文" to "かんぶん"),
    1673L to ("延宝" to "えんぽう"),
    1681L to ("天和" to "てんな"),
    1684L to ("貞享" to "じょうきょう"),
    1688L to ("元禄" to "げんろく"),
    1704L to ("宝永" to "ほうえい"),
    1711L to ("正徳" to "しょうとく"),
    1716L to ("享保" to "きょうほう"),
    1736L to ("元文" to "げんぶん"),
    1741L to ("寛保" to "かんぽう"),
    1744L to ("延享" to "えんきょう"),
    1748L to ("寛延" to "かんえん"),
    1751L to ("宝暦" to "ほうれき"),
    1764L to ("明和" to "めいわ"),
    1773L to ("安永" to "あんえい"),
    1781L to ("天明" to "てんめい"),
    1801L to ("寛政" to "かんせい"),
    1802L to ("享和" to "きょうわ"),
    1804L to ("文化" to "ぶんか"),
    1818L to ("文政" to "ぶんせい"),
    1831L to ("天保" to "てんぽう"),
    1845L to ("弘化" to "こうか"),
    1848L to ("嘉永" to "かえい"),
    1855L to ("安政" to "あんせい"),
    1860L to ("万延" to "まんえい"),
    1861L to ("文久" to "ぶんきゅう"),
    1864L to ("元治" to "げんじ"),
    1865L to ("慶応" to "けいおう"),
    1868L to ("明治" to "めいじ"),
    1912L to ("大正" to "たいしょう"),
    1926L to ("昭和" to "しょうわ"),
    1989L to ("平成" to "へいせい"),
    2019L to ("令和" to "れいわ"),
)
