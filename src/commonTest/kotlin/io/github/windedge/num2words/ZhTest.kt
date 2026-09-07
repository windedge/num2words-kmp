package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun n2zh(number: Long, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh", to = to, options = options)

private fun n2zh(number: Int, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh", to = to, options = options)

private fun n2zh(number: Double, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh", to = to, options = options)

private fun n2zh(number: String, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh", to = to, options = options)

/** Golden samples from Python tests/test_zh.py. */
class ZhTest {
    @Test
    fun low() {
        assertEquals("零", n2zh(0))
        assertEquals("零", n2zh(0, options = mapOf("reading" to "capital")))
        assertEquals("一", n2zh(1))
        assertEquals("壹", n2zh(1, options = mapOf("reading" to "capital")))
        assertEquals("二", n2zh(2))
        assertEquals("貳", n2zh(2, options = mapOf("reading" to "capital")))
        assertEquals("三", n2zh(3))
        assertEquals("叁", n2zh(3, options = mapOf("reading" to "capital")))
        assertEquals("四", n2zh(4))
        assertEquals("肆", n2zh(4, options = mapOf("reading" to "capital")))
        assertEquals("五", n2zh(5))
        assertEquals("伍", n2zh(5, options = mapOf("reading" to "capital")))
        assertEquals("六", n2zh(6))
        assertEquals("陸", n2zh(6, options = mapOf("reading" to "capital")))
        assertEquals("七", n2zh(7))
        assertEquals("柒", n2zh(7, options = mapOf("reading" to "capital")))
        assertEquals("八", n2zh(8))
        assertEquals("捌", n2zh(8, options = mapOf("reading" to "capital")))
        assertEquals("九", n2zh(9))
        assertEquals("玖", n2zh(9, options = mapOf("reading" to "capital")))
        assertEquals("十", n2zh(10))
        assertEquals("壹拾", n2zh(10, options = mapOf("reading" to "capital")))
        assertEquals("十一", n2zh(11))
        assertEquals("壹拾壹", n2zh(11, options = mapOf("reading" to "capital")))
        assertEquals("十二", n2zh(12))
        assertEquals("壹拾貳", n2zh(12, options = mapOf("reading" to "capital")))
        assertEquals("十三", n2zh(13))
        assertEquals("壹拾叁", n2zh(13, options = mapOf("reading" to "capital")))
        assertEquals("十四", n2zh(14))
        assertEquals("壹拾肆", n2zh(14, options = mapOf("reading" to "capital")))
        assertEquals("十五", n2zh(15))
        assertEquals("壹拾伍", n2zh(15, options = mapOf("reading" to "capital")))
        assertEquals("十六", n2zh(16))
        assertEquals("壹拾陸", n2zh(16, options = mapOf("reading" to "capital")))
        assertEquals("十七", n2zh(17))
        assertEquals("壹拾柒", n2zh(17, options = mapOf("reading" to "capital")))
        assertEquals("十八", n2zh(18))
        assertEquals("壹拾捌", n2zh(18, options = mapOf("reading" to "capital")))
        assertEquals("十九", n2zh(19))
        assertEquals("壹拾玖", n2zh(19, options = mapOf("reading" to "capital")))
        assertEquals("二十", n2zh(20))
        assertEquals("貳拾", n2zh(20, options = mapOf("reading" to "capital")))
    }

    @Test
    fun mid() {
        assertEquals("一百", n2zh(100))
        assertEquals("壹佰", n2zh(100, options = mapOf("reading" to "capital")))
        assertEquals("負一百二十三", n2zh(-123))
        assertEquals("壹佰貳拾叁", n2zh(123, options = mapOf("reading" to "capital")))
        assertEquals("三百", n2zh(300))
        assertEquals("叁佰", n2zh("300", options = mapOf("reading" to "capital")))
        assertEquals("一千", n2zh(1000))
        assertEquals("負壹仟", n2zh(-1000, options = mapOf("reading" to "capital")))
        assertEquals("捌仟", n2zh("8000", options = mapOf("reading" to "capital")))
    }

    @Test
    fun high() {
        assertEquals("一萬", n2zh(10000))
        assertEquals("壹萬", n2zh("10000", options = mapOf("reading" to "capital")))
        assertEquals("一萬二千三百四十五", n2zh(12345))
        assertEquals("壹萬貳仟叁佰肆拾伍", n2zh("12345", options = mapOf("reading" to "capital")))
        assertEquals("一億", n2zh(100000000))
        assertEquals("壹億", n2zh(100000000, options = mapOf("reading" to "capital")))
        assertEquals("十二億三千四百五十六萬七千八百九十", n2zh(1234567890))
        assertEquals(
            "壹拾貳億叁仟肆佰伍拾陸萬柒仟捌佰玖拾",
            n2zh(1234567890, options = mapOf("reading" to "capital")),
        )
        assertEquals(
            "一千二百三十四京五千六百七十八兆九千零一十二億三千四百五十六萬七千八百九十",
            n2zh("12345678901234567890"),
        )
        assertEquals("一百二十兆零七百八十九億零五十萬零九十", n2zh("120078900500090"))
        assertFailsWith<Num2WordsOverflowError> { n2zh("1" + "0".repeat(100)) }
    }

    @Test
    fun stuffZero() {
        assertEquals("一百二十萬零三千四百零五", n2zh(1203405, options = mapOf("stuff_zero" to 1)))
        assertEquals("一百二十萬三千四百零五", n2zh(1203405, options = mapOf("stuff_zero" to 2)))
        assertEquals("一百二十萬三千四百五", n2zh(1203405, options = mapOf("stuff_zero" to 3)))
        assertEquals("九億零八百零七萬零六百零五", n2zh(908070605, options = mapOf("stuff_zero" to 1)))
        assertEquals("九億零八百零七萬零六百零五", n2zh(908070605, options = mapOf("stuff_zero" to 2)))
        assertEquals("九億八百七萬六百五", n2zh(908070605, options = mapOf("stuff_zero" to 3)))
        assertEquals("十二億零三萬四千零五", n2zh(1200034005, options = mapOf("stuff_zero" to 1)))
        assertEquals("十二億零三萬四千零五", n2zh(1200034005, options = mapOf("stuff_zero" to 2)))
        assertEquals("十二億三萬四千五", n2zh(1200034005, options = mapOf("stuff_zero" to 3)))
        assertEquals("五百萬零六", n2zh(5000006, options = mapOf("stuff_zero" to 1)))
        assertEquals("五百萬零六", n2zh(5000006, options = mapOf("stuff_zero" to 2)))
        assertEquals("五百萬六", n2zh(5000006, options = mapOf("stuff_zero" to 3)))
        assertEquals("一百零二兆零三十億零四千萬", n2zh(102003040000000, options = mapOf("stuff_zero" to 1)))
        assertEquals("一百零二兆零三十億四千萬", n2zh(102003040000000, options = mapOf("stuff_zero" to 2)))
        assertEquals("一百二兆三十億四千萬", n2zh(102003040000000, options = mapOf("stuff_zero" to 3)))
    }

    @Test
    fun cardinalFloat() {
        assertEquals("零點一二三四五六七八九", n2zh(0.123456789))
        assertEquals("一億點零一", n2zh(100000000.01))
        assertEquals("壹億點零壹", n2zh(100000000.01, options = mapOf("reading" to "capital")))
    }

    @Test
    fun ordinal() {
        assertEquals("第零", n2zh(0, to = "ordinal"))
        assertEquals("第二", n2zh(2, to = "ordinal"))
        assertEquals("第十", n2zh(10, to = "ordinal"))
        assertEquals("第十一", n2zh(11, to = "ordinal"))
        assertEquals("第十九", n2zh("19", to = "ordinal"))
        assertEquals("第一百零九", n2zh(109, to = "ordinal"))
        assertEquals("第二名", n2zh(2, to = "ordinal", options = mapOf("counter" to "名")))
        assertEquals("第三位", n2zh(3, to = "ordinal", options = mapOf("counter" to "位")))
    }

    @Test
    fun ordinalNum() {
        assertEquals("第1.5", n2zh(1.5, to = "ordinal_num"))
        assertEquals("第120", n2zh(120, to = "ordinal_num"))
    }

    @Test
    fun currency() {
        assertEquals("零圓整", n2zh("0", to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("伍圓整", n2zh(5.00, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("零元", n2zh("0", to = "currency"))
        assertEquals("五元", n2zh(5.00, to = "currency"))
        assertEquals("壹拾圓伍分", n2zh(10.05, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("十元零五分", n2zh(10.05, to = "currency"))
        assertEquals("壹拾貳圓壹角貳分", n2zh(12.12, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("壹佰貳拾叁萬伍仟陸佰柒拾捌圓整", n2zh(1235678, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals(
            "壹拾貳億叁仟肆佰伍拾陸萬柒仟捌佰玖拾圓壹角貳分",
            n2zh("1234567890.123", to = "currency", options = mapOf("reading" to "capital")),
        )
        assertEquals("六萬七千八百九十元一角三分", n2zh(67890.126, to = "currency"))
        assertEquals(
            "美元玖拾捌萬柒仟陸佰伍拾肆圓叁角",
            n2zh(987654.3, to = "currency", options = mapOf("currency" to "USD", "reading" to "capital")),
        )
        assertEquals(
            "美元九十八萬七千六百五十四元三角",
            n2zh(987654.3, to = "currency", options = mapOf("currency" to "USD")),
        )
        assertEquals(
            "歐元一百三十五元七角九分",
            n2zh(135.79, to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertFailsWith<Num2WordsNotImplemented> {
            n2zh(10, to = "currency", options = mapOf("currency" to "ABC"))
        }
    }

    @Test
    fun year() {
        assertEquals("二〇二〇年", n2zh(2020, to = "year", options = mapOf("prefer" to listOf("〇"))))
        assertEquals("二零二零年", n2zh(2020, to = "year"))
        assertEquals("二零二零年", n2zh(2020.0, to = "year"))
        assertEquals("公元二零二零年", n2zh(2020, to = "year", options = mapOf("reading" to "capital")))
        assertEquals(
            "西元二零二零年",
            n2zh(2020, to = "year", options = mapOf("reading" to "capital", "prefer" to listOf("西元"))),
        )
        assertEquals("公元前一年", n2zh(-1, to = "year"))
        assertEquals("西元前一年", n2zh(-1, to = "year", options = mapOf("prefer" to listOf("西元"))))
        assertFailsWith<Num2WordsValueError> { n2zh(2020.1, to = "year") }
    }
}
