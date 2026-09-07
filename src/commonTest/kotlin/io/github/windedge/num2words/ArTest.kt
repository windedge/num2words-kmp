package io.github.windedge.num2words

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Golden samples from Python tests/test_ar.py (generated). */
class ArTest {
    @Test
    fun defaultCurrency() {
        assertEquals("واحد ريال",
            num2words(1, lang = "ar", to = "currency"),
        )
        assertEquals("اثنان ريالان",
            num2words(2, lang = "ar", to = "currency"),
        )
        assertEquals("عشرة ريالات",
            num2words(10, lang = "ar", to = "currency"),
        )
        assertEquals("مائة ريال",
            num2words(100, lang = "ar", to = "currency"),
        )
        assertEquals("ستمائة و اثنان و خمسون ريالاً و اثنتا عشرة هللة",
            num2words(652.12, lang = "ar", to = "currency"),
        )
        assertEquals("ثلاثمائة و أربعة و عشرون ريالاً",
            num2words(324, lang = "ar", to = "currency"),
        )
        assertEquals("ألفا ريال",
            num2words(2000, lang = "ar", to = "currency"),
        )
        assertEquals("خمسمائة و واحد و أربعون ريالاً",
            num2words(541, lang = "ar", to = "currency"),
        )
        assertEquals("عشرة آلاف ريال",
            num2words(10000, lang = "ar", to = "currency"),
        )
        assertEquals("عشرون ألف ريال و اثنتا عشرة هللة",
            num2words(20000.12, lang = "ar", to = "currency"),
        )
        assertEquals("مليون ريال",
            num2words(1000000, lang = "ar", to = "currency"),
        )
        assertEquals("تسعمائة و ثلاثة و عشرون ألفاً و أربعمائة و أحد عشر ريالاً",
            num2words(923411, lang = "ar", to = "currency"),
        )
        assertEquals("ثلاثة و ستون ألفاً و أربعمائة و أحد عشر ريالاً",
            num2words(63411, lang = "ar", to = "currency"),
        )
        assertEquals("مليون ريال و تسع و تسعون هللة",
            num2words(1000000.99, lang = "ar", to = "currency"),
        )
    }

    @Test
    fun currencyParam() {
        assertEquals("واحد دينار",
            num2words(1, lang = "ar", to = "currency", options = mapOf("currency" to "KWD")),
        )
        assertEquals("عشرة جنيهات",
            num2words(10, lang = "ar", to = "currency", options = mapOf("currency" to "EGP")),
        )
        assertEquals("عشرون ألف جنيه و اثنتا عشرة قرش",
            num2words(20000.12, lang = "ar", to = "currency", options = mapOf("currency" to "EGP")),
        )
        assertEquals("تسعمائة و ثلاثة و عشرون ألفاً و أربعمائة و أحد عشر ريالاً",
            num2words(923411, lang = "ar", to = "currency", options = mapOf("currency" to "SR")),
        )
        assertEquals("مليون دينار و تسع و تسعون فلس",
            num2words(1000000.99, lang = "ar", to = "currency", options = mapOf("currency" to "KWD")),
        )
        assertEquals("ألف دينار و أربعمائة و عشرون مليم",
            num2words(1000.42, lang = "ar", to = "currency", options = mapOf("currency" to "TND")),
        )
        assertEquals("مائة و ثلاثة و عشرون ديناراً و مئتان و عشر مليمات",
            num2words(123.21, lang = "ar", to = "currency", options = mapOf("currency" to "TND")),
        )
    }

    @Test
    fun ordinal() {
        assertEquals("اول",
            num2words(1, lang = "ar", to = "ordinal"),
        )
        assertEquals("ثاني",
            num2words(2, lang = "ar", to = "ordinal"),
        )
        assertEquals("ثالث",
            num2words(3, lang = "ar", to = "ordinal"),
        )
        assertEquals("رابع",
            num2words(4, lang = "ar", to = "ordinal"),
        )
        assertEquals("خامس",
            num2words(5, lang = "ar", to = "ordinal"),
        )
        assertEquals("سادس",
            num2words(6, lang = "ar", to = "ordinal"),
        )
        assertEquals("تاسع",
            num2words(9, lang = "ar", to = "ordinal"),
        )
        assertEquals("عشرون",
            num2words(20, lang = "ar", to = "ordinal"),
        )
        assertEquals("أربع و تسعون",
            num2words(94, lang = "ar", to = "ordinal"),
        )
        assertEquals("مائة و اثنان",
            num2words(102, lang = "ar", to = "ordinal"),
        )
        assertEquals("تسعمائة و ثلاثة و عشرون ألفاً و أربعمائة و أحد عشر",
            num2words(923411, lang = "ar", to = "ordinal_num"),
        )
        assertEquals("ثلاثة و عشرون",
            num2words(23, lang = "ar", to = "cardinal"),
        )
        assertEquals("ثلاث و عشرون",
            num2words(23, lang = "ar", to = "ordinal"),
        )
        assertEquals("ثلاثة و عشرون",
            num2words(23, lang = "ar", to = "cardinal"),
        )
    }

    @Test
    fun cardinal() {
        assertEquals("صفر",
            num2words(0, lang = "ar", to = "cardinal"),
        )
        assertEquals("اثنا عشر",
            num2words(12, lang = "ar", to = "cardinal"),
        )
        assertEquals("اثنا عشر  , ثلاثون",
            num2words(12.3, lang = "ar", to = "cardinal"),
        )
        assertEquals("اثنا عشر  , إحدى",
            num2words(12.01, lang = "ar", to = "cardinal"),
        )
        assertEquals("اثنا عشر  , اثنتان",
            num2words(12.02, lang = "ar", to = "cardinal"),
        )
        assertEquals("اثنا عشر  , ثلاث",
            num2words(12.03, lang = "ar", to = "cardinal"),
        )
        assertEquals("اثنا عشر  , أربع و ثلاثون",
            num2words(12.34, lang = "ar", to = "cardinal"),
        )
        assertEquals(num2words(12.34, lang = "ar", to = "cardinal"), num2words(12.345, lang = "ar", to = "cardinal"))
        assertEquals("سالب ثمانية آلاف و ثلاثمائة و أربعة و عشرون",
            num2words(-8324, lang = "ar", to = "cardinal"),
        )
        assertEquals("مئتا",
            num2words(200, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة",
            num2words(700, lang = "ar", to = "cardinal"),
        )
        assertEquals("مائة و ألف ألف و عشرة",
            num2words(101010, lang = "ar", to = "cardinal"),
        )
        assertEquals("ثلاثة آلاف و أربعمائة و واحد و ثلاثون  , اثنتا عشرة",
            num2words(3431.12, lang = "ar", to = "cardinal"),
        )
        assertEquals("أربعمائة و واحد و ثلاثون",
            num2words(431, lang = "ar", to = "cardinal"),
        )
        assertEquals("أربعة و تسعون ألفاً و مئتان و واحد و ثلاثون",
            num2words(94231, lang = "ar", to = "cardinal"),
        )
        assertEquals("ألف و أربعمائة و واحد و ثلاثون",
            num2words(1431, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة و أربعون",
            num2words(740, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة و واحد و أربعون",
            num2words(741, lang = "ar", to = "cardinal"),
        )
        assertEquals("مئتان و اثنان و ستون",
            num2words(262, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة و ثمانية و تسعون",
            num2words(798, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة و عشرة",
            num2words(710, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة و أحد عشر",
            num2words(711, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة",
            num2words(700, lang = "ar", to = "cardinal"),
        )
        assertEquals("سبعمائة و واحد",
            num2words(701, lang = "ar", to = "cardinal"),
        )
        assertEquals("مليون و مئتان و ثمانية و خمسون ألفاً و ثمانمائة و ثمانية و ثمانون",
            num2words(1258888, lang = "ar", to = "cardinal"),
        )
        assertEquals("ألف و مائة",
            num2words(1100, lang = "ar", to = "cardinal"),
        )
        assertEquals("مليار و خمسمائة و واحد و عشرون",
            num2words(1000000521, lang = "ar", to = "cardinal"),
        )
    }

    @Test
    fun prefixAndSuffix() {
        assertEquals("فقط ستمائة و خمسة و أربعون ريالاً لاغير",
            num2words(645, lang = "ar", to = "currency", options = mapOf("prefix" to "فقط", "suffix" to "لاغير")),
        )
    }

    @Test
    fun year() {
        assertEquals("ألفا",
            num2words(2000, lang = "ar", to = "year"),
        )
    }

    @Test
    fun maxNumbers() {
        for (number in listOf("1" + "0".repeat(51), "1" + "0".repeat(50) + "2")) {
            try {
                num2words(number, lang = "ar")
                throw AssertionError("expected overflow")
            } catch (e: Num2WordsOverflowError) {
                assertTrue(e.message!!.contains("must be less"))
            }
        }
    }

    @Test
    fun bigNumbers() {
        assertEquals("تريديسيليون و خمسة و أربعون ديسيليوناً و ثلاثة كوينتليونات و ملياران و ثلاثمائة",
            num2words("1000000045000000000000003000000002000000300", lang = "ar", to = "cardinal"),
        )
        assertEquals("سالب تريديسيليون و ثلاثة كوينتليونات و ملياران و ثلاثمائة و اثنان",
            num2words("-1000000000000000000000003000000002000000302", lang = "ar", to = "cardinal"),
        )
        assertEquals("تسعة كوينتينيليونات و تسعمائة و تسعة و تسعون كوادريسيليوناً و تسعمائة و تسعة و تسعون تريديسيليوناً و تسعمائة و تسعة و تسعون دوديسيليوناً و تسعمائة و تسعة و تسعون أندسيليوناً و تسعمائة و تسعة و تسعون ديسيليوناً و تسعمائة و تسعة و تسعون نونيليوناً و تسعمائة و تسعة و تسعون أوكتيليوناً و تسعمائة و تسعة و تسعون سبتيليوناً و تسعمائة و تسعة و تسعون سكستيليوناً و تسعمائة و تسعة و تسعون كوينتليوناً و تسعمائة و تسعة و تسعون كوادريليوناً و تسعمائة و تسعة و تسعون تريليوناً و تسعمائة و تسعة و تسعون ملياراً و تسعمائة و تسعة و تسعون مليوناً و تسعمائة و تسعة و تسعون ألفاً و تسعمائة و اثنان و تسعون",
            num2words("9999999999999999999999999999999999999999999999992", lang = "ar", to = "cardinal"),
        )
    }

}
