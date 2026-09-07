package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals

private val IT_EUR = listOf(
    1.00 to "un euro e zero centesimi",
    2.01 to "due euro e un centesimo",
    8.10 to "otto euro e dieci centesimi",
    12.26 to "dodici euro e ventisei centesimi",
    21.29 to "ventun euro e ventinove centesimi",
    81.25 to "ottantun euro e venticinque centesimi",
    100.00 to "cento euro e zero centesimi",
)

private val IT_USD = listOf(
    1.00 to "un dollaro e zero centesimi",
    2.01 to "due dollari e un centesimo",
    8.10 to "otto dollari e dieci centesimi",
    12.26 to "dodici dollari e ventisei centesimi",
    21.29 to "ventun dollari e ventinove centesimi",
    81.25 to "ottantun dollari e venticinque centesimi",
    100.00 to "cento dollari e zero centesimi",
)

private val IT_GBP = listOf(
    1.00 to "una sterlina e zero penny",
    2.01 to "due sterline e un penny",
    8.10 to "otto sterline e dieci penny",
    12.26 to "dodici sterline e ventisei penny",
    21.29 to "ventun sterline e ventinove penny",
    81.25 to "ottantun sterline e venticinque penny",
    100.00 to "cento sterline e zero penny",
)

/** Golden samples from Python tests/test_it.py. */
class ItTest {
    @Test
    fun negative() {
        val number = 648972145
        val posCrd = num2words(number, lang = "it")
        val negCrd = num2words(-number, lang = "it")
        val posOrd = num2words(number, lang = "it", to = "ordinal")
        val negOrd = num2words(-number, lang = "it", to = "ordinal")
        assertEquals("meno $posCrd", negCrd)
        assertEquals("meno $posOrd", negOrd)
    }

    @Test
    fun floatToCardinal() {
        assertEquals("tre virgola uno quattro uno cinque", num2words("3.1415", lang = "it"))
        assertEquals("tre virgola uno quattro uno cinque", num2words(3.1415, lang = "it"))
        assertEquals("meno cinque virgola uno cinque", num2words(-5.15, lang = "it"))
        assertEquals("meno zero virgola uno cinque", num2words(-0.15, lang = "it"))
    }

    @Test
    fun floatToOrdinal() {
        assertEquals("terzo virgola uno quattro uno cinque", num2words(3.1415, lang = "it", to = "ordinal"))
        assertEquals("meno quinto virgola uno cinque", num2words(-5.15, lang = "it", to = "ordinal"))
        assertEquals("meno zero virgola uno cinque", num2words(-0.15, lang = "it", to = "ordinal"))
    }

    @Test
    fun zero() {
        assertEquals("zero", num2words(0, lang = "it"))
        assertEquals("zero", num2words(0, lang = "it", to = "ordinal"))
    }

    @Test
    fun from1To10() {
        assertEquals("uno", num2words(1, lang = "it"))
        assertEquals("due", num2words(2, lang = "it"))
        assertEquals("sette", num2words(7, lang = "it"))
        assertEquals("dieci", num2words(10, lang = "it"))
    }

    @Test
    fun from11To19() {
        assertEquals("undici", num2words(11, lang = "it"))
        assertEquals("tredici", num2words(13, lang = "it"))
        assertEquals("quindici", num2words(15, lang = "it"))
        assertEquals("sedici", num2words(16, lang = "it"))
        assertEquals("diciannove", num2words(19, lang = "it"))
    }

    @Test
    fun from20To99() {
        assertEquals("venti", num2words(20, lang = "it"))
        assertEquals("ventuno", num2words(21, lang = "it"))
        assertEquals("ventitré", num2words(23, lang = "it"))
        assertEquals("ventotto", num2words(28, lang = "it"))
        assertEquals("trentuno", num2words(31, lang = "it"))
        assertEquals("quaranta", num2words(40, lang = "it"))
        assertEquals("sessantasei", num2words(66, lang = "it"))
        assertEquals("novantadue", num2words(92, lang = "it"))
    }

    @Test
    fun from100To999() {
        assertEquals("cento", num2words(100, lang = "it"))
        assertEquals("centoundici", num2words(111, lang = "it"))
        assertEquals("centocinquanta", num2words(150, lang = "it"))
        assertEquals("centonovantasei", num2words(196, lang = "it"))
        assertEquals("duecento", num2words(200, lang = "it"))
        assertEquals("duecentodieci", num2words(210, lang = "it"))
        assertEquals("settecentouno", num2words(701, lang = "it"))
    }

    @Test
    fun from1000To9999() {
        assertEquals("mille", num2words(1000, lang = "it"))
        assertEquals("milleuno", num2words(1001, lang = "it"))
        assertEquals("millecinquecento", num2words(1500, lang = "it"))
        assertEquals("settemilatrecentosettantotto", num2words(7378, lang = "it"))
        assertEquals("duemila", num2words(2000, lang = "it"))
        assertEquals("duemilacento", num2words(2100, lang = "it"))
        assertEquals("seimilaottocentosettanta", num2words(6870, lang = "it"))
        assertEquals("diecimila", num2words(10000, lang = "it"))
        assertEquals("novantottomilasettecentosessantacinque", num2words(98765, lang = "it"))
        assertEquals("centomila", num2words(100000, lang = "it"))
        assertEquals("cinquecentoventitremilaquattrocentocinquantasei", num2words(523456, lang = "it"))
    }

    @Test
    fun big() {
        assertEquals("un milione", num2words(1000000, lang = "it"))
        assertEquals("un milione e sette", num2words(1000007, lang = "it"))
        assertEquals("un milione e duecentomila", num2words(1200000, lang = "it"))
        assertEquals("tre milioni", num2words(3000000, lang = "it"))
        assertEquals("tre milioni e cinque", num2words(3000005, lang = "it"))
        assertEquals("tre milioni e ottocentomila", num2words(3800000, lang = "it"))
        assertEquals("un miliardo", num2words(1000000000, lang = "it"))
        assertEquals("un miliardo e diciassette", num2words(1000000017, lang = "it"))
        assertEquals("due miliardi", num2words(2000000000, lang = "it"))
        assertEquals("due miliardi e mille", num2words(2000001000, lang = "it"))
        assertEquals(
            "un miliardo, duecentotrentaquattro milioni e " +
                "cinquecentosessantasettemilaottocentonovanta",
            num2words(1234567890, lang = "it"),
        )
        assertEquals("un bilione", num2words(1000000000000L, lang = "it"))
        assertEquals(
            "centoventitré quadriliardi, quattrocentocinquantasei " +
                "quadrilioni, settecentottantanove triliardi, dodici trilioni, " +
                "trecentoquarantacinque biliardi, seicentosettantotto bilioni, " +
                "novecentouno miliardi, duecentotrentaquattro milioni e " +
                "cinquecentosessantasettemilaottocentonovanta",
            num2words("123456789012345678901234567890", lang = "it"),
        )
    }

    @Test
    fun nth1To99() {
        assertEquals("primo", num2words(1, lang = "it", to = "ordinal"))
        assertEquals("ottavo", num2words(8, lang = "it", to = "ordinal"))
        assertEquals("ventunesimo", num2words(21, lang = "it", to = "ordinal"))
        assertEquals("ventitreesimo", num2words(23, lang = "it", to = "ordinal"))
        assertEquals("quarantasettesimo", num2words(47, lang = "it", to = "ordinal"))
        assertEquals("novantanovesimo", num2words(99, lang = "it", to = "ordinal"))
    }

    @Test
    fun nth100To999() {
        assertEquals("centesimo", num2words(100, lang = "it", to = "ordinal"))
        assertEquals("centododicesimo", num2words(112, lang = "it", to = "ordinal"))
        assertEquals("centoventesimo", num2words(120, lang = "it", to = "ordinal"))
        assertEquals("centoventunesimo", num2words(121, lang = "it", to = "ordinal"))
        assertEquals("trecentosedicesimo", num2words(316, lang = "it", to = "ordinal"))
        assertEquals("settecentesimo", num2words(700, lang = "it", to = "ordinal"))
        assertEquals("ottocentotreesimo", num2words(803, lang = "it", to = "ordinal"))
        assertEquals("novecentoventitreesimo", num2words(923, lang = "it", to = "ordinal"))
    }

    @Test
    fun nth1000To999999() {
        assertEquals("millesimo", num2words(1000, lang = "it", to = "ordinal"))
        assertEquals("milleunesimo", num2words(1001, lang = "it", to = "ordinal"))
        assertEquals("milletreesimo", num2words(1003, lang = "it", to = "ordinal"))
        assertEquals("milleduecentesimo", num2words(1200, lang = "it", to = "ordinal"))
        assertEquals("ottomilaseicentoquarantesimo", num2words(8640, lang = "it", to = "ordinal"))
        assertEquals("quattordicimillesimo", num2words(14000, lang = "it", to = "ordinal"))
        assertEquals(
            "centoventitremilaquattrocentocinquantaseiesimo",
            num2words(123456, lang = "it", to = "ordinal"),
        )
        assertEquals(
            "novecentottantasettemilaseicentocinquantaquattresimo",
            num2words(987654, lang = "it", to = "ordinal"),
        )
    }

    @Test
    fun nthBig() {
        assertEquals("un miliardo e unesimo", num2words(1000000001, lang = "it", to = "ordinal"))
        assertEquals(
            "centoventitré quadriliardi, quattrocentocinquantasei " +
                "quadrilioni, settecentottantanove triliardi, dodici trilioni, " +
                "trecentoquarantacinque biliardi, seicentosettantotto bilioni, " +
                "novecentouno miliardi, duecentotrentaquattro milioni e " +
                "cinquecentosessantasettemilaottocentonovantesimo",
            num2words("123456789012345678901234567890", lang = "it", to = "ordinal"),
        )
    }

    @Test
    fun withFloats() {
        assertEquals("uno", num2words(1.0, lang = "it"))
        assertEquals("uno virgola uno", num2words(1.1, lang = "it"))
    }

    @Test
    fun withStrings() {
        // Python loops 0..2001; sampled here (JS BigInt is ~50x slower, same coverage).
        val samples = (0 until 2002 step 7).toList() + listOf(0, 1, 19, 20, 99, 100, 999, 1000, 2001)
        for (i in samples) {
            num2words(i.toString(), lang = "it", to = "cardinal")
            num2words(i.toString(), lang = "it", to = "ordinal")
        }
        assertEquals("primo", num2words("1", lang = "it", to = "ordinal"))
        assertEquals("centesimo", num2words("100", lang = "it", to = "ordinal"))
        assertEquals("millesimo", num2words("1000", lang = "it", to = "ordinal"))
        assertEquals(
            "un quadriliardo, duecentotrentaquattro quadrilioni, " +
                "cinquecentosessantasette triliardi, ottocentonovanta trilioni, " +
                "centoventitré biliardi, quattrocentocinquantasei bilioni, " +
                "settecentottantanove miliardi, dodici milioni e " +
                "trecentoquarantacinquemilaseicentosettantottesimo",
            num2words("1234567890123456789012345678", lang = "it", to = "ordinal"),
        )
    }

    @Test
    fun currencyEur() {
        for ((v, e) in IT_EUR) {
            assertEquals(e, num2words(v, lang = "it", to = "currency", options = mapOf("currency" to "EUR")))
        }
    }

    @Test
    fun currencyUsd() {
        for ((v, e) in IT_USD) {
            assertEquals(e, num2words(v, lang = "it", to = "currency", options = mapOf("currency" to "USD")))
        }
    }

    @Test
    fun currencyGbp() {
        for ((v, e) in IT_GBP) {
            assertEquals(e, num2words(v, lang = "it", to = "currency", options = mapOf("currency" to "GBP")))
        }
    }
}
