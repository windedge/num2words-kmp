package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun n2zhTw(number: Long, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_TW", to = to, options = options)

private fun n2zhTw(number: Int, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_TW", to = to, options = options)

private fun n2zhTw(number: Double, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_TW", to = to, options = options)

/** Golden samples from Python tests/test_zh_tw.py. */
class ZhTwTest {
    @Test
    fun low() {
        assertEquals("負一", n2zhTw(-1))
        assertEquals("ㄈㄨˋㄧ", n2zhTw(-1, options = mapOf("reading" to true)))
    }

    @Test
    fun mid() {
        assertEquals("一百", n2zhTw(100))
        assertEquals("ㄧㄅㄞˇ", n2zhTw(100, options = mapOf("reading" to true)))
        assertEquals("一百零一", n2zhTw(101))
        assertEquals("ㄧㄅㄞˇㄌㄧㄥˊㄧ", n2zhTw(101, options = mapOf("reading" to true)))
        assertEquals("一百二十三", n2zhTw(123))
        assertEquals("ㄧㄅㄞˇㄦˋㄕˊㄙㄢ", n2zhTw(123, options = mapOf("reading" to true)))
        assertEquals("一千", n2zhTw(1000))
        assertEquals("ㄧㄑㄧㄢ", n2zhTw(1000, options = mapOf("reading" to true)))
        assertEquals("一千零一十", n2zhTw(1010))
        assertEquals("ㄧㄑㄧㄢㄌㄧㄥˊㄧㄕˊ", n2zhTw(1010, options = mapOf("reading" to true)))
    }

    @Test
    fun high() {
        assertEquals("一萬", n2zhTw(10000))
        assertEquals("ㄧㄨㄢˋ", n2zhTw(10000, options = mapOf("reading" to true)))
        assertEquals("一萬零一", n2zhTw(10001))
        assertEquals("ㄧㄨㄢˋㄌㄧㄥˊㄧ", n2zhTw(10001, options = mapOf("reading" to true)))
        assertEquals("十萬", n2zhTw(100000))
        assertEquals("ㄕˊㄨㄢˋ", n2zhTw(100000, options = mapOf("reading" to true)))
        assertEquals("十萬零一", n2zhTw(100001))
        assertEquals("ㄕˊㄨㄢˋㄌㄧㄥˊㄧ", n2zhTw(100001, options = mapOf("reading" to true)))
        assertEquals("一萬二千三百四十五", n2zhTw(12345))
        assertEquals(
            "ㄧㄨㄢˋㄦˋㄑㄧㄢㄙㄢㄅㄞˇㄙˋㄕˊㄨˇ",
            n2zhTw(12345, options = mapOf("reading" to true)),
        )
        assertEquals("一億", n2zhTw(100000000))
        assertEquals("ㄧㄧˋ", n2zhTw(100000000, options = mapOf("reading" to true)))
        assertEquals("五億零八十萬", n2zhTw(5 * 100000000 + 80 * 10000))
        assertEquals(
            "ㄨˇㄧˋㄌㄧㄥˊㄅㄚㄕˊㄨㄢˋ",
            n2zhTw(5 * 100000000 + 80 * 10000, options = mapOf("reading" to true)),
        )
        assertEquals("十億", n2zhTw(1000000000))
        assertEquals("ㄕˊㄧˋ", n2zhTw(1000000000, options = mapOf("reading" to true)))
        assertEquals("一億二千三百四十五萬六千七百八十九", n2zhTw(123456789))
        assertEquals(
            "ㄧㄧˋㄦˋㄑㄧㄢㄙㄢㄅㄞˇㄙˋㄕˊㄨˇㄨㄢˋㄌㄧㄡˋㄑㄧㄢㄑㄧㄅㄞˇㄅㄚㄕˊㄐㄧㄡˇ",
            n2zhTw(123456789, options = mapOf("reading" to true)),
        )
        assertEquals("四千零八十億", n2zhTw(4080 * 100000000L))
        assertEquals(
            "ㄙˋㄑㄧㄢㄌㄧㄥˊㄅㄚㄕˊㄧˋ",
            n2zhTw(4080 * 100000000L, options = mapOf("reading" to true)),
        )
        assertFailsWith<Num2WordsOverflowError> { n2zhTw("1" + "0".repeat(100)) }
    }

    @Test
    fun cardinalFloat() {
        assertEquals("零點零一二三四五六七八九", n2zhTw(0.0123456789))
        assertEquals(
            "ㄌㄧㄥˊㄉㄧㄢˇㄌㄧㄥˊㄧㄦˋㄙㄢㄙˋㄨˇㄌㄧㄡˋㄑㄧㄅㄚㄐㄧㄡˇ",
            n2zhTw(0.0123456789, options = mapOf("reading" to true)),
        )
        assertEquals("負零點零一二三四五六七八九", n2zhTw(-0.0123456789))
        assertEquals(
            "ㄈㄨˋㄌㄧㄥˊㄉㄧㄢˇㄌㄧㄥˊㄧㄦˋㄙㄢㄙˋㄨˇㄌㄧㄡˋㄑㄧㄅㄚㄐㄧㄡˇ",
            n2zhTw(-0.0123456789, options = mapOf("reading" to true)),
        )
        assertEquals("一億點零一", n2zhTw(100000000.01))
        assertEquals(
            "ㄧㄧˋㄉㄧㄢˇㄌㄧㄥˊㄧ",
            n2zhTw(100000000.01, options = mapOf("reading" to true)),
        )
    }

    @Test
    fun ordinal() {
        assertEquals("第零", n2zhTw(0, to = "ordinal"))
        assertEquals("ㄉㄧˋㄌㄧㄥˊ", n2zhTw(0, to = "ordinal", options = mapOf("reading" to true)))
        assertEquals(
            "ㄉㄧˋㄌㄧㄥˊ˙ㄍㄜ",
            n2zhTw(0, to = "ordinal", options = mapOf("counter" to "個", "reading" to true)),
        )
        assertEquals("第二名", n2zhTw(2, to = "ordinal", options = mapOf("counter" to "名")))
        assertEquals("第三位", n2zhTw(3, to = "ordinal", options = mapOf("counter" to "位")))
        assertFailsWith<Num2WordsNotImplemented> {
            n2zhTw(4, to = "ordinal", options = mapOf("reading" to true, "counter" to "隻"))
        }
    }

    @Test
    fun ordinalNum() {
        assertEquals("ㄉㄧˋ0", n2zhTw(0, to = "ordinal_num", options = mapOf("reading" to true)))
        assertEquals(
            "ㄉㄧˋ0˙ㄍㄜ",
            n2zhTw(0, to = "ordinal_num", options = mapOf("counter" to "個", "reading" to true)),
        )
        assertFailsWith<Num2WordsNotImplemented> {
            n2zhTw(4, to = "ordinal_num", options = mapOf("reading" to true, "counter" to "隻"))
        }
    }

    @Test
    fun year() {
        assertEquals("民國元年", n2zhTw(1912, to = "year", options = mapOf("era" to true)))
        assertEquals(
            "民國1年",
            n2zhTw(1912, to = "year", options = mapOf("era" to true, "reading" to "arabic")),
        )
        assertEquals("民國二年", n2zhTw(1913, to = "year", options = mapOf("era" to true)))
        assertEquals("民國二十一年", n2zhTw(1932, to = "year", options = mapOf("era" to true)))
        assertEquals("民國一百年", n2zhTw(2011, to = "year", options = mapOf("era" to true)))
        assertEquals("民國一零一年", n2zhTw(2012, to = "year", options = mapOf("era" to true)))
        assertEquals("民國一一四年", n2zhTw(2025, to = "year", options = mapOf("era" to true)))
        assertEquals(
            "民國114年",
            n2zhTw(2025, to = "year", options = mapOf("era" to true, "reading" to "arabic")),
        )
        assertFailsWith<Num2WordsValueError> {
            n2zhTw(1911, to = "year", options = mapOf("era" to true))
        }
        assertEquals("二零二零年", n2zhTw(2020, to = "year"))
        assertFailsWith<Num2WordsValueError> {
            n2zhTw(2020.1, to = "year", options = mapOf("era" to true))
        }
    }
}

private fun n2zhTw(number: String, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_TW", to = to, options = options)
