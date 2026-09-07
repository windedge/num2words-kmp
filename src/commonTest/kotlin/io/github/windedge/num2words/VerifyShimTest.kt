package io.github.windedge.num2words

import kotlin.test.Test

/**
 * Outputs one JSON line per call to [Num2Words.convert], consumed by
 * `scripts/verify_against_python.py`. Never asserts; safe to run in
 * the normal `gradle jvmTest` cycle (extra log lines only).
 */
class VerifyShimTest {
    private fun emit(lang: String, op: String, value: String, actual: Any?) {
        val s = actual?.toString() ?: "null"
        val safe = s.replace("\\", "\\\\").replace("\"", "\\\"")
        println("{\"lang\":\"$lang\",\"op\":\"$op\",\"value\":\"$value\",\"actual\":\"$safe\"}")
    }

    private fun emitCurrency(lang: String, value: String) {
        try {
            emit(lang, "currency", value, num2words(value, lang = lang, to = "currency"))
        } catch (e: Exception) {
            emit(lang, "currency", value, "ERR:${e::class.simpleName}:${e.message}")
        }
    }

    private val cardinals = listOf("0", "1", "2", "10", "11", "13", "19", "20",
        "100", "1000", "10000", "1000000", "123456789", "1.5", "3.14", "-7",
        "0.1", "2.675")

    private fun cardinal(lang: String) =
        cardinals.forEach { v -> emit(lang, "cardinal", v, num2words(v, lang = lang)) }

    @Test fun cardinalEn() = cardinal("en")
    @Test fun cardinalZh() = cardinal("zh")
    @Test fun cardinalZhCn() = cardinal("zh_CN")
    @Test fun cardinalZhHk() = cardinal("zh_HK")
    @Test fun cardinalZhTw() = cardinal("zh_TW")
    @Test fun cardinalDe() = cardinal("de")
    @Test fun cardinalFr() = cardinal("fr")
    @Test fun cardinalEs() = cardinal("es")
    @Test fun cardinalIt() = cardinal("it")
    @Test fun cardinalPt() = cardinal("pt")
    @Test fun cardinalJa() = cardinal("ja")
    @Test fun cardinalKo() = cardinal("ko")
    @Test fun cardinalAr() = cardinal("ar")

    @Test fun ordinalEn() = listOf("1", "5", "10", "21", "100").forEach { v -> emit("en", "ordinal", v, num2words(v, lang = "en", to = "ordinal")) }
    @Test fun ordinalZh() = listOf("0", "2", "10", "11", "109").forEach { v -> emit("zh", "ordinal", v, num2words(v, lang = "zh", to = "ordinal")) }
    @Test fun ordinalDe() = listOf("0", "1", "7", "100", "1000").forEach { v -> emit("de", "ordinal", v, num2words(v, lang = "de", to = "ordinal")) }
    @Test fun ordinalFr() = listOf("1", "5", "8", "9", "35").forEach { v -> emit("fr", "ordinal", v, num2words(v, lang = "fr", to = "ordinal")) }
    @Test fun ordinalEs() = listOf("1", "5", "10").forEach { v -> emit("es", "ordinal", v, num2words(v, lang = "es", to = "ordinal")) }
    @Test fun ordinalIt() = listOf("1", "8", "21", "100").forEach { v -> emit("it", "ordinal", v, num2words(v, lang = "it", to = "ordinal")) }
    @Test fun ordinalJa() = listOf("0", "2", "3").forEach { v -> emit("ja", "ordinal", v, num2words(v, lang = "ja", to = "ordinal")) }
    @Test fun ordinalKo() = listOf("1", "2", "5", "10").forEach { v -> emit("ko", "ordinal", v, num2words(v, lang = "ko", to = "ordinal")) }
    @Test fun ordinalAr() = listOf("1", "2", "3", "20", "23").forEach { v -> emit("ar", "ordinal", v, num2words(v.toInt(), lang = "ar", to = "ordinal")) }

    @Test fun ordinalNumEn() = listOf("10", "21", "102", "73").forEach { v -> emit("en", "ordinal_num", v, num2words(v, lang = "en", to = "ordinal_num")) }
    @Test fun ordinalNumFr() = listOf("1", "8", "21", "1000").forEach { v -> emit("fr", "ordinal_num", v, num2words(v, lang = "fr", to = "ordinal_num")) }
    @Test fun ordinalNumIt() = listOf("1", "8", "21", "100").forEach { v -> emit("it", "ordinal_num", v, num2words(v, lang = "it", to = "ordinal_num")) }

    @Test fun yearEn() = listOf("2002", "1780", "1990", "2001").forEach { v -> emit("en", "year", v, num2words(v, lang = "en", to = "year")) }
    @Test fun yearDe() = listOf("2002", "1780").forEach { v -> emit("de", "year", v, num2words(v, lang = "de", to = "year")) }
    @Test fun yearFr() = listOf("2020", "2000").forEach { v -> emit("fr", "year", v, num2words(v, lang = "fr", to = "year")) }
    @Test fun yearEs() = listOf("2020", "1492").forEach { v -> emit("es", "year", v, num2words(v, lang = "es", to = "year")) }
    @Test fun yearIt() = listOf("2020", "2000", "1000").forEach { v -> emit("it", "year", v, num2words(v, lang = "it", to = "year")) }
    @Test fun yearPt() = listOf("2020", "1001", "-30").forEach { v -> emit("pt", "year", v, num2words(v, lang = "pt", to = "year")) }
    @Test fun yearJa() = run {
        listOf("2021", "2019", "2018", "645").forEach { v -> emit("ja", "year", v, num2words(v, lang = "ja", to = "year")) }
        emit("ja", "year", "-99", num2words("-99", lang = "ja", to = "year", options = mapOf("era" to false)))
    }
    @Test fun yearKo() = listOf("2000", "2018", "1954", "-1000").forEach { v -> emit("ko", "year", v, num2words(v, lang = "ko", to = "year")) }
    @Test fun yearAr() = listOf("2000", "1900").forEach { v -> emit("ar", "year", v, num2words(v, lang = "ar", to = "year")) }

    private val currValues = listOf("1.00", "2.01", "8.10", "12.26", "21.29", "81.25", "100.00")

    @Test fun currEn() = currValues.forEach { v -> emit("en", "currency", v, num2words(v, lang = "en", to = "currency", options = mapOf("currency" to "USD"))) }
    @Test fun currZh() = currValues.forEach { v -> emit("zh", "currency", v, num2words(v, lang = "zh", to = "currency")) }
    @Test fun currZhCn() = currValues.forEach { v -> emit("zh_CN", "currency", v, num2words(v, lang = "zh_CN", to = "currency")) }
    @Test fun currZhHk() = currValues.forEach { v -> emit("zh_HK", "currency", v, num2words(v, lang = "zh_HK", to = "currency")) }
    @Test fun currZhTw() = currValues.forEach { v -> emit("zh_TW", "currency", v, num2words(v, lang = "zh_TW", to = "currency")) }
    @Test fun currDe() = currValues.forEach { v -> emit("de", "currency", v, num2words(v, lang = "de", to = "currency", options = mapOf("currency" to "EUR"))) }
    @Test fun currFr() = currValues.forEach { v -> emit("fr", "currency", v, num2words(v, lang = "fr", to = "currency", options = mapOf("currency" to "EUR"))) }
    @Test fun currEs() = currValues.forEach { v -> emit("es", "currency", v, num2words(v, lang = "es", to = "currency")) }
    @Test fun currIt() = currValues.forEach { v -> emit("it", "currency", v, num2words(v, lang = "it", to = "currency", options = mapOf("currency" to "EUR"))) }
    @Test fun currPt() = currValues.forEach { v -> emit("pt", "currency", v, num2words(v, lang = "pt", to = "currency")) }
    @Test fun currJa() = currValues.forEach { v -> emitCurrency("ja", v) }
    @Test fun currKo() = currValues.forEach { v -> emitCurrency("ko", v) }
    @Test fun currAr() = currValues.forEach { v -> emit("ar", "currency", v, num2words(v, lang = "ar", to = "currency")) }
}