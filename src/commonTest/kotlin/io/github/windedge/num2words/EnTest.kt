package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Golden samples extracted from Python tests/test_en.py; output must match byte-for-byte. */
class EnTest {
    @Test
    fun andJoin199() {
        // ref https://github.com/savoirfairelinux/num2words/issues/8
        assertEquals("one hundred and ninety-nine", num2words(199))
    }

    @Test
    fun ordinal() {
        assertEquals("zeroth", num2words(0, lang = "en", to = "ordinal"))
        assertEquals("first", num2words(1, lang = "en", to = "ordinal"))
        assertEquals("thirteenth", num2words(13, lang = "en", to = "ordinal"))
        assertEquals("twenty-second", num2words(22, lang = "en", to = "ordinal"))
        assertEquals("twelfth", num2words(12, lang = "en", to = "ordinal"))
        assertEquals("one hundred and thirtieth", num2words(130, lang = "en", to = "ordinal"))
        assertEquals("one thousand and third", num2words(1003, lang = "en", to = "ordinal"))
    }

    @Test
    fun ordinalNum() {
        assertEquals("10th", num2words(10, lang = "en", to = "ordinal_num"))
        assertEquals("21st", num2words(21, lang = "en", to = "ordinal_num"))
        assertEquals("102nd", num2words(102, lang = "en", to = "ordinal_num"))
        assertEquals("73rd", num2words(73, lang = "en", to = "ordinal_num"))
    }

    @Test
    fun cardinalForFloat() {
        assertEquals("zero point one two", num2words(0.12))
        assertEquals("minus zero point one two", num2words(-0.12))
        // issue 24
        assertEquals("twelve point five", num2words(12.5))
        assertEquals("twelve point five one", num2words(12.51))
        assertEquals("twelve point five three", num2words(12.53))
        assertEquals("twelve point five nine", num2words(12.59))
    }

    @Test
    fun overflow() {
        assertFailsWith<Num2WordsOverflowError> {
            num2words(
                "1000000000000000000000000000000000000000000000000000000" +
                    "0000000000000000000000000000000000000000000000000000000" +
                    "0000000000000000000000000000000000000000000000000000000" +
                    "0000000000000000000000000000000000000000000000000000000" +
                    "0000000000000000000000000000000000000000000000000000000" +
                    "00000000000000000000000000000000",
            )
        }
    }

    @Test
    fun toCurrency() {
        assertEquals(
            "thirty-eight dollars and 40 cents",
            num2words(
                "38.4", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to false, "currency" to "USD"),
            ),
        )
        assertEquals(
            "zero dollars",
            num2words(
                "0", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to false, "currency" to "USD"),
            ),
        )
        assertEquals(
            "one dollar and one cent",
            num2words(
                "1.01", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "USD"),
            ),
        )
        assertEquals(
            "four thousand, seven hundred and seventy-eight US dollars and zero cents",
            num2words(
                "4778.00", lang = "en", to = "currency",
                options = mapOf(
                    "separator" to " and",
                    "cents" to true,
                    "currency" to "USD",
                    "adjective" to true,
                ),
            ),
        )
        assertEquals(
            "four thousand, seven hundred and seventy-eight dollars and zero cents",
            num2words(
                "4778.00", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "USD"),
            ),
        )
        assertEquals(
            "one peso and ten cents",
            num2words(
                "1.1", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "MXN"),
            ),
        )
        assertEquals(
            "one hundred and fifty-eight pesos and thirty cents",
            num2words(
                "158.3", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "MXN"),
            ),
        )
        assertEquals(
            "two thousand pesos and zero cents",
            num2words(
                "2000.00", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "MXN"),
            ),
        )
        assertEquals(
            "four pesos and one cent",
            num2words(
                "4.01", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "MXN"),
            ),
        )
        assertEquals(
            "two thousand sums and zero tiyins",
            num2words(
                "2000.00", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "UZS"),
            ),
        )
        assertEquals(
            "two thousand yen and zero sen",
            num2words(
                "2000.00", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "JPY"),
            ),
        )
        assertEquals(
            "two thousand won and zero jeon",
            num2words(
                "2000.00", lang = "en", to = "currency",
                options = mapOf("separator" to " and", "cents" to true, "currency" to "KRW"),
            ),
        )
    }

    @Test
    fun toYear() {
        // issue 141
        // "e2 e2"
        assertEquals("nineteen ninety", num2words(1990, lang = "en", to = "year"))
        assertEquals("fifty-five fifty-five", num2words(5555, lang = "en", to = "year"))
        assertEquals("twenty seventeen", num2words(2017, lang = "en", to = "year"))
        assertEquals("ten sixty-six", num2words(1066, lang = "en", to = "year"))
        assertEquals("eighteen sixty-five", num2words(1865, lang = "en", to = "year"))
        // "e3 and e1"; "e2 oh-e1"; "e3"
        assertEquals("three thousand", num2words(3000, lang = "en", to = "year"))
        assertEquals("two thousand and one", num2words(2001, lang = "en", to = "year"))
        assertEquals("nineteen oh-one", num2words(1901, lang = "en", to = "year"))
        assertEquals("two thousand", num2words(2000, lang = "en", to = "year"))
        assertEquals("nine oh-five", num2words(905, lang = "en", to = "year"))
        // "e2 hundred"; "e3"
        assertEquals("sixty-six hundred", num2words(6600, lang = "en", to = "year"))
        assertEquals("nineteen hundred", num2words(1900, lang = "en", to = "year"))
        assertEquals("six hundred", num2words(600, lang = "en", to = "year"))
        assertEquals("fifty", num2words(50, lang = "en", to = "year"))
        assertEquals("zero", num2words(0, lang = "en", to = "year"))
        // suffixes
        assertEquals("forty-four BC", num2words(-44, lang = "en", to = "year"))
        assertEquals(
            "forty-four BCE",
            num2words(-44, lang = "en", to = "year", options = mapOf("suffix" to "BCE")),
        )
        assertEquals(
            "one AD",
            num2words(1, lang = "en", to = "year", options = mapOf("suffix" to "AD")),
        )
        assertEquals(
            "sixty-six m.y.a.",
            num2words(66, lang = "en", to = "year", options = mapOf("suffix" to "m.y.a.")),
        )
        assertEquals("sixty-six million BC", num2words(-66000000, lang = "en", to = "year"))
    }
}
