package io.github.windedge.num2words

import io.github.windedge.num2words.lang.Num2WordAr
import io.github.windedge.num2words.lang.Num2WordDe
import io.github.windedge.num2words.lang.Num2WordEn
import io.github.windedge.num2words.lang.Num2WordEs
import io.github.windedge.num2words.lang.Num2WordFr
import io.github.windedge.num2words.lang.Num2WordIt
import io.github.windedge.num2words.lang.JaOptions
import io.github.windedge.num2words.lang.Num2WordJa
import io.github.windedge.num2words.lang.Num2WordKo
import io.github.windedge.num2words.lang.Num2WordPt
import io.github.windedge.num2words.lang.Num2WordZh
import io.github.windedge.num2words.lang.Num2WordZhCn
import io.github.windedge.num2words.lang.Num2WordZhHk
import io.github.windedge.num2words.lang.Num2WordZhTw
import io.github.windedge.num2words.lang.ZhOptions

/** Output kind, mirroring the `to` argument of Python num2words. */
enum class ConvertTo {
    CARDINAL,
    ORDINAL,
    ORDINAL_NUM,
    YEAR,
    CURRENCY,
    ;

    companion object {
        fun parse(s: String): ConvertTo = when (s.lowercase()) {
            "cardinal" -> CARDINAL
            "ordinal" -> ORDINAL
            "ordinal_num" -> ORDINAL_NUM
            "year" -> YEAR
            "currency" -> CURRENCY
            else -> throw Num2WordsNotImplemented("Unknown convert type: $s")
        }
    }
}

/**
 * Entry point, mirroring num2words/__init__.py num2words().
 *
 * Python **kwargs arrive as [options]; each language reads the keys it needs
 * (currency/separator/cents/adjective/suffix/...). Unknown keys are ignored,
 * same as Python passing an unused kwarg to a method that accepts **kwargs.
 * Output is byte-identical to Python for the same input, language and options.
 */
object Num2Words {
    private val converters: Map<String, () -> Num2WordBase> = mapOf(
        "en" to ::Num2WordEn,
        "zh" to ::Num2WordZh,
        "zh_CN" to ::Num2WordZhCn,
        "zh_HK" to ::Num2WordZhHk,
        "zh_TW" to ::Num2WordZhTw,
        "de" to ::Num2WordDe,
        "fr" to ::Num2WordFr,
        "es" to ::Num2WordEs,
        "it" to ::Num2WordIt,
        "pt" to ::Num2WordPt,
        "ja" to ::Num2WordJa,
        "ko" to ::Num2WordKo,
        "ar" to ::Num2WordAr,
    )

    fun convert(
        number: Long,
        lang: String = "en",
        to: ConvertTo = ConvertTo.CARDINAL,
        options: Map<String, Any?> = emptyMap(),
    ): Any = convert(NumValue.Whole(BigInt.fromLong(number)), lang, to, options)

    fun convert(
        number: Int,
        lang: String = "en",
        to: ConvertTo = ConvertTo.CARDINAL,
        options: Map<String, Any?> = emptyMap(),
    ): Any = convert(number.toLong(), lang, to, options)

    fun convert(
        number: Double,
        lang: String = "en",
        to: ConvertTo = ConvertTo.CARDINAL,
        options: Map<String, Any?> = emptyMap(),
    ): Any = convert(NumValue.FloatVal(number), lang, to, options)

    fun convert(
        number: String,
        lang: String = "en",
        to: ConvertTo = ConvertTo.CARDINAL,
        options: Map<String, Any?> = emptyMap(),
    ): Any {
        val converter = resolve(lang)
        return convert(NumValue.Decimal(converter.strToNumber(number)), lang, to, options)
    }

    fun convert(
        number: NumValue,
        lang: String = "en",
        to: ConvertTo = ConvertTo.CARDINAL,
        options: Map<String, Any?> = emptyMap(),
    ): Any {
        val converter = resolve(lang)
        if (converter is Num2WordZh) return convertZh(converter, number, to, options)
        if (converter is Num2WordEs) return convertEs(converter, number, to, options)
        if (converter is Num2WordDe) return convertDe(converter, number, to, options)
        if (converter is Num2WordIt) return convertIt(converter, number, to, options)
        if (converter is Num2WordPt) return convertPt(converter, number, to, options)
        if (converter is Num2WordFr) return convertFr(converter, number, to, options)
        if (converter is Num2WordJa) return convertJa(converter, number, to, options)
        if (converter is Num2WordKo) return convertKo(converter, number, to, options)
        if (converter is Num2WordAr) return convertAr(converter, number, to, options)
        return when (to) {
            ConvertTo.CARDINAL -> converter.toCardinal(number)
            ConvertTo.ORDINAL -> converter.toOrdinal(number)
            ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
            ConvertTo.YEAR -> converter.toYear(
                number,
                suffix = options["suffix"] as? String,
                longval = (options["longval"] as? Boolean) ?: true,
            )

            ConvertTo.CURRENCY -> converter.toCurrency(
                number,
                currency = (options["currency"] as? String) ?: "EUR",
                cents = (options["cents"] as? Boolean) ?: true,
                separator = (options["separator"] as? String) ?: ",",
                adjective = (options["adjective"] as? Boolean) ?: false,
            )
        }
    }

    private fun convertZh(
        converter: Num2WordZh,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any {
        val zh = zhOptions(options)
        val counter = options["counter"] as? String ?: ""
        return when (to) {
            ConvertTo.CARDINAL -> converter.toCardinalZh(number, zh)
            ConvertTo.ORDINAL -> converter.toOrdinalZh(number, counter, zh)
            ConvertTo.ORDINAL_NUM -> converter.toOrdinalNumZh(number, counter, zh).toString()
            ConvertTo.YEAR -> if (converter is Num2WordZhTw) {
                converter.toYearTw(
                    number,
                    era = (options["era"] as? Boolean) ?: false,
                    options = zh,
                )
            } else {
                converter.toYearZh(number, zh)
            }

            ConvertTo.CURRENCY -> converter.toCurrencyZh(
                number,
                currency = (options["currency"] as? String) ?: "XXX",
                options = zh,
            )
        }
    }

    private fun convertEs(
        converter: Num2WordEs,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any {
        val gender = (options["gender"] as? String) ?: "m"
        return when (to) {
            ConvertTo.CARDINAL -> converter.toCardinal(number)
            ConvertTo.ORDINAL -> converter.toOrdinalEs(number, gender)
            ConvertTo.ORDINAL_NUM -> converter.toOrdinalNumEs(number, gender).toString()
            ConvertTo.YEAR -> converter.toYear(number)
            ConvertTo.CURRENCY -> converter.toCurrencyEs(
                number,
                currency = (options["currency"] as? String) ?: "EUR",
                cents = (options["cents"] as? Boolean) ?: true,
                separator = (options["separator"] as? String) ?: " con",
                adjective = (options["adjective"] as? Boolean) ?: false,
            )
        }
    }

    private fun convertDe(
        converter: Num2WordDe,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any = when (to) {
        ConvertTo.CARDINAL -> converter.toCardinal(number)
        ConvertTo.ORDINAL -> converter.toOrdinal(number)
        ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
        ConvertTo.YEAR -> converter.toYearDe(
            number,
            longval = (options["longval"] as? Boolean) ?: true,
        )

        ConvertTo.CURRENCY -> converter.toCurrencyDe(
            number,
            currency = (options["currency"] as? String) ?: "EUR",
            cents = (options["cents"] as? Boolean) ?: true,
            separator = (options["separator"] as? String) ?: " und",
            adjective = (options["adjective"] as? Boolean) ?: false,
        )
    }

    private fun convertIt(
        converter: Num2WordIt,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any = when (to) {
        ConvertTo.CARDINAL -> converter.toCardinal(number)
        ConvertTo.ORDINAL -> converter.toOrdinal(number)
        ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
        ConvertTo.YEAR -> converter.toYear(number)
        ConvertTo.CURRENCY -> converter.toCurrencyIt(
            number,
            currency = (options["currency"] as? String) ?: "EUR",
            cents = (options["cents"] as? Boolean) ?: true,
            separator = (options["separator"] as? String) ?: " e",
            adjective = (options["adjective"] as? Boolean) ?: false,
        )
    }

    private fun convertPt(
        converter: Num2WordPt,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any = when (to) {
        ConvertTo.CARDINAL -> converter.toCardinal(number)
        ConvertTo.ORDINAL -> converter.toOrdinal(number)
        ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
        ConvertTo.YEAR -> converter.toYearPt(
            number,
            longval = (options["longval"] as? Boolean) ?: true,
        )

        ConvertTo.CURRENCY -> converter.toCurrencyPt(
            number,
            currency = (options["currency"] as? String) ?: "EUR",
            cents = (options["cents"] as? Boolean) ?: true,
            separator = (options["separator"] as? String) ?: " e",
            adjective = (options["adjective"] as? Boolean) ?: false,
        )
    }

    private fun convertJa(
        converter: Num2WordJa,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any {
        val ja = JaOptions(
            reading = options["reading"],
            prefer = (options["prefer"] as? List<*>)?.map { it.toString() }?.toSet(),
            counter = (options["counter"] as? String) ?: "番",
            era = (options["era"] as? Boolean) ?: true,
        )
        return when (to) {
            ConvertTo.CARDINAL -> converter.toCardinalJa(number, ja)
            ConvertTo.ORDINAL -> converter.toOrdinalJa(number, ja)
            ConvertTo.ORDINAL_NUM -> converter.toOrdinalNumJa(number, ja).toString()
            ConvertTo.YEAR -> converter.toYearJa(number, ja)
            ConvertTo.CURRENCY -> converter.toCurrencyJa(
                number,
                currency = (options["currency"] as? String) ?: "JPY",
                cents = (options["cents"] as? Boolean) ?: false,
                separator = (options["separator"] as? String) ?: "",
                adjective = (options["adjective"] as? Boolean) ?: false,
                options = ja,
            )
        }
    }

    private fun convertKo(
        converter: Num2WordKo,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any = when (to) {
        ConvertTo.CARDINAL -> converter.toCardinal(number)
        ConvertTo.ORDINAL -> converter.toOrdinal(number)
        ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
        ConvertTo.YEAR -> converter.toYearKo(
            number,
            suffix = options["suffix"] as? String,
            longval = (options["longval"] as? Boolean) ?: true,
        )

        ConvertTo.CURRENCY -> converter.toCurrencyKo(
            number,
            currency = (options["currency"] as? String) ?: "KRW",
            cents = (options["cents"] as? Boolean) ?: false,
            separator = (options["separator"] as? String) ?: "",
            adjective = (options["adjective"] as? Boolean) ?: false,
        )
    }

    private fun convertAr(
        converter: Num2WordAr,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any = when (to) {
        ConvertTo.CARDINAL -> converter.toCardinal(number)
        ConvertTo.ORDINAL -> converter.toOrdinalAr(
            number,
            prefix = (options["prefix"] as? String) ?: "",
        )

        ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
        ConvertTo.YEAR -> converter.toYear(number)
        ConvertTo.CURRENCY -> converter.toCurrencyAr(
            number,
            currency = (options["currency"] as? String) ?: "SR",
            prefix = (options["prefix"] as? String) ?: "",
            suffix = (options["suffix"] as? String) ?: "",
        )
    }

    private fun convertFr(
        converter: Num2WordFr,
        number: NumValue,
        to: ConvertTo,
        options: Map<String, Any?>,
    ): Any = when (to) {
        ConvertTo.CARDINAL -> converter.toCardinal(number)
        ConvertTo.ORDINAL -> converter.toOrdinal(number)
        ConvertTo.ORDINAL_NUM -> converter.toOrdinalNum(number).toString()
        ConvertTo.YEAR -> converter.toYear(number)
        ConvertTo.CURRENCY -> converter.toCurrency(
            number,
            currency = (options["currency"] as? String) ?: "EUR",
            cents = (options["cents"] as? Boolean) ?: true,
            separator = (options["separator"] as? String) ?: " et",
            adjective = (options["adjective"] as? Boolean) ?: false,
        )
    }

    private fun zhOptions(options: Map<String, Any?>): ZhOptions = ZhOptions(
        stuffZero = (options["stuff_zero"] as? Int) ?: 2,
        reading = options["reading"],
        prefer = (options["prefer"] as? List<*>)?.map { it.toString() }?.toSet(),
    )

    private fun resolve(lang: String): Num2WordBase {
        val factory = converters[lang] ?: converters[lang.take(2)]
        return factory?.invoke() ?: throw Num2WordsNotImplemented("Language not implemented: $lang")
    }
}

/** Convenience top-level function mirroring Python num2words(). */
fun num2words(
    number: Long,
    lang: String = "en",
    to: String = "cardinal",
    options: Map<String, Any?> = emptyMap(),
): Any = Num2Words.convert(number, lang, ConvertTo.parse(to), options)

fun num2words(
    number: Int,
    lang: String = "en",
    to: String = "cardinal",
    options: Map<String, Any?> = emptyMap(),
): Any = Num2Words.convert(number, lang, ConvertTo.parse(to), options)

fun num2words(
    number: Double,
    lang: String = "en",
    to: String = "cardinal",
    options: Map<String, Any?> = emptyMap(),
): Any = Num2Words.convert(number, lang, ConvertTo.parse(to), options)

fun num2words(
    number: String,
    lang: String = "en",
    to: String = "cardinal",
    options: Map<String, Any?> = emptyMap(),
): Any = Num2Words.convert(number, lang, ConvertTo.parse(to), options)
