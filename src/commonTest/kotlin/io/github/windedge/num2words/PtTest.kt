package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Golden samples from Python tests/test_pt.py (generated). */
class PtTest {
    @Test
    fun cardinalInteger() {
        assertEquals("um",
            num2words(1, lang = "pt"),
        )
        assertEquals("dois",
            num2words(2, lang = "pt"),
        )
        assertEquals("três",
            num2words(3, lang = "pt"),
        )
        assertEquals("quatro",
            num2words(4, lang = "pt"),
        )
        assertEquals("cinco",
            num2words(5, lang = "pt"),
        )
        assertEquals("seis",
            num2words(6, lang = "pt"),
        )
        assertEquals("sete",
            num2words(7, lang = "pt"),
        )
        assertEquals("oito",
            num2words(8, lang = "pt"),
        )
        assertEquals("nove",
            num2words(9, lang = "pt"),
        )
        assertEquals("dez",
            num2words(10, lang = "pt"),
        )
        assertEquals("onze",
            num2words(11, lang = "pt"),
        )
        assertEquals("doze",
            num2words(12, lang = "pt"),
        )
        assertEquals("treze",
            num2words(13, lang = "pt"),
        )
        assertEquals("catorze",
            num2words(14, lang = "pt"),
        )
        assertEquals("quinze",
            num2words(15, lang = "pt"),
        )
        assertEquals("dezasseis",
            num2words(16, lang = "pt"),
        )
        assertEquals("dezassete",
            num2words(17, lang = "pt"),
        )
        assertEquals("dezoito",
            num2words(18, lang = "pt"),
        )
        assertEquals("dezanove",
            num2words(19, lang = "pt"),
        )
        assertEquals("vinte",
            num2words(20, lang = "pt"),
        )
        assertEquals("vinte e um",
            num2words(21, lang = "pt"),
        )
        assertEquals("vinte e dois",
            num2words(22, lang = "pt"),
        )
        assertEquals("trinta e cinco",
            num2words(35, lang = "pt"),
        )
        assertEquals("noventa e nove",
            num2words(99, lang = "pt"),
        )
        assertEquals("cem",
            num2words(100, lang = "pt"),
        )
        assertEquals("cento e um",
            num2words(101, lang = "pt"),
        )
        assertEquals("cento e vinte e oito",
            num2words(128, lang = "pt"),
        )
        assertEquals("setecentos e treze",
            num2words(713, lang = "pt"),
        )
        assertEquals("mil",
            num2words(1000, lang = "pt"),
        )
        assertEquals("mil e um",
            num2words(1001, lang = "pt"),
        )
        assertEquals("mil cento e onze",
            num2words(1111, lang = "pt"),
        )
        assertEquals("dois mil cento e catorze",
            num2words(2114, lang = "pt"),
        )
        assertEquals("dois mil e duzentos",
            num2words(2200, lang = "pt"),
        )
        assertEquals("dois mil duzentos e trinta",
            num2words(2230, lang = "pt"),
        )
        assertEquals("setenta e três mil e quatrocentos",
            num2words(73400, lang = "pt"),
        )
        assertEquals("setenta e três mil quatrocentos e vinte e um",
            num2words(73421, lang = "pt"),
        )
        assertEquals("cem mil",
            num2words(100000, lang = "pt"),
        )
        assertEquals("duzentos e cinquenta mil e cinquenta",
            num2words(250050, lang = "pt"),
        )
        assertEquals("seis milhões",
            num2words(6000000, lang = "pt"),
        )
        assertEquals("cem milhões",
            num2words(100000000, lang = "pt"),
        )
        assertEquals("dezanove mil milhões",
            num2words(19000000000, lang = "pt"),
        )
        assertEquals("cento e quarenta e cinco mil milhões e dois",
            num2words(145000000002, lang = "pt"),
        )
        assertEquals("quatro milhões seiscentos e trinta e cinco mil cento e dois",
            num2words(4635102, lang = "pt"),
        )
        assertEquals("cento e quarenta e cinco mil duzentos e cinquenta e quatro milhões seiscentos e trinta e cinco mil cento e dois",
            num2words(145254635102, lang = "pt"),
        )
        assertEquals("um bilião",
            num2words(1000000000000, lang = "pt"),
        )
        assertEquals("dois biliões",
            num2words(2000000000000, lang = "pt"),
        )
        assertEquals("mil biliões",
            num2words(1000000000000000, lang = "pt"),
        )
        assertEquals("dois mil biliões",
            num2words(2000000000000000, lang = "pt"),
        )
        assertEquals("um trilião",
            num2words(1000000000000000000, lang = "pt"),
        )
        assertEquals("dois triliões",
            num2words(2000000000000000000, lang = "pt"),
        )
    }

    @Test
    fun cardinalIntegerNegative() {
        assertEquals("menos um",
            num2words(-1, lang = "pt"),
        )
        assertEquals("menos duzentos e cinquenta e seis",
            num2words(-256, lang = "pt"),
        )
        assertEquals("menos mil",
            num2words(-1000, lang = "pt"),
        )
        assertEquals("menos um milhão",
            num2words(-1000000, lang = "pt"),
        )
        assertEquals("menos um milhão duzentos e trinta e quatro mil quinhentos e sessenta e sete",
            num2words(-1234567, lang = "pt"),
        )
    }

    @Test
    fun cardinalFloat() {
        assertEquals("um",
            num2words("1.00", lang = "pt"),
        )
        assertEquals("um vírgula zero um",
            num2words("1.01", lang = "pt"),
        )
        assertEquals("um vírgula zero três cinco",
            num2words("1.035", lang = "pt"),
        )
        assertEquals("um vírgula três cinco",
            num2words("1.35", lang = "pt"),
        )
        assertEquals("três vírgula um quatro um cinco nove",
            num2words("3.14159", lang = "pt"),
        )
        assertEquals("cento e um vírgula dois dois",
            num2words("101.22", lang = "pt"),
        )
        assertEquals("dois mil trezentos e quarenta e cinco vírgula sete cinco",
            num2words("2345.75", lang = "pt"),
        )
    }

    @Test
    fun cardinalFloatNegative() {
        assertEquals("menos dois vírgula três quatro",
            num2words("-2.34", lang = "pt"),
        )
        assertEquals("menos nove vírgula nove nove",
            num2words("-9.99", lang = "pt"),
        )
        assertEquals("menos sete vírgula zero um",
            num2words("-7.01", lang = "pt"),
        )
        assertEquals("menos duzentos e vinte e dois vírgula dois dois",
            num2words("-222.22", lang = "pt"),
        )
    }

    @Test
    fun ordinal() {
        assertEquals("primeiro",
            num2words(1, lang = "pt", to = "ordinal"),
        )
        assertEquals("segundo",
            num2words(2, lang = "pt", to = "ordinal"),
        )
        assertEquals("terceiro",
            num2words(3, lang = "pt", to = "ordinal"),
        )
        assertEquals("quarto",
            num2words(4, lang = "pt", to = "ordinal"),
        )
        assertEquals("quinto",
            num2words(5, lang = "pt", to = "ordinal"),
        )
        assertEquals("sexto",
            num2words(6, lang = "pt", to = "ordinal"),
        )
        assertEquals("sétimo",
            num2words(7, lang = "pt", to = "ordinal"),
        )
        assertEquals("oitavo",
            num2words(8, lang = "pt", to = "ordinal"),
        )
        assertEquals("nono",
            num2words(9, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo",
            num2words(10, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo primeiro",
            num2words(11, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo segundo",
            num2words(12, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo terceiro",
            num2words(13, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo quarto",
            num2words(14, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo quinto",
            num2words(15, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo sexto",
            num2words(16, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo sétimo",
            num2words(17, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo oitavo",
            num2words(18, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo nono",
            num2words(19, lang = "pt", to = "ordinal"),
        )
        assertEquals("vigésimo",
            num2words(20, lang = "pt", to = "ordinal"),
        )
        assertEquals("vigésimo primeiro",
            num2words(21, lang = "pt", to = "ordinal"),
        )
        assertEquals("vigésimo segundo",
            num2words(22, lang = "pt", to = "ordinal"),
        )
        assertEquals("trigésimo quinto",
            num2words(35, lang = "pt", to = "ordinal"),
        )
        assertEquals("nonagésimo nono",
            num2words(99, lang = "pt", to = "ordinal"),
        )
        assertEquals("centésimo",
            num2words(100, lang = "pt", to = "ordinal"),
        )
        assertEquals("centésimo primeiro",
            num2words(101, lang = "pt", to = "ordinal"),
        )
        assertEquals("centésimo vigésimo oitavo",
            num2words(128, lang = "pt", to = "ordinal"),
        )
        assertEquals("septigentésimo décimo terceiro",
            num2words(713, lang = "pt", to = "ordinal"),
        )
        assertEquals("milésimo",
            num2words(1000, lang = "pt", to = "ordinal"),
        )
        assertEquals("milésimo primeiro",
            num2words(1001, lang = "pt", to = "ordinal"),
        )
        assertEquals("milésimo centésimo décimo primeiro",
            num2words(1111, lang = "pt", to = "ordinal"),
        )
        assertEquals("segundo milésimo centésimo décimo quarto",
            num2words(2114, lang = "pt", to = "ordinal"),
        )
        assertEquals("septuagésimo terceiro milésimo quadrigentésimo vigésimo primeiro",
            num2words(73421, lang = "pt", to = "ordinal"),
        )
        assertEquals("centésimo milésimo",
            num2words(100000, lang = "pt", to = "ordinal"),
        )
        assertEquals("ducentésimo quinquagésimo milésimo quinquagésimo",
            num2words(250050, lang = "pt", to = "ordinal"),
        )
        assertEquals("sexto milionésimo",
            num2words(6000000, lang = "pt", to = "ordinal"),
        )
        assertEquals("décimo nono milésimo milionésimo",
            num2words(19000000000, lang = "pt", to = "ordinal"),
        )
        assertEquals("centésimo quadragésimo quinto milésimo milionésimo segundo",
            num2words(145000000002, lang = "pt", to = "ordinal"),
        )
    }

    @Test
    fun currencyInteger() {
        assertEquals("um euro",
            num2words(1.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dois euros",
            num2words(2.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("três euros",
            num2words(3.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("quatro euros",
            num2words(4.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cinco euros",
            num2words(5.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("seis euros",
            num2words(6.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("sete euros",
            num2words(7.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("oito euros",
            num2words(8.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("nove euros",
            num2words(9.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dez euros",
            num2words(10.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("onze euros",
            num2words(11.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("doze euros",
            num2words(12.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("treze euros",
            num2words(13.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("catorze euros",
            num2words(14.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("quinze euros",
            num2words(15.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dezasseis euros",
            num2words(16.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dezassete euros",
            num2words(17.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dezoito euros",
            num2words(18.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dezanove euros",
            num2words(19.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("vinte euros",
            num2words(20.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("vinte e um euros",
            num2words(21.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("vinte e dois euros",
            num2words(22.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("trinta e cinco euros",
            num2words(35.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("noventa e nove euros",
            num2words(99.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cem euros",
            num2words(100.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cento e um euros",
            num2words(101.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cento e vinte e oito euros",
            num2words(128.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("setecentos e treze euros",
            num2words(713.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("mil euros",
            num2words(1000.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("mil e um euros",
            num2words(1001.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("mil cento e onze euros",
            num2words(1111.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dois mil cento e catorze euros",
            num2words(2114.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("setenta e três mil quatrocentos e vinte e um euros",
            num2words(73421.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cem mil euros",
            num2words(100000.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("duzentos e cinquenta mil e cinquenta euros",
            num2words(250050.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("seis milhões de euros",
            num2words(6000000.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dezanove mil milhões de euros",
            num2words(19000000000.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cento e quarenta e cinco mil milhões e dois euros",
            num2words(145000000002.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("um dólar",
            num2words(1.0, lang = "pt", to = "currency", options = mapOf("currency" to "USD")),
        )
        assertEquals("um dólar e cinquenta cêntimos",
            num2words(1.5, lang = "pt", to = "currency", options = mapOf("currency" to "USD")),
        )
        assertFailsWith<Num2WordsNotImplemented> { num2words(1.0, lang = "pt", to = "currency", options = mapOf("currency" to "CHF")) }
    }

    @Test
    fun currencyIntegerNegative() {
        assertEquals("menos um euro",
            num2words(-1.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos duzentos e cinquenta e seis euros",
            num2words(-256.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos mil euros",
            num2words(-1000.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos um milhão de euros",
            num2words(-1000000.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos um milhão duzentos e trinta e quatro mil quinhentos e sessenta e sete euros",
            num2words(-1234567.0, lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
    }

    @Test
    fun currencyFloat() {
        assertEquals("um euro",
            num2words("1.00", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("um euro e um cêntimo",
            num2words("1.01", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("um euro e três cêntimos",
            num2words("1.03", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("um euro e trinta e cinco cêntimos",
            num2words("1.35", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("três euros e catorze cêntimos",
            num2words("3.14", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("cento e um euros e vinte e dois cêntimos",
            num2words("101.22", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("dois mil trezentos e quarenta e cinco euros e setenta e cinco cêntimos",
            num2words("2345.75", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
    }

    @Test
    fun currencyFloatNegative() {
        assertEquals("menos dois euros e trinta e quatro cêntimos",
            num2words("-2.34", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos nove euros e noventa e nove cêntimos",
            num2words("-9.99", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos sete euros e um cêntimo",
            num2words("-7.01", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
        assertEquals("menos duzentos e vinte e dois euros e vinte e dois cêntimos",
            num2words("-222.22", lang = "pt", to = "currency", options = mapOf("currency" to "EUR")),
        )
    }

    @Test
    fun year() {
        assertEquals("mil e um",
            num2words(1001, lang = "pt", to = "year"),
        )
        assertEquals("mil setecentos e oitenta e nove",
            num2words(1789, lang = "pt", to = "year"),
        )
        assertEquals("mil novecentos e quarenta e dois",
            num2words(1942, lang = "pt", to = "year"),
        )
        assertEquals("mil novecentos e oitenta e quatro",
            num2words(1984, lang = "pt", to = "year"),
        )
        assertEquals("dois mil",
            num2words(2000, lang = "pt", to = "year"),
        )
        assertEquals("dois mil e um",
            num2words(2001, lang = "pt", to = "year"),
        )
        assertEquals("dois mil e dezasseis",
            num2words(2016, lang = "pt", to = "year"),
        )
    }

    @Test
    fun yearNegative() {
        assertEquals("trinta antes de Cristo",
            num2words(-30, lang = "pt", to = "year"),
        )
        assertEquals("setecentos e quarenta e quatro antes de Cristo",
            num2words(-744, lang = "pt", to = "year"),
        )
        assertEquals("dez mil antes de Cristo",
            num2words(-10000, lang = "pt", to = "year"),
        )
    }

    @Test
    fun ordinalNum() {
        assertEquals("1º",
            num2words(1, lang = "pt", to = "ordinal_num"),
        )
        assertEquals("100º",
            num2words(100, lang = "pt", to = "ordinal_num"),
        )
    }

}
