package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyTest {
    @Test
    fun parseIntegerWithCents() {
        assertEquals(Triple(BigInt.fromLong(1), 1, false), parseCurrencyParts(101L))
        assertEquals(Triple(BigInt.fromLong(1), 23, true), parseCurrencyParts(-123L))
    }

    @Test
    fun parseIntegerWithoutCents() {
        assertEquals(
            Triple(BigInt.fromLong(101), 0, false),
            parseCurrencyParts(101L, isIntWithCents = false),
        )
        assertEquals(
            Triple(BigInt.fromLong(123), 0, true),
            parseCurrencyParts(-123L, isIntWithCents = false),
        )
    }

    @Test
    fun parseFloat() {
        assertEquals(Triple(BigInt.fromLong(1), 1, false), parseCurrencyParts(1.01))
        assertEquals(Triple(BigInt.fromLong(1), 23, true), parseCurrencyParts(-1.23))
        assertEquals(Triple(BigInt.fromLong(1), 20, true), parseCurrencyParts(-1.2))
        assertEquals(Triple(BigInt.ZERO, 0, false), parseCurrencyParts(0.004))
        assertEquals(Triple(BigInt.ZERO, 1, false), parseCurrencyParts(0.005))
        assertEquals(Triple(BigInt.ZERO, 1, false), parseCurrencyParts(0.006))
        assertEquals(Triple(BigInt.ZERO, 0, false), parseCurrencyParts(0.0005))
        assertEquals(Triple(BigInt.ZERO, 98, false), parseCurrencyParts(0.984))
        assertEquals(Triple(BigInt.ZERO, 99, false), parseCurrencyParts(0.989))
        assertEquals(Triple(BigInt.ZERO, 99, false), parseCurrencyParts(0.994))
        assertEquals(Triple(BigInt.fromLong(1), 0, false), parseCurrencyParts(0.999))
    }

    @Test
    fun parseDecimal() {
        assertEquals(
            Triple(BigInt.fromLong(1), 1, false),
            parseCurrencyParts(SimpleDecimal.fromString("1.01")),
        )
        assertEquals(
            Triple(BigInt.fromLong(1), 23, true),
            parseCurrencyParts(SimpleDecimal.fromString("-1.23")),
        )
        assertEquals(
            Triple(BigInt.fromLong(1), 23, true),
            parseCurrencyParts(SimpleDecimal.fromString("-1.233")),
        )
        assertEquals(
            Triple(BigInt.fromLong(1), 99, true),
            parseCurrencyParts(SimpleDecimal.fromString("-1.989")),
        )
    }

    @Test
    fun parseString() {
        assertEquals(Triple(BigInt.fromLong(1), 1, false), parseCurrencyParts("1.01"))
        assertEquals(Triple(BigInt.fromLong(1), 23, true), parseCurrencyParts("-1.23"))
        assertEquals(Triple(BigInt.fromLong(1), 20, true), parseCurrencyParts("-1.2"))
        assertEquals(Triple(BigInt.fromLong(1), 0, false), parseCurrencyParts("1"))
    }

    @Test
    fun unsupportedTypeFails() {
        assertFailsWith<Num2WordsValueError> { parseCurrencyParts(true) }
    }
}

class UtilsTest {
    @Test
    fun splitByXInt() {
        assertEquals(listOf(12), splitByX("12", 3))
        assertEquals(listOf(1, 234), splitByX("1234", 3))
        assertEquals(listOf(12, 345, 678, 900), splitByX("12345678900", 3))
        assertEquals(listOf(1, 0), splitByX("1000000", 6))
    }

    @Test
    fun splitByXString() {
        assertEquals(listOf("12"), splitByX("12", 3, formatInt = false))
        assertEquals(listOf("1", "234"), splitByX("1234", 3, formatInt = false))
        assertEquals(
            listOf("12", "345", "678", "900"),
            splitByX("12345678900", 3, formatInt = false),
        )
        assertEquals(listOf("1", "000000"), splitByX("1000000", 6, formatInt = false))
    }

    @Test
    fun getDigitsCases() {
        assertEquals(listOf(2, 1, 0), getDigits(12))
        assertEquals(listOf(4, 3, 2), getDigits(234))
    }
}

class BaseTest {
    private val base = Num2WordBase()

    @Test
    fun currencyNotImplementedWithoutForms() {
        assertFailsWith<Num2WordsNotImplemented> {
            base.toCurrency(NumValue.Decimal(SimpleDecimal.fromString("1.00")), currency = "EUR")
        }
    }

    @Test
    fun cardinalFloatRejectsGarbage() {
        assertFailsWith<Num2WordsValueError> { SimpleDecimal.fromString("a") }
    }

    @Test
    fun mergeNotImplemented() {
        assertFailsWith<Num2WordsNotImplemented> {
            base.merge("a" to BigInt.fromLong(2), "b" to BigInt.fromLong(3))
        }
    }

    @Test
    fun titleFlag() {
        assertEquals("one", base.title("one"))
        base.isTitle = true
        assertEquals("One", base.title("one"))
        base.excludeTitle.add("one")
        assertEquals("one", base.title("one"))
    }

    @Test
    fun setHighNumwordsNotImplemented() {
        assertFailsWith<Num2WordsNotImplemented> { base.setHighNumwords(emptyList()) }
    }

    @Test
    fun ordinalNumIdentity() {
        assertEquals(BigInt.fromLong(1), base.toOrdinalNum(NumValue.Whole(BigInt.fromLong(1))))
        assertEquals(BigInt.fromLong(100), base.toOrdinalNum(NumValue.Whole(BigInt.fromLong(100))))
        assertEquals(BigInt.fromLong(1000), base.toOrdinalNum(NumValue.Whole(BigInt.fromLong(1000))))
    }

    @Test
    fun pluralizeNotImplemented() {
        assertFailsWith<Num2WordsNotImplemented> { base.pluralize(BigInt.ZERO, emptyList()) }
    }
}
