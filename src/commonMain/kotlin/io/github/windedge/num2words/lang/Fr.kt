package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

/** Mirrors num2words/lang_FR.py Num2Word_FR. */
class Num2WordFr : Num2WordEU() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "EUR" to (listOf("euro", "euros") to listOf("centime", "centimes")),
        "USD" to (listOf("dollar", "dollars") to listOf("cent", "cents")),
        "FRF" to (listOf("franc", "francs") to listOf("centime", "centimes")),
        "GBP" to (listOf("livre", "livres") to listOf("penny", "pence")),
        "CNY" to (listOf("yuan", "yuans") to listOf("fen", "jiaos")),
    )

    private val ords: Map<String, String> = mapOf(
        "cinq" to "cinquième",
        "neuf" to "neuvième",
    )

    override fun setup() {
        super.setup()
        negword = "moins "
        pointword = "virgule"
        errmsgNonnum = "Seulement des nombres peuvent être convertis en mots."
        errmsgToobig = "Nombre trop grand pour être converti en mots (abs(%s) > %s)."
        excludeTitle.clear()
        excludeTitle.addAll(listOf("et", "virgule", "moins"))
        useNumwords(
            high = highWords,
            mid = listOf(
                BigInt.fromLong(1000) to "mille",
                BigInt.fromLong(100) to "cent",
                BigInt.fromLong(80) to "quatre-vingts",
                BigInt.fromLong(60) to "soixante",
                BigInt.fromLong(50) to "cinquante",
                BigInt.fromLong(40) to "quarante",
                BigInt.fromLong(30) to "trente",
            ),
            low = listOf(
                "vingt", "dix-neuf", "dix-huit", "dix-sept",
                "seize", "quinze", "quatorze", "treize", "douze",
                "onze", "dix", "neuf", "huit", "sept", "six",
                "cinq", "quatre", "trois", "deux", "un", "zéro",
            ),
        )
    }

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        var (ctext, cnum) = left
        val (ntext0, nnum) = right
        var ntext = ntext0
        val one = BigInt.ONE
        val hundred = BigInt.fromLong(100)
        val thousand = BigInt.fromLong(1000)
        val million = BigInt.fromLong(1000000)
        if (cnum == one) {
            if (nnum < million) return right
        } else {
            if (((cnum - BigInt.fromLong(80)) % hundred == BigInt.ZERO ||
                    (cnum % hundred == BigInt.ZERO && cnum < thousand)
                    ) && nnum < million && ctext.endsWith("s")
            ) {
                ctext = ctext.dropLast(1)
            }
            if (cnum < thousand && nnum != thousand &&
                !ntext.endsWith("s") && nnum % hundred == BigInt.ZERO
            ) {
                ntext += "s"
            }
        }
        if (nnum < cnum && cnum < hundred) {
            return if (nnum % BigInt.fromLong(10) == one && cnum != BigInt.fromLong(80)) {
                "$ctext et $ntext" to cnum + nnum
            } else {
                "$ctext-$ntext" to cnum + nnum
            }
        }
        if (nnum > cnum) return "$ctext $ntext" to cnum * nnum
        return "$ctext $ntext" to cnum + nnum
    }

    override fun toOrdinal(value: NumValue): String {
        verifyOrdinal(value)
        val whole = wholeOf(value)
        if (whole == BigInt.ONE) return "premier"
        var word = toCardinal(value)
        var replaced = false
        for ((src, repl) in ords) {
            if (word.endsWith(src)) {
                word = word.dropLast(src.length) + repl
                replaced = true
                break
            }
        }
        if (!replaced) {
            if (word.endsWith("e")) word = word.dropLast(1)
            word += "ième"
        }
        return word
    }

    override fun toOrdinalNum(value: NumValue): Any {
        verifyOrdinal(value)
        val whole = wholeOf(value)
        return "${wholeToText(value)}${if (whole == BigInt.ONE) "er" else "me"}"
    }

    private fun wholeOf(value: NumValue): BigInt = when (value) {
        is NumValue.Whole -> value.v
        is NumValue.Decimal -> integerIfWhole(value.v)
            ?: throw Num2WordsValueError(fmt(errmsgFloatord, value.v.toPlainString()))

        is NumValue.FloatVal -> doubleToBigInt(value.v)
    }

    private fun wholeToText(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }
}
