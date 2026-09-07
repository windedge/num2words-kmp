package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordsNotImplemented
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

/** Mirrors num2words/lang_DE.py Num2Word_DE. */
class Num2WordDe : Num2WordEU() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "EUR" to (listOf("Euro", "Euro") to listOf("Cent", "Cent")),
        "GBP" to (listOf("Pfund", "Pfund") to listOf("Penny", "Pence")),
        "USD" to (listOf("Dollar", "Dollar") to listOf("Cent", "Cent")),
        "CNY" to (listOf("Yuan", "Yuan") to listOf("Jiao", "Fen")),
        "DEM" to (listOf("Mark", "Mark") to listOf("Pfennig", "Pfennig")),
    )

    private val ords: Map<String, String> = mapOf(
        "eins" to "ers",
        "drei" to "drit",
        "acht" to "ach",
        "sieben" to "sieb",
        "ig" to "igs",
        "ert" to "erts",
        "end" to "ends",
        "ion" to "ions",
        "nen" to "ns",
        "rde" to "rds",
        "rden" to "rds",
    )

    override fun setup() {
        negword = "minus "
        pointword = "Komma"
        errmsgFloatord = "Die Gleitkommazahl %s kann nicht in eine Ordnungszahl konvertiert werden."
        errmsgNonnum = "Nur Zahlen (type(%s)) können in Wörter konvertiert werden."
        errmsgNegord = "Die negative Zahl %s kann nicht in eine Ordnungszahl konvertiert werden."
        errmsgToobig = "Die Zahl %s muss kleiner als %s sein."
        excludeTitle.clear()
        gigaSuffix = "illiarde"
        megaSuffix = "illion"
        val lows = listOf("Non", "Okt", "Sept", "Sext", "Quint", "Quadr", "Tr", "B", "M")
        val units = listOf("", "un", "duo", "tre", "quattuor", "quin", "sex", "sept", "okto", "novem")
        val tens = listOf(
            "dez", "vigint", "trigint", "quadragint", "quinquagint",
            "sexagint", "septuagint", "oktogint", "nonagint",
        )
        useNumwords(
            high = listOf("zent") + genHighNumwords(units, tens, lows),
            mid = listOf(
                BigInt.fromLong(1000) to "tausend",
                BigInt.fromLong(100) to "hundert",
                BigInt.fromLong(90) to "neunzig",
                BigInt.fromLong(80) to "achtzig",
                BigInt.fromLong(70) to "siebzig",
                BigInt.fromLong(60) to "sechzig",
                BigInt.fromLong(50) to "fünfzig",
                BigInt.fromLong(40) to "vierzig",
                BigInt.fromLong(30) to "dreißig",
            ),
            low = listOf(
                "zwanzig", "neunzehn", "achtzehn", "siebzehn",
                "sechzehn", "fünfzehn", "vierzehn", "dreizehn",
                "zwölf", "elf", "zehn", "neun", "acht",
                "sieben", "sechs", "fünf", "vier", "drei",
                "zwei", "eins", "null",
            ),
        )
    }

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        var (ctext, cnum) = left
        var (ntext, nnum) = right
        val million = BigInt.pow10(6)
        val thousand = BigInt.fromLong(1000)
        val hundred = BigInt.fromLong(100)
        val ten = BigInt.fromLong(10)
        if (cnum == BigInt.ONE) {
            if (nnum == hundred || nnum == thousand) return ("ein$ntext" to nnum)
            else if (nnum < million) return right
            ctext = "eine"
        }
        val word: String
        val value: BigInt
        if (nnum > cnum) {
            if (nnum >= million) {
                if (cnum > BigInt.ONE) {
                    ntext = if (ntext.endsWith("e")) ntext + "n" else ntext + "en"
                }
                ctext += " "
            }
            word = ctext + ntext
            value = cnum * nnum
        } else {
            if (nnum < ten && ten < cnum && cnum < hundred) {
                if (nnum == BigInt.ONE) ntext = "ein"
                val tmp = ntext
                ntext = ctext
                ctext = tmp + "und"
            } else if (cnum >= million) {
                ctext += " "
            }
            word = ctext + ntext
            value = cnum + nnum
        }
        return word to value
    }

    override fun toOrdinal(value: NumValue): String {
        verifyOrdinal(value)
        var outword = toCardinal(value).lowercase()
        for ((key, replacement) in ords) {
            if (outword.endsWith(key)) {
                outword = outword.dropLast(key.length) + replacement
                break
            }
        }
        var res = outword + "te"
        if (res == "eintausendste" || res == "einhundertste") {
            res = res.replaceFirst("ein", "")
        }
        // eine (...)illionste -> (...)illionste
        res = Regex("eine ([a-z]+(illion|illiard)ste)$").replace(res) { it.groupValues[1] }
        // (...) (...)illionste -> (...)(...)illionste (no space)
        res = Regex(" ([a-z]+(illion|illiard)ste)$").replace(res) { it.groupValues[1] }
        return res
    }

    override fun toOrdinalNum(value: NumValue): Any {
        verifyOrdinal(value)
        return "${wholeTextOf(value)}."
    }

    fun toCurrencyDe(
        value: NumValue,
        currency: String = "EUR",
        cents: Boolean = true,
        separator: String = " und",
        adjective: Boolean = false,
    ): String {
        val result = super.toCurrency(value, currency, cents, separator, adjective)
        return result.replace("eins ", "ein ")
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyDe(value, currency, cents, separator, adjective)

    fun toYearDe(value: NumValue, longval: Boolean = true): String {
        val v = wholeOfYear(value)
        if ((v / BigInt.fromLong(100)) % BigInt.fromLong(10) == BigInt.ZERO) {
            return toCardinal(NumValue.Whole(v))
        }
        return toSplitNum(v, hightxt = "hundert", longval = longval).replace(" ", "")
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearDe(value, longval)

    private fun wholeOfYear(value: NumValue): BigInt = when (value) {
        is NumValue.Whole -> value.v
        is NumValue.Decimal -> integerIfWhole(value.v)
            ?: throw Num2WordsValueError(fmt(errmsgNonnum, value.v.toPlainString()))

        is NumValue.FloatVal -> doubleToBigInt(value.v)
    }

    private fun wholeTextOf(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }
}
