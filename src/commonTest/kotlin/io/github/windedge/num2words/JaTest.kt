package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun n2j(number: Int, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "ja", to = to, options = options)

private fun n2j(number: Long, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "ja", to = to, options = options)

private fun n2j(number: Double, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "ja", to = to, options = options)

private fun n2j(number: String, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "ja", to = to, options = options)

/** Golden samples from Python tests/test_ja.py. */
class JaTest {
    @Test
    fun low() {
        assertEquals("零", n2j(0))
        assertEquals("〇", n2j(0, options = mapOf("prefer" to listOf("〇"))))
        assertEquals("ゼロ", n2j(0, options = mapOf("reading" to true)))
        assertEquals("れい", n2j(0, options = mapOf("reading" to true, "prefer" to listOf("れい"))))
        assertEquals("一", n2j(1))
        assertEquals("いち", n2j(1, options = mapOf("reading" to true)))
        assertEquals("二", n2j(2))
        assertEquals("に", n2j(2, options = mapOf("reading" to true)))
        assertEquals("三", n2j(3))
        assertEquals("さん", n2j(3, options = mapOf("reading" to true)))
        assertEquals("四", n2j(4))
        assertEquals("よん", n2j(4, options = mapOf("reading" to true)))
        assertEquals("し", n2j(4, options = mapOf("reading" to true, "prefer" to listOf("し"))))
        assertEquals("五", n2j(5))
        assertEquals("ご", n2j(5, options = mapOf("reading" to true)))
        assertEquals("六", n2j(6))
        assertEquals("ろく", n2j(6, options = mapOf("reading" to true)))
        assertEquals("七", n2j(7))
        assertEquals("なな", n2j(7, options = mapOf("reading" to true)))
        assertEquals("しち", n2j(7, options = mapOf("reading" to true, "prefer" to listOf("しち"))))
        assertEquals("八", n2j(8))
        assertEquals("はち", n2j(8, options = mapOf("reading" to true)))
        assertEquals("九", n2j(9))
        assertEquals("きゅう", n2j(9, options = mapOf("reading" to true)))
        assertEquals("十", n2j(10))
        assertEquals("じゅう", n2j(10, options = mapOf("reading" to true)))
        assertEquals("十一", n2j(11))
        assertEquals("じゅういち", n2j(11, options = mapOf("reading" to true)))
        assertEquals("十二", n2j(12))
        assertEquals("じゅうに", n2j(12, options = mapOf("reading" to true)))
        assertEquals("十三", n2j(13))
        assertEquals("じゅうさん", n2j(13, options = mapOf("reading" to true)))
        assertEquals("十四", n2j(14))
        assertEquals("じゅうよん", n2j(14, options = mapOf("reading" to true)))
        assertEquals("じゅうし", n2j(14, options = mapOf("reading" to true, "prefer" to listOf("し"))))
        assertEquals("十五", n2j(15))
        assertEquals("じゅうご", n2j(15, options = mapOf("reading" to true)))
        assertEquals("十六", n2j(16))
        assertEquals("じゅうろく", n2j(16, options = mapOf("reading" to true)))
        assertEquals("十七", n2j(17))
        assertEquals("じゅうなな", n2j(17, options = mapOf("reading" to true)))
        assertEquals("じゅうしち", n2j(17, options = mapOf("reading" to true, "prefer" to listOf("しち"))))
        assertEquals("十八", n2j(18))
        assertEquals("じゅうはち", n2j(18, options = mapOf("reading" to true)))
        assertEquals("十九", n2j(19))
        assertEquals("じゅうきゅう", n2j(19, options = mapOf("reading" to true)))
        assertEquals("二十", n2j(20))
        assertEquals("にじゅう", n2j(20, options = mapOf("reading" to true)))
    }

    @Test
    fun mid() {
        assertEquals("百", n2j(100))
        assertEquals("ひゃく", n2j(100, options = mapOf("reading" to true)))
        assertEquals("百二十三", n2j(123))
        assertEquals("ひゃくにじゅうさん", n2j(123, options = mapOf("reading" to true)))
        assertEquals("三百", n2j(300))
        assertEquals("さんびゃく", n2j(300, options = mapOf("reading" to true)))
        assertEquals("四百", n2j(400))
        assertEquals("よんひゃく", n2j(400, options = mapOf("reading" to true)))
        assertEquals("六百", n2j(600))
        assertEquals("ろっぴゃく", n2j(600, options = mapOf("reading" to true)))
        assertEquals("しちひゃく", n2j(700, options = mapOf("reading" to true, "prefer" to listOf("しち"))))
        assertEquals("はっぴゃく", n2j(800, options = mapOf("reading" to true)))
        assertEquals("千", n2j(1000))
        assertEquals("せん", n2j(1000, options = mapOf("reading" to true)))
        assertEquals("さんぜん", n2j(3000, options = mapOf("reading" to true)))
        assertEquals("はっせん", n2j(8000, options = mapOf("reading" to true)))
    }

    @Test
    fun high() {
        assertEquals("一万", n2j(10000))
        assertEquals("いちまん", n2j(10000, options = mapOf("reading" to true)))
        assertEquals("一万二千三百四十五", n2j(12345))
        assertEquals(
            "いちまんにせんさんびゃくよんじゅうご",
            n2j(12345, options = mapOf("reading" to true)),
        )
        assertEquals("一億", n2j(100000000))
        assertEquals("いちおく", n2j(100000000, options = mapOf("reading" to true)))
        assertEquals("一億二千三百四十五万六千七百八十九", n2j(123456789))
        assertEquals(
            "いちおくにせんさんびゃくよんじゅうごまんろくせんななひゃくはちじゅうきゅう",
            n2j(123456789, options = mapOf("reading" to true)),
        )
        assertEquals("一兆", n2j(1000000000000L))
        assertEquals("いっちょう", n2j(1000000000000L, options = mapOf("reading" to true)))
        assertEquals("一兆二千三百四十五億六千七百八十九万百二十三", n2j(1234567890123L))
        assertEquals(
            "いっちょうにせんさんびゃくよんじゅうごおくろくせんななひゃくはちじゅうきゅうまんひゃくにじゅうさん",
            n2j(1234567890123L, options = mapOf("reading" to true)),
        )
    }

    @Test
    fun cardinalFloat() {
        assertEquals(
            "〇点〇一二三四五六七八九",
            n2j(0.0123456789, options = mapOf("prefer" to listOf("〇"))),
        )
        assertEquals(
            "れいてんれいいちにさんよんごろくななはちきゅう",
            n2j(0.0123456789, options = mapOf("reading" to true)),
        )
        assertEquals("一億点零一", n2j(100000000.01))
        assertEquals("いちおくてんれいいち", n2j(100000000.01, options = mapOf("reading" to true)))
    }

    @Test
    fun ordinal() {
        assertEquals("零番目", n2j(0, to = "ordinal"))
        assertEquals(
            "れいばんめ",
            n2j(0, to = "ordinal", options = mapOf("reading" to true, "prefer" to listOf("れい"))),
        )
        assertEquals("二人目", n2j(2, to = "ordinal", options = mapOf("counter" to "人")))
        assertEquals("三つ目", n2j(3, to = "ordinal", options = mapOf("counter" to "つ")))
        assertFailsWith<Num2WordsNotImplemented> {
            n2j(4, to = "ordinal", options = mapOf("reading" to true, "counter" to "人"))
        }
    }

    @Test
    fun ordinalNum() {
        assertEquals("0番目", n2j(0, to = "ordinal_num"))
        assertEquals("0ばんめ", n2j(0, to = "ordinal_num", options = mapOf("reading" to true)))
        assertEquals("2人目", n2j(2, to = "ordinal_num", options = mapOf("counter" to "人")))
        assertEquals("3つ目", n2j(3, to = "ordinal_num", options = mapOf("counter" to "つ")))
    }

    @Test
    fun currency() {
        assertEquals("一億二千三百四十五万六千七百八十九円", n2j(123456789, to = "currency"))
        assertEquals(
            "いちおくにせんさんびゃくよんじゅうごまんろくせんななひゃくはちじゅうきゅうえん",
            n2j(123456789, to = "currency", options = mapOf("reading" to true)),
        )
    }

    @Test
    fun year() {
        assertEquals("令和三年", n2j(2021, to = "year"))
        assertEquals("れいわさんねん", n2j(2021, to = "year", options = mapOf("reading" to true)))
        assertEquals("令和3年", n2j(2021, to = "year", options = mapOf("reading" to "arabic")))
        assertEquals("令和元年", n2j(2019, to = "year"))
        assertEquals("れいわがんねん", n2j(2019, to = "year", options = mapOf("reading" to true)))
        assertEquals("令和1年", n2j(2019, to = "year", options = mapOf("reading" to "arabic")))
        assertEquals("平成三十年", n2j(2018, to = "year"))
        assertEquals("へいせいさんじゅうねん", n2j(2018, to = "year", options = mapOf("reading" to true)))
        assertEquals("平成30年", n2j(2018, to = "year", options = mapOf("reading" to "arabic")))
        assertEquals("平成二十九年", n2j(2017, to = "year"))
        assertEquals("へいせいにじゅうくねん", n2j(2017, to = "year", options = mapOf("reading" to true)))
        assertEquals("平成29年", n2j(2017, to = "year", options = mapOf("reading" to "arabic")))
        assertEquals("二千九年", n2j(2009, to = "year", options = mapOf("era" to false)))
        assertEquals(
            "にせんくねん",
            n2j(2009, to = "year", options = mapOf("reading" to true, "era" to false)),
        )
        assertEquals("二千年", n2j(2000, to = "year", options = mapOf("era" to false)))
        assertEquals(
            "にせんねん",
            n2j(2000, to = "year", options = mapOf("era" to false, "reading" to true)),
        )
        assertEquals("大化元年", n2j(645, to = "year"))
        assertEquals("たいかがんねん", n2j(645, to = "year", options = mapOf("reading" to true)))
        assertEquals("紀元前九十九年", n2j(-99, to = "year", options = mapOf("era" to false)))
        assertEquals(
            "きげんぜんきゅうじゅうくねん",
            n2j(-99, to = "year", options = mapOf("era" to false, "reading" to true)),
        )
        assertEquals("天授元年", n2j(1375, to = "year"))
        assertEquals(
            "永和元年",
            n2j(1375, to = "year", options = mapOf("prefer" to listOf("えいわ"))),
        )
    }

    @Test
    fun rendakuMergePairs() {
        val e12 = BigInt.pow10(12)
        val e16 = BigInt.pow10(16)
        fun r(l: Pair<String, BigInt>, rr: Pair<String, BigInt>) =
            io.github.windedge.num2words.lang.rendakuMergePairs(l, rr)

        assertEquals("はっちょう" to (BigInt.fromLong(8) * e12), r("はち" to BigInt.fromLong(8), "ちょう" to e12))
        assertEquals("じゅっちょう" to (BigInt.fromLong(10) * e12), r("じゅう" to BigInt.fromLong(10), "ちょう" to e12))
        assertEquals("いっけい" to e16, r("いち" to BigInt.ONE, "けい" to e16))
        assertEquals("ろっけい" to (BigInt.fromLong(6) * e16), r("ろく" to BigInt.fromLong(6), "けい" to e16))
        assertEquals("はっけい" to (BigInt.fromLong(8) * e16), r("はち" to BigInt.fromLong(8), "けい" to e16))
        assertEquals("じゅっけい" to (BigInt.fromLong(10) * e16), r("じゅう" to BigInt.fromLong(10), "けい" to e16))
        assertEquals(
            "ひゃっけい" to (BigInt.fromLong(100) * e16),
            r("ひゃく" to BigInt.fromLong(100), "けい" to e16),
        )
    }
}
