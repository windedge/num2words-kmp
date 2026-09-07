package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals

private fun n2zhCn(number: Long, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_CN", to = to, options = options)

private fun n2zhCn(number: Int, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_CN", to = to, options = options)

private fun n2zhCn(number: Double, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_CN", to = to, options = options)

private fun n2zhCn(number: String, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_CN", to = to, options = options)

/** Golden samples from Python tests/test_zh_cn.py. */
class ZhCnTest {
    @Test
    fun low() {
        assertEquals("二", n2zhCn(2))
        assertEquals("贰", n2zhCn(2, options = mapOf("reading" to "capital")))
        assertEquals("六", n2zhCn(6))
        assertEquals("陆", n2zhCn(6, options = mapOf("reading" to "capital")))
    }

    @Test
    fun high() {
        assertEquals("一万", n2zhCn(10000))
        assertEquals("壹万", n2zhCn("10000", options = mapOf("reading" to "capital")))
        assertEquals("一万二千三百四十五", n2zhCn(12345))
        assertEquals("壹万贰仟叁佰肆拾伍", n2zhCn("12345", options = mapOf("reading" to "capital")))
        assertEquals("一亿", n2zhCn(100000000))
        assertEquals("壹亿", n2zhCn(100000000, options = mapOf("reading" to "capital")))
        assertEquals("十二亿三千四百五十六万七千八百九十", n2zhCn(1234567890))
        assertEquals(
            "壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾",
            n2zhCn(1234567890, options = mapOf("reading" to "capital")),
        )
        assertEquals(
            "一千二百三十四京五千六百七十八兆九千零一十二亿三千四百五十六万七千八百九十",
            n2zhCn("12345678901234567890"),
        )
        assertEquals("一百二十兆零七百八十九亿零五十万零九十", n2zhCn("120078900500090"))
    }

    @Test
    fun mid() {
        assertEquals("负一百二十三", n2zhCn(-123))
        assertEquals("负壹仟", n2zhCn(-1000, options = mapOf("reading" to "capital")))
    }

    @Test
    fun cardinalFloat() {
        assertEquals("零点一二三四五六七八九", n2zhCn(0.123456789))
        assertEquals("一亿点零一", n2zhCn(100000000.01))
        assertEquals("壹亿点零壹", n2zhCn(100000000.01, options = mapOf("reading" to "capital")))
    }

    @Test
    fun currency() {
        assertEquals("零圆正", n2zhCn("0", to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("伍圆正", n2zhCn(5.00, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("零元", n2zhCn("0", to = "currency"))
        assertEquals("五元", n2zhCn(5.00, to = "currency"))
        assertEquals("壹拾圆伍分", n2zhCn(10.05, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("十元零五分", n2zhCn(10.05, to = "currency"))
        assertEquals("壹拾贰圆壹角贰分", n2zhCn(12.12, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("壹佰贰拾叁万伍仟陆佰柒拾捌圆正", n2zhCn(1235678, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals(
            "壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾圆壹角贰分",
            n2zhCn("1234567890.123", to = "currency", options = mapOf("reading" to "capital")),
        )
        assertEquals("六万七千八百九十元一角三分", n2zhCn(67890.126, to = "currency"))
        assertEquals(
            "美元玖拾捌万柒仟陆佰伍拾肆圆叁角",
            n2zhCn(987654.3, to = "currency", options = mapOf("currency" to "USD", "reading" to "capital")),
        )
        assertEquals(
            "美元九十八万七千六百五十四元三角",
            n2zhCn(987654.3, to = "currency", options = mapOf("currency" to "USD")),
        )
        assertEquals(
            "欧元一百三十五元七角九分",
            n2zhCn(135.79, to = "currency", options = mapOf("currency" to "EUR")),
        )
    }
}
