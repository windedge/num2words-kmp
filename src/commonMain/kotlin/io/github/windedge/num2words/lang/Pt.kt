package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordsNotImplemented
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

private val PT_DOLLAR = listOf("dólar", "dólares")
private val PT_CENTS = listOf("cêntimo", "cêntimos")

/** Mirrors num2words/lang_PT.py Num2Word_PT. */
class Num2WordPt : Num2WordEU() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "AUD" to (PT_DOLLAR to PT_CENTS),
        "CAD" to (PT_DOLLAR to PT_CENTS),
        "EUR" to (listOf("euro", "euros") to PT_CENTS),
        "GBP" to (listOf("libra", "libras") to listOf("péni", "pence")),
        "USD" to (PT_DOLLAR to PT_CENTS),
    )

    private val ords: List<Map<Int, String>> = listOf(
        mapOf(
            0 to "", 1 to "primeiro", 2 to "segundo", 3 to "terceiro",
            4 to "quarto", 5 to "quinto", 6 to "sexto", 7 to "sétimo",
            8 to "oitavo", 9 to "nono",
        ),
        mapOf(
            0 to "", 1 to "décimo", 2 to "vigésimo", 3 to "trigésimo",
            4 to "quadragésimo", 5 to "quinquagésimo", 6 to "sexagésimo",
            7 to "septuagésimo", 8 to "octogésimo", 9 to "nonagésimo",
        ),
        mapOf(
            0 to "", 1 to "centésimo", 2 to "ducentésimo", 3 to "tricentésimo",
            4 to "quadrigentésimo", 5 to "quingentésimo", 6 to "seiscentésimo",
            7 to "septigentésimo", 8 to "octigentésimo", 9 to "nongentésimo",
        ),
    )

    private val thousandSeparators: Map<Int, String> = mapOf(
        3 to "milésimo",
        6 to "milionésimo",
        9 to "milésimo milionésimo",
        12 to "bilionésimo",
        15 to "milésimo bilionésimo",
    )

    private val hundreds: Map<Int, String> = mapOf(
        1 to "cento", 2 to "duzentos", 3 to "trezentos", 4 to "quatrocentos",
        5 to "quinhentos", 6 to "seiscentos", 7 to "setecentos",
        8 to "oitocentos", 9 to "novecentos",
    )

    override fun setup() {
        super.setup()
        gigaSuffix = ""
        megaSuffix = "ilião"
        negword = "menos "
        pointword = "vírgula"
        excludeTitle.clear()
        excludeTitle.addAll(listOf("e", "vírgula", "menos"))
        useNumwords(
            high = genHighNumwords(emptyList(), emptyList(), listOf("quatr", "tr", "b", "m")),
            mid = listOf(
                BigInt.fromLong(1000) to "mil",
                BigInt.fromLong(100) to "cem",
                BigInt.fromLong(90) to "noventa",
                BigInt.fromLong(80) to "oitenta",
                BigInt.fromLong(70) to "setenta",
                BigInt.fromLong(60) to "sessenta",
                BigInt.fromLong(50) to "cinquenta",
                BigInt.fromLong(40) to "quarenta",
                BigInt.fromLong(30) to "trinta",
            ),
            low = listOf(
                "vinte", "dezanove", "dezoito", "dezassete", "dezasseis",
                "quinze", "catorze", "treze", "doze", "onze", "dez",
                "nove", "oito", "sete", "seis", "cinco", "quatro", "três", "dois",
                "um", "zero",
            ),
        )
    }

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        var (ctext, cnum) = left
        var (ntext, nnum) = right
        val one = BigInt.ONE
        val hundred = BigInt.fromLong(100)
        val thousand = BigInt.fromLong(1000)
        val million = BigInt.fromLong(1000000)
        val billion = BigInt.fromLong(1000000000)
        if (cnum == one) {
            if (nnum < million) return right
            ctext = "um"
        } else if (cnum == hundred && nnum % thousand != BigInt.ZERO) {
            ctext = "cento"
        }
        if (nnum < cnum) {
            return "$ctext e $ntext" to cnum + nnum
        } else if (nnum % billion == BigInt.ZERO && cnum > one) {
            ntext = ntext.dropLast(4) + "liões"
        } else if (nnum % million == BigInt.ZERO && cnum > one) {
            ntext = ntext.dropLast(4) + "lhões"
        }
        if (ntext == "milião") ntext = "milhão"
        if (nnum == hundred) {
            ctext = requireNotNull(hundreds[cnum.toLong().toInt()])
            ntext = ""
        } else {
            ntext = " $ntext"
        }
        return (ctext + ntext to cnum * nnum)
    }

    fun toCardinalPt(value: NumValue): String {
        var result = super.toCardinal(value)
        for (ext in listOf("mil", "milhão", "milhões", "mil milhões", "bilião", "biliões", "mil biliões")) {
            if (Regex(".*$ext e \\w*entos? (?=.*e)").containsMatchIn(result)) {
                result = result.replace("$ext e", ext)
            }
        }
        return result
    }

    override fun toCardinal(value: NumValue): String = toCardinalPt(value)

    override fun toOrdinal(value: NumValue): String {
        verifyOrdinal(value)
        val text = wholeTextOf(value)
        val result = mutableListOf<String>()
        var thousandSeparator = ""
        val reversed = text.reversed()
        for ((idx, ch) in reversed.withIndex()) {
            if (idx != 0 && idx % 3 == 0) {
                thousandSeparator = thousandSeparators[idx] ?: ""
            }
            if (ch != '0' && thousandSeparator.isNotEmpty()) {
                result.add(thousandSeparator)
                thousandSeparator = ""
            }
            result.add(requireNotNull(ords[idx % 3][ch - '0']))
        }
        var out = result.reversed().joinToString(" ").trim().replace(Regex("\\s+"), " ")
        if (out.startsWith("primeiro") && text != "1") {
            out = out.substring("primeiro ".length)
        }
        return out
    }

    override fun toOrdinalNum(value: NumValue): Any {
        verifyOrdinal(value)
        return "${wholeTextOf(value)}º"
    }

    fun toYearPt(value: NumValue, longval: Boolean = true): String {
        val v = wholeOf(value)
        if (v.sign() < 0) return toCardinal(NumValue.Whole(-v)) + " antes de Cristo"
        return toCardinal(value)
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearPt(value, longval)

    fun toCurrencyPt(
        value: NumValue,
        currency: String = "EUR",
        cents: Boolean = true,
        separator: String = " e",
        adjective: Boolean = false,
    ): String {
        val backupNegword = negword
        negword = backupNegword.dropLast(1)
        var result = super.toCurrency(value, currency, cents, separator, adjective)
        negword = backupNegword
        val cr1 = currencyForms[currency]?.first
            ?: throw Num2WordsNotImplemented("Currency code \"$currency\" not implemented")
        for (ext in listOf("milhão", "milhões", "bilião", "biliões", "trilião", "triliões")) {
            if (Regex(".*$ext (?=${cr1[1]})").containsMatchIn(result)) {
                result = result.replace(ext, "$ext de")
                break
            }
        }
        result = result.replace(" e zero cêntimos", "")
        return result
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyPt(value, currency, cents, separator, adjective)

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
