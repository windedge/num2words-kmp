package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.Num2WordBase
import io.github.windedge.num2words.Num2WordsOverflowError
import io.github.windedge.num2words.Num2WordsValueError
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.SimpleDecimal
import io.github.windedge.num2words.checkValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.fmt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

private val AR_CURRENCY_SR = listOf(
    listOf("ريال", "ريالان", "ريالات", "ريالاً"),
    listOf("هللة", "هللتان", "هللات", "هللة"),
)
private val AR_CURRENCY_EGP = listOf(
    listOf("جنيه", "جنيهان", "جنيهات", "جنيهاً"),
    listOf("قرش", "قرشان", "قروش", "قرش"),
)
private val AR_CURRENCY_KWD = listOf(
    listOf("دينار", "ديناران", "دينارات", "ديناراً"),
    listOf("فلس", "فلسان", "فلس", "فلس"),
)
private val AR_CURRENCY_TND = listOf(
    listOf("دينار", "ديناران", "دينارات", "ديناراً"),
    listOf("مليماً", "ميلمان", "مليمات", "مليم"),
)

private val ARABIC_ONES = listOf(
    "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية",
    "تسعة", "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر",
    "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر",
)

/** Mirrors num2words/lang_AR.py Num2Word_AR (self-contained group engine). */
class Num2WordAr : Num2WordBase() {
    private val arMaxVal: BigInt = BigInt.pow10(51)

    var number: String = ""
    var arabicPrefixText: String = ""
    var arabicSuffixText: String = ""
    var integerValue: BigInt = BigInt.ZERO
    var decimalValue: BigInt = BigInt.ZERO
    var partPrecision: Int = 2
    var currencyUnit: List<String> = AR_CURRENCY_SR[0]
    var currencySubunit: List<String> = AR_CURRENCY_SR[1]
    var isCurrencyPartNameFeminine: Boolean = true
    var isCurrencyNameFeminine: Boolean = false
    var separator: String = "و"

    val arabicOnes: List<String> = ARABIC_ONES
    val arabicFeminineOnes: List<String> = listOf(
        "", "إحدى", "اثنتان", "ثلاث", "أربع", "خمس", "ست", "سبع", "ثمان",
        "تسع", "عشر", "إحدى عشرة", "اثنتا عشرة", "ثلاث عشرة", "أربع عشرة",
        "خمس عشرة", "ست عشرة", "سبع عشرة", "ثماني عشرة", "تسع عشرة",
    )
    val arabicOrdinal: List<String> = listOf(
        "", "اول", "ثاني", "ثالث", "رابع", "خامس", "سادس", "سابع", "ثامن",
        "تاسع", "عاشر", "حادي عشر", "ثاني عشر", "ثالث عشر", "رابع عشر",
        "خامس عشر", "سادس عشر", "سابع عشر", "ثامن عشر", "تاسع عشر",
    )
    val arabicTens: List<String> = listOf(
        "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون",
    )
    val arabicHundreds: List<String> = listOf(
        "", "مائة", "مئتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة",
        "سبعمائة", "ثمانمائة", "تسعمائة",
    )
    val arabicAppendedTwos: List<String> = listOf(
        "مئتا", "ألفا", "مليونا", "مليارا", "تريليونا", "كوادريليونا",
        "كوينتليونا", "سكستيليونا", "سبتيليونا", "أوكتيليونا ",
        "نونيليونا", "ديسيليونا", "أندسيليونا", "دوديسيليونا",
        "تريديسيليونا", "كوادريسيليونا", "كوينتينيليونا",
    )
    val arabicTwos: List<String> = listOf(
        "مئتان", "ألفان", "مليونان", "ملياران", "تريليونان",
        "كوادريليونان", "كوينتليونان", "سكستيليونان", "سبتيليونان",
        "أوكتيليونان ", "نونيليونان ", "ديسيليونان", "أندسيليونان",
        "دوديسيليونان", "تريديسيليونان", "كوادريسيليونان", "كوينتينيليونان",
    )
    val arabicGroup: List<String> = listOf(
        "مائة", "ألف", "مليون", "مليار", "تريليون", "كوادريليون",
        "كوينتليون", "سكستيليون", "سبتيليون", "أوكتيليون", "نونيليون",
        "ديسيليون", "أندسيليون", "دوديسيليون", "تريديسيليون",
        "كوادريسيليون", "كوينتينيليون",
    )
    val arabicAppendedGroup: List<String> = listOf(
        "", "ألفاً", "مليوناً", "ملياراً", "تريليوناً", "كوادريليوناً",
        "كوينتليوناً", "سكستيليوناً", "سبتيليوناً", "أوكتيليوناً",
        "نونيليوناً", "ديسيليوناً", "أندسيليوناً", "دوديسيليوناً",
        "تريديسيليوناً", "كوادريسيليوناً", "كوينتينيليوناً",
    )
    val arabicPluralGroups: List<String> = listOf(
        "", "آلاف", "ملايين", "مليارات", "تريليونات", "كوادريليونات",
        "كوينتليونات", "سكستيليونات", "سبتيليونات", "أوكتيليونات",
        "نونيليونات", "ديسيليونات", "أندسيليونات", "دوديسيليونات",
        "تريديسيليونات", "كوادريسيليونات", "كوينتينيليونات",
    )

    override fun setup() {
        // Tables are fixed literals; sizes are compile-time constants.
    }

    // -- engine ------------------------------------------------------------------

    fun numberToArabic(prefix: String, suffix: String) {
        arabicPrefixText = prefix
        arabicSuffixText = suffix
        extractIntegerAndDecimalParts()
    }

    fun extractIntegerAndDecimalParts() {
        val splits = number.split(".")
        integerValue = BigInt.parse(splits[0])
        decimalValue = if (splits.size > 1) BigInt.parse(decimalValueText(splits[1])) else BigInt.ZERO
    }

    fun decimalValueText(decimalPart: String): String {
        if (partPrecision != decimalPart.length) {
            var builder = decimalPart
            repeat(maxOf(0, partPrecision - decimalPart.length)) { builder += "0" }
            val dec = if (builder.length <= partPrecision) builder.length else partPrecision
            return builder.substring(0, dec)
        }
        return decimalPart
    }

    fun digitFeminineStatus(digit: Int, groupLevel: Int): String {
        if (groupLevel == -1) {
            return if (isCurrencyPartNameFeminine) arabicFeminineOnes[digit] else arabicOnes[digit]
        }
        if (groupLevel == 0) {
            return if (isCurrencyNameFeminine) arabicFeminineOnes[digit] else arabicOnes[digit]
        }
        return arabicOnes[digit]
    }

    fun processArabicGroup(groupNumber: BigInt, groupLevel: Int, remainingNumber: BigInt): String {
        val tens = (groupNumber % BigInt.fromLong(100)).toLong()
        val hundredsInt = (groupNumber / BigInt.fromLong(100)).toLong()
        // Python compares Decimal(group/100) == 0, which is true only for group == 0.
        val hundredsIsZero = groupNumber.isZero()
        var retVal = ""
        if (hundredsInt > 0) {
            retVal = if (tens == 0L && hundredsInt == 2L) {
                arabicAppendedTwos[0]
            } else {
                arabicHundreds[hundredsInt.toInt()]
            }
            if (retVal.isNotEmpty() && tens != 0L) retVal += " و "
        }
        if (tens > 0) {
            if (tens < 20) {
                checkValue(groupLevel < arabicTwos.size) { "AR group level" }
                if (tens == 2L && hundredsInt == 0L && groupLevel > 0) {
                    val pow = integerValue.toString().length - 1
                    if (integerValue > BigInt.fromLong(10) && pow % 3 == 0 &&
                        integerValue == BigInt.fromLong(2) * BigInt.pow10(pow)
                    ) {
                        retVal = arabicAppendedTwos[groupLevel]
                    } else {
                        retVal = arabicTwos[groupLevel]
                    }
                } else {
                    if (tens == 1L && groupLevel > 0 && hundredsIsZero) {
                        retVal += ""
                    } else if ((tens == 1L || tens == 2L) &&
                        (groupLevel == 0 || groupLevel == -1) &&
                        hundredsIsZero && remainingNumber.isZero()
                    ) {
                        retVal += ""
                    } else if (tens == 1L && groupLevel > 0) {
                        retVal += arabicGroup[groupLevel]
                    } else {
                        retVal += digitFeminineStatus(tens.toInt(), groupLevel)
                    }
                }
            } else {
                val ones = (tens % 10).toInt()
                val tensIdx = (tens / 10 - 2).toInt()
                if (ones > 0) retVal += digitFeminineStatus(ones, groupLevel)
                if (retVal.isNotEmpty() && ones != 0) retVal += " و "
                retVal += arabicTens[tensIdx]
            }
        }
        return retVal
    }

    /** Mirrors to_str: exact decimal text, 9 fractional digits max. */
    fun toStrAr(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> {
            val plain = value.v.toPlainString()
            val dot = plain.indexOf('.')
            if (dot < 0) plain else {
                val frac = plain.substring(dot + 1).take(9).trimEnd('0')
                if (frac.isEmpty()) plain.substring(0, dot) else plain.substring(0, dot) + "." + frac
            }
        }

        is NumValue.FloatVal -> {
            val d = value.v
            if (d % 1.0 == 0.0) doubleToBigInt(d).toString()
            else {
                val dec = SimpleDecimal.fromDouble(d)
                toStrAr(NumValue.Decimal(dec))
            }
        }
    }

    fun convertAr(value: NumValue): String {
        number = toStrAr(value)
        numberToArabic(arabicPrefixText, arabicSuffixText)
        return convertToArabic()
    }

    fun convertToArabic(): String {
        val tempZero = integerValue.isZero() && decimalValue.isZero()
        if (tempZero) return "صفر"
        val decimalString = processArabicGroup(decimalValue, -1, BigInt.ZERO)
        var retVal = ""
        var group = 0
        var tempNumber = integerValue
        while (tempNumber > BigInt.ZERO) {
            val numberToProcess = (tempNumber % BigInt.fromLong(1000)).toLong()
            tempNumber /= BigInt.fromLong(1000)
            val groupDescription = processArabicGroup(
                BigInt.fromLong(numberToProcess), group, tempNumber,
            )
            if (groupDescription.isNotEmpty()) {
                if (group > 0) {
                    if (retVal.isNotEmpty()) retVal = "و $retVal"
                    if (numberToProcess != 2L && numberToProcess != 1L) {
                        checkValue(group < arabicGroup.size) { "AR group" }
                        if (numberToProcess % 100 != 1L) {
                            if (numberToProcess in 3..10) {
                                retVal = "${arabicPluralGroups[group]} $retVal"
                            } else {
                                retVal = if (retVal.isNotEmpty()) {
                                    "${arabicAppendedGroup[group]} $retVal"
                                } else {
                                    "${arabicGroup[group]} $retVal"
                                }
                            }
                        } else {
                            retVal = "${arabicGroup[group]} $retVal"
                        }
                    }
                }
                retVal = "$groupDescription $retVal"
            }
            group++
        }
        var formatted = ""
        if (arabicPrefixText.isNotEmpty()) formatted += "$arabicPrefixText "
        formatted += retVal
        if (!integerValue.isZero()) {
            val remaining100 = (integerValue % BigInt.fromLong(100)).toLong()
            formatted += when {
                remaining100 == 0L -> currencyUnit[0]
                remaining100 == 1L -> currencyUnit[0]
                remaining100 == 2L -> if (integerValue == BigInt.fromLong(2)) currencyUnit[1] else currencyUnit[0]
                remaining100 in 3..10 -> currencyUnit[2]
                else -> currencyUnit[3]
            }
        }
        if (!decimalValue.isZero()) {
            formatted += " $separator "
            formatted += decimalString
        }
        if (!decimalValue.isZero()) {
            formatted += " "
            val remaining100 = (decimalValue % BigInt.fromLong(100)).toLong()
            formatted += when {
                remaining100 == 0L -> currencySubunit[0]
                remaining100 == 1L -> currencySubunit[0]
                remaining100 == 2L -> currencySubunit[1]
                remaining100 in 3..10 -> currencySubunit[2]
                else -> currencySubunit[3]
            }
        }
        if (arabicSuffixText.isNotEmpty()) formatted += " $arabicSuffixText"
        return formatted
    }

    fun validateNumber(value: NumValue): NumValue {
        val over = when (value) {
            is NumValue.Whole -> value.v >= arMaxVal
            is NumValue.Decimal -> value.v.compareTo(SimpleDecimal.fromBigInt(arMaxVal)) >= 0
            is NumValue.FloatVal -> value.v >= 1e51
        }
        if (over) {
            val text = when (value) {
                is NumValue.Whole -> value.v.toString()
                is NumValue.Decimal -> value.v.toPlainString()
                is NumValue.FloatVal -> trimDouble(value.v)
            }
            throw Num2WordsOverflowError(fmt(errmsgToobig, text, arMaxVal.toString()))
        }
        return value
    }

    fun setCurrencyPrefer(currency: String) {
        if (currency == "TND") {
            currencyUnit = AR_CURRENCY_TND[0]
            currencySubunit = AR_CURRENCY_TND[1]
            partPrecision = 3
        } else if (currency == "EGP") {
            currencyUnit = AR_CURRENCY_EGP[0]
            currencySubunit = AR_CURRENCY_EGP[1]
            partPrecision = 2
        } else if (currency == "KWD") {
            currencyUnit = AR_CURRENCY_KWD[0]
            currencySubunit = AR_CURRENCY_KWD[1]
            partPrecision = 2
        } else {
            currencyUnit = AR_CURRENCY_SR[0]
            currencySubunit = AR_CURRENCY_SR[1]
            partPrecision = 2
        }
    }

    fun toCurrencyAr(value: NumValue, currency: String = "SR", prefix: String = "", suffix: String = ""): String {
        setCurrencyPrefer(currency)
        isCurrencyNameFeminine = false
        separator = "و"
        arabicPrefixText = prefix
        arabicSuffixText = suffix
        if (currency != "SR" && currency != "TND" && currency != "EGP" && currency != "KWD") {
            // Python raises KeyError only for unknown codes via dict lookup;
            // here every non-listed code falls back to SR per set_currency_prefer.
        }
        return convertAr(validateNumber(value))
    }

    fun toOrdinalAr(value: NumValue, prefix: String = ""): String {
        val whole = when (value) {
            is NumValue.Whole -> value.v
            is NumValue.Decimal -> integerIfWhole(value.v) ?: BigInt.ZERO
            is NumValue.FloatVal -> doubleToBigInt(value.v)
        }
        if (whole <= BigInt.fromLong(19)) return arabicOrdinal[whole.toLong().toInt()]
        if (whole < BigInt.fromLong(100)) isCurrencyNameFeminine = true
        else isCurrencyNameFeminine = false
        currencySubunit = listOf("", "", "", "")
        currencyUnit = listOf("", "", "", "")
        arabicPrefixText = prefix
        arabicSuffixText = ""
        return convertAr(validateNumber(NumValue.Whole(whole.abs()))).trim()
    }

    override fun toOrdinal(value: NumValue): String = toOrdinalAr(value)

    override fun toOrdinalNum(value: NumValue): Any = toOrdinalAr(value).trim()

    fun toYearAr(value: NumValue): String {
        val v = validateNumber(value)
        return toCardinalAr(v)
    }

    override fun toYear(value: NumValue, suffix: String?, longval: Boolean): String =
        toYearAr(value)

    fun toCardinalAr(value: NumValue): String {
        isCurrencyNameFeminine = false
        val v = validateNumber(value)
        var minus = ""
        var absV = v
        val sign = when (v) {
            is NumValue.Whole -> v.v.sign()
            is NumValue.Decimal -> v.v.unscaled.sign()
            is NumValue.FloatVal -> if (v.v < 0) -1 else 0
        }
        if (sign < 0) {
            minus = "سالب "
            absV = when (v) {
                is NumValue.Whole -> NumValue.Whole(-v.v)
                is NumValue.Decimal -> NumValue.Decimal(v.v.negate())
                is NumValue.FloatVal -> NumValue.FloatVal(-v.v)
            }
        }
        separator = ","
        currencySubunit = listOf("", "", "", "")
        currencyUnit = listOf("", "", "", "")
        arabicPrefixText = ""
        arabicSuffixText = ""
        return minus + convertAr(absV).trim()
    }

    override fun toCardinal(value: NumValue): String = toCardinalAr(value)
}
