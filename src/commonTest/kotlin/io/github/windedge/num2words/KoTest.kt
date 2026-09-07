package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Golden samples from Python tests/test_ko.py. */
class KoTest {
    @Test
    fun low() {
        val cases = listOf(
            0 to "영", 1 to "일", 2 to "이", 3 to "삼", 4 to "사",
            5 to "오", 6 to "육", 7 to "칠", 8 to "팔", 9 to "구",
            10 to "십", 11 to "십일", 12 to "십이", 13 to "십삼",
            14 to "십사", 15 to "십오", 16 to "십육", 17 to "십칠",
            18 to "십팔", 19 to "십구", 20 to "이십", 25 to "이십오",
            31 to "삼십일", 42 to "사십이", 54 to "오십사",
            63 to "육십삼", 76 to "칠십육", 89 to "팔십구", 98 to "구십팔",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko"))
    }

    @Test
    fun mid() {
        val cases = listOf(
            100 to "백", 121 to "백이십일", 160 to "백육십",
            256 to "이백오십육", 285 to "이백팔십오", 486 to "사백팔십육",
            627 to "육백이십칠", 808 to "팔백팔", 999 to "구백구십구",
            1004 to "천사", 2018 to "이천십팔", 7063 to "칠천육십삼",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko"))
    }

    @Test
    fun high() {
        val cases = listOf(
            10000 to "만",
            11020 to "만 천이십",
            25891 to "이만 오천팔백구십일",
            64237 to "육만 사천이백삼십칠",
            241572 to "이십사만 천오백칠십이",
            100000000 to "일억",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko"))
        assertEquals("오조 오억", num2words(5000500000000L, lang = "ko"))
    }

    @Test
    fun negative() {
        val cases = listOf(
            -11 to "마이너스 십일",
            -15 to "마이너스 십오",
            -18 to "마이너스 십팔",
            -241572 to "마이너스 이십사만 천오백칠십이",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko"))
    }

    @Test
    fun year() {
        val cases = listOf(
            2000 to "이천년",
            2002 to "이천이년",
            2018 to "이천십팔년",
            1954 to "천구백오십사년",
            1910 to "천구백십년",
            -1000 to "기원전 천년",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko", to = "year"))
    }

    @Test
    fun currency() {
        assertEquals("팔천삼백오십원", num2words(8350, lang = "ko", to = "currency"))
        assertEquals("만사천구백팔십원", num2words(14980, lang = "ko", to = "currency"))
        assertEquals("이억오천만사천원", num2words(250004000, lang = "ko", to = "currency"))
        assertEquals(
            "사달러 영센트",
            num2words(4, lang = "ko", to = "currency", options = mapOf("currency" to "USD")),
        )
        assertEquals(
            "십구달러 오십오센트",
            num2words(19.55, lang = "ko", to = "currency", options = mapOf("currency" to "USD")),
        )
        assertEquals(
            "십오엔",
            num2words(15, lang = "ko", to = "currency", options = mapOf("currency" to "JPY")),
        )
        assertEquals(
            "오십엔",
            num2words(50, lang = "ko", to = "currency", options = mapOf("currency" to "JPY")),
        )
        assertFailsWith<Num2WordsValueError> { num2words(190.55, lang = "ko", to = "currency") }
        assertFailsWith<Num2WordsNotImplemented> {
            num2words(4, lang = "ko", to = "currency", options = mapOf("currency" to "EUR"))
        }
    }

    @Test
    fun ordinal() {
        val cases = listOf(
            1 to "첫 번째",
            101 to "백 한 번째",
            2 to "두 번째",
            5 to "다섯 번째",
            10 to "열 번째",
            25 to "스물다섯 번째",
            137 to "백 서른일곱 번째",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko", to = "ordinal"))
    }

    @Test
    fun ordinalNum() {
        val cases = listOf(
            1 to "1 번째",
            101 to "101 번째",
            25 to "25 번째",
        )
        for ((n, e) in cases) assertEquals(e, num2words(n, lang = "ko", to = "ordinal_num"))
    }
}
