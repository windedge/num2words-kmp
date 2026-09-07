package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals

private fun n2zhHk(number: Double, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_HK", to = to, options = options)

private fun n2zhHk(number: String, to: String = "cardinal", options: Map<String, Any?> = emptyMap()): Any =
    num2words(number, lang = "zh_HK", to = to, options = options)

/** Golden samples from Python tests/test_zh_hk.py. */
class ZhHkTest {
    @Test
    fun currency() {
        assertEquals("零圓整", n2zhHk("0", to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("伍圓整", n2zhHk(5.00, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("零元", n2zhHk("0", to = "currency"))
        assertEquals("五元", n2zhHk(5.00, to = "currency"))
        assertEquals("壹拾圓伍分", n2zhHk(10.05, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("十元零五仙", n2zhHk(10.05, to = "currency"))
        assertEquals("壹拾貳圓壹角貳分", n2zhHk(12.12, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals("壹佰貳拾叁萬伍仟陸佰柒拾捌圓整", n2zhHk(1235678.0, to = "currency", options = mapOf("reading" to "capital")))
        assertEquals(
            "壹拾貳億叁仟肆佰伍拾陸萬柒仟捌佰玖拾圓壹角貳分",
            n2zhHk("1234567890.123", to = "currency", options = mapOf("reading" to "capital")),
        )
        assertEquals("六萬七千八百九十元一毫三仙", n2zhHk(67890.126, to = "currency"))
        assertEquals(
            "美元玖拾捌萬柒仟陸佰伍拾肆圓叁角",
            n2zhHk(987654.3, to = "currency", options = mapOf("currency" to "USD", "reading" to "capital")),
        )
        assertEquals(
            "美元九十八萬七千六百五十四元三毫",
            n2zhHk(987654.3, to = "currency", options = mapOf("currency" to "USD")),
        )
        assertEquals(
            "歐羅一百三十五元七毫九仙",
            n2zhHk(135.79, to = "currency", options = mapOf("currency" to "EUR")),
        )
    }
}
