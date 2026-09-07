package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordsNotImplemented
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.parseCurrencyParts
import io.github.windedge.num2words.trimDouble

/** Mirrors num2words/lang_KO.py Num2Word_KO. */
class Num2WordKo : io.github.windedge.num2words.Num2WordBase() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "KRW" to (listOf("원") to emptyList()),
        "USD" to (listOf("달러") to listOf("센트")),
        "JPY" to (listOf("엔") to emptyList()),
    )

    private val ords: Map<String, String> = mapOf(
        "일" to "한", "이" to "두", "삼" to "세", "사" to "네",
        "오" to "다섯", "육" to "여섯", "칠" to "일곱", "팔" to "여덟",
        "구" to "아홉", "십" to "열", "이십" to "스물", "삼십" to "서른",
        "사십" to "마흔", "오십" to "쉰", "육십" to "예순", "칠십" to "일흔",
        "팔십" to "여든", "구십" to "아흔",
    )

    override fun setup() {
        super.setup()
        negword = "마이너스 "
        pointword = "점"
        useNumwords(
            high = listOf(
                "무량대수", "불가사의", "나유타", "아승기", "항하사",
                "극", "재", "정", "간", "구", "양", "자", "해",
                "경", "조", "억", "만",
            ),
            mid = listOf(
                BigInt.fromLong(1000) to "천",
                BigInt.fromLong(100) to "백",
            ),
            low = listOf(
                "십", "구", "팔", "칠", "육", "오", "사", "삼", "이",
                "일", "영",
            ),
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

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        val (ltext, lnum) = left
        val (rtext, rnum) = right
        val tenK = BigInt.fromLong(10000)
        if (lnum == BigInt.ONE && rnum <= tenK) return right
        if (lnum < tenK && lnum > rnum) return "$ltext$rtext" to lnum + rnum
        if (lnum >= tenK && lnum > rnum) return "$ltext $rtext" to lnum + rnum
        return "$ltext$rtext" to lnum * rnum
    }

    override fun toOrdinal(value: NumValue): String {
        verifyOrdinal(value)
        val whole = wholeOf(value)
        if (whole == BigInt.ONE) return "첫 번째"
        val outwords = toCardinal(value).split(" ").toMutableList()
        val lastwords = outwords.last().split("백").toMutableList()
        val last = lastwords.last()
        if ("십" in last) {
            val tenOne = last.split("십").toMutableList()
            tenOne[0] = requireNotNull(ords[tenOne[0] + "십"])
            try {
                tenOne[1] = requireNotNull(ords[tenOne[1]])
                tenOne[0] = tenOne[0].replace("스무", "스물")
            } catch (e: Exception) {
                // keep tenOne[1] as-is (mirrors Python KeyError pass)
            }
            lastwords[lastwords.lastIndex] = tenOne.joinToString("")
        } else {
            lastwords[lastwords.lastIndex] = requireNotNull(ords[last])
        }
        outwords[outwords.lastIndex] = lastwords.joinToString("백 ")
        return outwords.joinToString(" ") + " 번째"
    }

    override fun toOrdinalNum(value: NumValue): Any {
        verifyOrdinal(value)
        return "${wholeTextOf(value)} 번째"
    }

    fun toYearKo(value: NumValue, suffix: String? = null, longval: Boolean = true): String {
        var v = wholeOf(value)
        var suf = suffix
        if (v.sign() < 0) {
            v = -v
            if (suf == null) suf = "기원전"
        }
        val valtext = toCardinal(NumValue.Whole(v))
        return if (suf == null) "${valtext}년" else "$suf ${valtext}년"
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearKo(value, suffix, longval)

    fun toCurrencyKo(
        value: NumValue,
        currency: String = "KRW",
        cents: Boolean = false,
        separator: String = "",
        adjective: Boolean = false,
    ): String {
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
        if ((cents || right != 0) && cr2.isEmpty()) {
            throw Num2WordsValueError("Decimals not supported for \"$currency\"")
        }
        val minusStr = if (isNegative) negword else ""
        val rightBig = BigInt.fromLong(right.toLong())
        return minusStr +
            toCardinal(NumValue.Whole(left)).replace(" ", "") +
            cr1[0] +
            (if (cr2.isNotEmpty()) " " + toCardinal(NumValue.Whole(rightBig)) else "") +
            (cr2.firstOrNull() ?: "")
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyKo(value, currency, cents, separator, adjective)

    private fun wholeOf(value: NumValue): BigInt = when (value) {
        is NumValue.Whole -> value.v
        is NumValue.Decimal -> integerIfWhole(value.v) ?: BigInt.ZERO
        is NumValue.FloatVal -> doubleToBigInt(value.v)
    }

    private fun wholeTextOf(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }
}
