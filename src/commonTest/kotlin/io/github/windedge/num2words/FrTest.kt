package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private val FR_CARDINAL: List<Pair<Any, String>> = listOf(
    1 to "un",
    2 to "deux",
    3 to "trois",
    5.5 to "cinq virgule cinq",
    11 to "onze",
    12 to "douze",
    16 to "seize",
    17.42 to "dix-sept virgule quatre deux",
    19 to "dix-neuf",
    20 to "vingt",
    21 to "vingt et un",
    26 to "vingt-six",
    27.312 to "vingt-sept virgule trois un deux",
    28 to "vingt-huit",
    30 to "trente",
    31 to "trente et un",
    40 to "quarante",
    44 to "quarante-quatre",
    50 to "cinquante",
    53.486 to "cinquante-trois virgule quatre huit six",
    55 to "cinquante-cinq",
    60 to "soixante",
    67 to "soixante-sept",
    70 to "soixante-dix",
    79 to "soixante-dix-neuf",
    89 to "quatre-vingt-neuf",
    95 to "quatre-vingt-quinze",
    100 to "cent",
    101 to "cent un",
    199 to "cent quatre-vingt-dix-neuf",
    203 to "deux cent trois",
    287 to "deux cent quatre-vingt-sept",
    300.42 to "trois cents virgule quatre deux",
    356 to "trois cent cinquante-six",
    400 to "quatre cents",
    434 to "quatre cent trente-quatre",
    578 to "cinq cent soixante-dix-huit",
    689 to "six cent quatre-vingt-neuf",
    729 to "sept cent vingt-neuf",
    894 to "huit cent quatre-vingt-quatorze",
    999 to "neuf cent quatre-vingt-dix-neuf",
    1000 to "mille",
    1001 to "mille un",
    1097 to "mille quatre-vingt-dix-sept",
    1104 to "mille cent quatre",
    1243 to "mille deux cent quarante-trois",
    2385 to "deux mille trois cent quatre-vingt-cinq",
    3766 to "trois mille sept cent soixante-six",
    4196 to "quatre mille cent quatre-vingt-seize",
    4196.42 to "quatre mille cent quatre-vingt-seize virgule quatre deux",
    5846 to "cinq mille huit cent quarante-six",
    6459 to "six mille quatre cent cinquante-neuf",
    7232 to "sept mille deux cent trente-deux",
    8569 to "huit mille cinq cent soixante-neuf",
    9539 to "neuf mille cinq cent trente-neuf",
    1000000 to "un million",
    1000001 to "un million un",
    4000000 to "quatre millions",
    4000004 to "quatre millions quatre",
    4300000 to "quatre millions trois cent mille",
    80000000 to "quatre-vingts millions",
    300000000 to "trois cents millions",
    10000000000000L to "dix billions",
    10000000000010L to "dix billions dix",
    100000000000000L to "cent billions",
    "1000000000000000000" to "un trillion",
    "1000000000000000000000" to "un trilliard",
    "10000000000000000000000000" to "dix quadrillions",
)

private val FR_ORDINAL = listOf(
    1 to "premier",
    8 to "huitième",
    12 to "douzième",
    14 to "quatorzième",
    28 to "vingt-huitième",
    100 to "centième",
    1000 to "millième",
    1000000 to "un millionième",
    "1000000000000000" to "un billiardième",
    "1000000000000000000" to "un trillionième",
)

private val FR_ORDINAL_NUM = listOf(
    1 to "1er",
    8 to "8me",
    11 to "11me",
    12 to "12me",
    14 to "14me",
    21 to "21me",
    28 to "28me",
    100 to "100me",
    101 to "101me",
    1000 to "1000me",
    1000000 to "1000000me",
)

private val FR_EUR = listOf(
    1.00 to "un euro et zéro centimes",
    2.01 to "deux euros et un centime",
    8.10 to "huit euros et dix centimes",
    12.26 to "douze euros et vingt-six centimes",
    21.29 to "vingt et un euros et vingt-neuf centimes",
    81.25 to "quatre-vingt-un euros et vingt-cinq centimes",
    100.00 to "cent euros et zéro centimes",
)

private val FR_FRF = listOf(
    1.00 to "un franc et zéro centimes",
    2.01 to "deux francs et un centime",
    8.10 to "huit francs et dix centimes",
    12.27 to "douze francs et vingt-sept centimes",
    21.29 to "vingt et un francs et vingt-neuf centimes",
    81.25 to "quatre-vingt-un francs et vingt-cinq centimes",
    100.00 to "cent francs et zéro centimes",
)

private val FR_USD = listOf(
    1.00 to "un dollar et zéro cents",
    2.01 to "deux dollars et un cent",
    8.10 to "huit dollars et dix cents",
    12.26 to "douze dollars et vingt-six cents",
    21.29 to "vingt et un dollars et vingt-neuf cents",
    81.25 to "quatre-vingt-un dollars et vingt-cinq cents",
    100.00 to "cent dollars et zéro cents",
)

// NOTE: on JS every Number is Double; Double checks come first and integral
// doubles convert to Long (mirrors Python int inputs). Applies to esNum too.
private fun frNum(v: Any): Any = when (v) {
    is Double -> if (v % 1.0 == 0.0 && v >= -9.0e18 && v <= 9.0e18) {
        num2words(v.toLong(), lang = "fr")
    } else {
        num2words(v, lang = "fr")
    }

    is Float -> if (v.toDouble() % 1.0 == 0.0) {
        num2words(v.toLong(), lang = "fr")
    } else {
        num2words(v.toDouble(), lang = "fr")
    }

    is Long -> num2words(v, lang = "fr")
    is Int -> num2words(v, lang = "fr")
    is String -> num2words(v, lang = "fr")
    else -> throw IllegalArgumentException()
}

/** Golden samples from Python tests/test_fr.py. */
class FrTest {
    @Test
    fun ordinalSpecialJoins() {
        assertEquals("cinquième", num2words(5, lang = "fr", to = "ordinal"))
        assertEquals("trente-cinquième", num2words(35, lang = "fr", to = "ordinal"))
        assertEquals("neuvième", num2words(9, lang = "fr", to = "ordinal"))
        assertEquals("quarante-neuvième", num2words(49, lang = "fr", to = "ordinal"))
    }

    @Test
    fun number() {
        for ((v, e) in FR_CARDINAL) assertEquals(e, frNum(v))
    }

    @Test
    fun ordinal() {
        for ((v, e) in FR_ORDINAL) {
            val actual = when (v) {
                is Int -> num2words(v, lang = "fr", to = "ordinal")
                is String -> num2words(v, lang = "fr", to = "ordinal")
                else -> throw IllegalArgumentException()
            }
            assertEquals(e, actual)
        }
    }

    @Test
    fun ordinalNum() {
        for ((v, e) in FR_ORDINAL_NUM) assertEquals(e, num2words(v, lang = "fr", to = "ordinal_num"))
    }

    @Test
    fun currencyEur() {
        for ((v, e) in FR_EUR) {
            assertEquals(e, num2words(v, lang = "fr", to = "currency", options = mapOf("currency" to "EUR")))
        }
    }

    @Test
    fun currencyFrf() {
        for ((v, e) in FR_FRF) {
            assertEquals(e, num2words(v, lang = "fr", to = "currency", options = mapOf("currency" to "FRF")))
        }
    }

    @Test
    fun currencyUsd() {
        for ((v, e) in FR_USD) {
            assertEquals(e, num2words(v, lang = "fr", to = "currency", options = mapOf("currency" to "USD")))
        }
    }

    @Test
    fun maxNumbers() {
        assertFailsWith<Num2WordsOverflowError> { num2words("1" + "0".repeat(700), lang = "fr") }
    }
}
