package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val DE_EUR = listOf(
    1.00 to "ein Euro und null Cent",
    2.01 to "zwei Euro und ein Cent",
    8.10 to "acht Euro und zehn Cent",
    12.26 to "zwölf Euro und sechsundzwanzig Cent",
    21.29 to "einundzwanzig Euro und neunundzwanzig Cent",
    81.25 to "einundachtzig Euro und fünfundzwanzig Cent",
    100.00 to "einhundert Euro und null Cent",
)

private val DE_USD = listOf(
    1.00 to "ein Dollar und null Cent",
    2.01 to "zwei Dollar und ein Cent",
    8.10 to "acht Dollar und zehn Cent",
    12.26 to "zwölf Dollar und sechsundzwanzig Cent",
    21.29 to "einundzwanzig Dollar und neunundzwanzig Cent",
    81.25 to "einundachtzig Dollar und fünfundzwanzig Cent",
    100.00 to "einhundert Dollar und null Cent",
)

private val DE_GBP = listOf(
    1.00 to "ein Pfund und null Pence",
    2.01 to "zwei Pfund und ein Penny",
    8.10 to "acht Pfund und zehn Pence",
    12.26 to "zwölf Pfund und sechsundzwanzig Pence",
    21.29 to "einundzwanzig Pfund und neunundzwanzig Pence",
    81.25 to "einundachtzig Pfund und fünfundzwanzig Pence",
    100.00 to "einhundert Pfund und null Pence",
)

private val DE_DEM = listOf(
    1.00 to "ein Mark und null Pfennig",
    2.01 to "zwei Mark und ein Pfennig",
    8.10 to "acht Mark und zehn Pfennig",
    12.26 to "zwölf Mark und sechsundzwanzig Pfennig",
    21.29 to "einundzwanzig Mark und neunundzwanzig Pfennig",
    81.25 to "einundachtzig Mark und fünfundzwanzig Pfennig",
    100.00 to "einhundert Mark und null Pfennig",
)

/** Golden samples from Python tests/test_de.py. */
class DeTest {
    @Test
    fun ordinalLessThanTwenty() {
        assertEquals("nullte", num2words(0, lang = "de", to = "ordinal"))
        assertEquals("erste", num2words(1, lang = "de", to = "ordinal"))
        assertEquals("siebte", num2words(7, lang = "de", to = "ordinal"))
        assertEquals("achte", num2words(8, lang = "de", to = "ordinal"))
        assertEquals("zwölfte", num2words(12, lang = "de", to = "ordinal"))
        assertEquals("siebzehnte", num2words(17, lang = "de", to = "ordinal"))
    }

    @Test
    fun ordinalMoreThanTwenty() {
        assertEquals("einundachtzigste", num2words(81, lang = "de", to = "ordinal"))
    }

    @Test
    fun ordinalAtCrucialNumber() {
        assertEquals("hundertste", num2words(100, lang = "de", to = "ordinal"))
        assertEquals("tausendste", num2words(1000, lang = "de", to = "ordinal"))
        assertEquals("viertausendste", num2words(4000, lang = "de", to = "ordinal"))
        assertEquals("millionste", num2words(1000000, lang = "de", to = "ordinal"))
        assertEquals("zweimillionste", num2words(2000000, lang = "de", to = "ordinal"))
        assertEquals("milliardste", num2words(1000000000, lang = "de", to = "ordinal"))
        assertEquals("fünfmilliardste", num2words(5000000000, lang = "de", to = "ordinal"))
    }

    @Test
    fun cardinalAtSomeNumbers() {
        assertEquals("einhundert", num2words(100, lang = "de"))
        assertEquals("eintausend", num2words(1000, lang = "de"))
        assertEquals("fünftausend", num2words(5000, lang = "de"))
        assertEquals("zehntausend", num2words(10000, lang = "de"))
        assertEquals("eine Million", num2words(1000000, lang = "de"))
        assertEquals("zwei Millionen", num2words(2000000, lang = "de"))
        assertEquals("vier Milliarden", num2words(4000000000, lang = "de"))
        assertEquals("eine Milliarde", num2words(1000000000, lang = "de"))
    }

    @Test
    fun cardinalForDecimalNumber() {
        assertEquals("drei Komma vier acht sechs", num2words(3.486, lang = "de"))
    }

    @Test
    fun giantCardinalForMerge() {
        assertEquals(
            "vier Billiarden fünfhundert Billionen " +
                "zweiundsiebzig Milliarden neunhundert Millionen einhundertelf",
            num2words(4500072900000111, lang = "de"),
        )
    }

    @Test
    fun ordinalNum() {
        assertEquals("7.", num2words(7, lang = "de", to = "ordinal_num"))
        assertEquals("81.", num2words(81, lang = "de", to = "ordinal_num"))
    }

    @Test
    fun ordinalForNegativeNumbers() {
        assertFailsWith<Num2WordsValueError> { num2words(-12, lang = "de", to = "ordinal") }
    }

    @Test
    fun ordinalForFloatingNumbers() {
        assertFailsWith<Num2WordsValueError> { num2words(2.453, lang = "de", to = "ordinal") }
    }

    @Test
    fun currencyEur() {
        for ((v, e) in DE_EUR) {
            assertEquals(e, num2words(v, lang = "de", to = "currency", options = mapOf("currency" to "EUR")))
        }
    }

    @Test
    fun currencyUsd() {
        for ((v, e) in DE_USD) {
            assertEquals(e, num2words(v, lang = "de", to = "currency", options = mapOf("currency" to "USD")))
        }
    }

    @Test
    fun currencyDem() {
        for ((v, e) in DE_DEM) {
            assertEquals(e, num2words(v, lang = "de", to = "currency", options = mapOf("currency" to "DEM")))
        }
    }

    @Test
    fun currencyGbp() {
        for ((v, e) in DE_GBP) {
            assertEquals(e, num2words(v, lang = "de", to = "currency", options = mapOf("currency" to "GBP")))
        }
    }

    @Test
    fun year() {
        assertEquals("zweitausendzwei", num2words(2002, lang = "de", to = "year"))
    }

    @Test
    fun yearBefore2000() {
        assertEquals("siebzehnhundertachtzig", num2words(1780, lang = "de", to = "year"))
    }
}
