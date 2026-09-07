package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

/** Mirrors num2words/lang_EN.py Num2Word_EN. */
class Num2WordEn : Num2WordEU() {
    private val ords: Map<String, String> = mapOf(
        "one" to "first",
        "two" to "second",
        "three" to "third",
        "four" to "fourth",
        "five" to "fifth",
        "six" to "sixth",
        "seven" to "seventh",
        "eight" to "eighth",
        "nine" to "ninth",
        "ten" to "tenth",
        "eleven" to "eleventh",
        "twelve" to "twelfth",
    )

    override fun setup() {
        super.setup()
        negword = "minus "
        pointword = "point"
        excludeTitle.addAll(listOf("and", "point", "minus"))
        useNumwords(
            high = highWords,
            mid = listOf(
                BigInt.fromLong(1000) to "thousand",
                BigInt.fromLong(100) to "hundred",
                BigInt.fromLong(90) to "ninety",
                BigInt.fromLong(80) to "eighty",
                BigInt.fromLong(70) to "seventy",
                BigInt.fromLong(60) to "sixty",
                BigInt.fromLong(50) to "fifty",
                BigInt.fromLong(40) to "forty",
                BigInt.fromLong(30) to "thirty",
            ),
            low = listOf(
                "twenty", "nineteen", "eighteen", "seventeen",
                "sixteen", "fifteen", "fourteen", "thirteen",
                "twelve", "eleven", "ten", "nine", "eight",
                "seven", "six", "five", "four", "three", "two",
                "one", "zero",
            ),
        )
    }

    override fun setHighNumwords(high: List<String>) {
        val max = 3 + 3 * high.size
        var n = max
        for (word in high) {
            cards[BigInt.pow10(n)] = word + "illion"
            n -= 3
        }
    }

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        val (ltext, lnum) = left
        val (rtext, rnum) = right
        val one = BigInt.ONE
        val hundred = BigInt.fromLong(100)
        return when {
            lnum == one && rnum < hundred -> rtext to rnum
            lnum < hundred && lnum > rnum -> "$ltext-$rtext" to lnum + rnum
            lnum >= hundred && rnum < hundred -> "$ltext and $rtext" to lnum + rnum
            rnum > lnum -> "$ltext $rtext" to lnum * rnum
            else -> "$ltext, $rtext" to lnum + rnum
        }
    }

    override fun toOrdinal(value: NumValue): String {
        verifyOrdinal(value)
        val outwords = toCardinal(value).split(" ").toMutableList()
        val lastwords = outwords.last().split("-").toMutableList()
        var lastword = lastwords.last().lowercase()
        lastword = ords[lastword] ?: run {
            if (lastword.endsWith("y")) lastword = lastword.dropLast(1) + "ie"
            lastword + "th"
        }
        lastwords[lastwords.lastIndex] = title(lastword)
        outwords[outwords.lastIndex] = lastwords.joinToString("-")
        return outwords.joinToString(" ")
    }

    override fun toOrdinalNum(value: NumValue): Any {
        verifyOrdinal(value)
        return "${wholeText(value)}${toOrdinal(value).takeLast(2)}"
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String {
        var v = wholeOf(value)
        var suf = suffix
        if (v.sign() < 0) {
            v = -v
            if (suf == null) suf = "BC"
        }
        val (high, low) = v.divmod(BigInt.fromLong(100))
        val hundred = BigInt.fromLong(100)
        val ten = BigInt.fromLong(10)
        val valtext = if (high.isZero() || (high % ten).isZero() && low < ten || high >= hundred) {
            toCardinal(NumValue.Whole(v))
        } else {
            val hightext = toCardinal(NumValue.Whole(high))
            val lowtext = when {
                low.isZero() -> "hundred"
                low < ten -> "oh-${toCardinal(NumValue.Whole(low))}"
                else -> toCardinal(NumValue.Whole(low))
            }
            "$hightext $lowtext"
        }
        return if (suf == null) valtext else "$valtext $suf"
    }

    private fun wholeOf(value: NumValue): BigInt = when (value) {
        is NumValue.Whole -> value.v
        is NumValue.Decimal -> integerIfWhole(value.v)
            ?: throw Num2WordsValueError(fmt(errmsgNonnum, value.v.toPlainString()))

        is NumValue.FloatVal -> doubleToBigInt(value.v)
    }

    private fun wholeText(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }
}
