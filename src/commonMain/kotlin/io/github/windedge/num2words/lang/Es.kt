package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt
import io.github.windedge.num2words.NumValue
import io.github.windedge.num2words.doubleToBigInt
import io.github.windedge.num2words.integerIfWhole
import io.github.windedge.num2words.trimDouble

private val ES_GENERIC_DOLLARS = listOf("dólar", "dólares")
private val ES_GENERIC_CENTS = listOf("centavo", "centavos")

private val ES_CURRENCIES_UNA = setOf(
    "SLL", "SEK", "NOK", "CZK", "DKK", "ISK", "SKK", "GBP", "CYP", "EGP",
    "FKP", "GIP", "LBP", "SDG", "SHP", "SSP", "SYP", "INR", "IDR", "LKR",
    "MUR", "NPR", "PKR", "SCR", "ESP", "TRY", "ITL",
)
private val ES_CENTS_UNA = setOf("EGP", "JOD", "LBP", "SDG", "SSP", "SYP")

/** Mirrors num2words/lang_ES.py Num2Word_ES. */
class Num2WordEs : Num2WordEU() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = esCurrencyForms()

    var genderStem: String = "o"

    private val ords: Map<BigInt, String> = mapOf(
        BigInt.fromLong(1) to "primer",
        BigInt.fromLong(2) to "segund",
        BigInt.fromLong(3) to "tercer",
        BigInt.fromLong(4) to "cuart",
        BigInt.fromLong(5) to "quint",
        BigInt.fromLong(6) to "sext",
        BigInt.fromLong(7) to "séptim",
        BigInt.fromLong(8) to "octav",
        BigInt.fromLong(9) to "noven",
        BigInt.fromLong(10) to "décim",
        BigInt.fromLong(20) to "vigésim",
        BigInt.fromLong(30) to "trigésim",
        BigInt.fromLong(40) to "cuadragésim",
        BigInt.fromLong(50) to "quincuagésim",
        BigInt.fromLong(60) to "sexagésim",
        BigInt.fromLong(70) to "septuagésim",
        BigInt.fromLong(80) to "octogésim",
        BigInt.fromLong(90) to "nonagésim",
        BigInt.fromLong(100) to "centésim",
        BigInt.fromLong(200) to "ducentésim",
        BigInt.fromLong(300) to "tricentésim",
        BigInt.fromLong(400) to "cuadrigentésim",
        BigInt.fromLong(500) to "quingentésim",
        BigInt.fromLong(600) to "sexcentésim",
        BigInt.fromLong(700) to "septigentésim",
        BigInt.fromLong(800) to "octigentésim",
        BigInt.fromLong(900) to "noningentésim",
        BigInt.fromLong(1000) to "milésim",
        BigInt.fromLong(1000000) to "millonésim",
        BigInt.fromLong(1000000000) to "billonésim",
        BigInt.pow10(12) to "trillonésim",
        BigInt.pow10(15) to "cuadrillonésim",
    )

    override fun setup() {
        gigaSuffix = ""
        megaSuffix = "illón"
        negword = "menos "
        pointword = "punto"
        errmsgNonnum = "type(%s) no es [long, int, float]"
        errmsgFloatord = "El float %s no puede ser tratado como un ordinal."
        errmsgNegord = "El número negativo %s no puede ser tratado como un ordinal."
        errmsgToobig = "abs(%s) deber ser inferior a %s."
        genderStem = "o"
        excludeTitle.clear()
        excludeTitle.addAll(listOf("y", "menos", "punto"))
        useNumwords(
            high = genHighNumwords(emptyList(), emptyList(), listOf("cuatr", "tr", "b", "m")),
            mid = listOf(
                BigInt.fromLong(1000) to "mil",
                BigInt.fromLong(100) to "cien",
                BigInt.fromLong(90) to "noventa",
                BigInt.fromLong(80) to "ochenta",
                BigInt.fromLong(70) to "setenta",
                BigInt.fromLong(60) to "sesenta",
                BigInt.fromLong(50) to "cincuenta",
                BigInt.fromLong(40) to "cuarenta",
                BigInt.fromLong(30) to "treinta",
            ),
            low = listOf(
                "veintinueve", "veintiocho", "veintisiete",
                "veintiséis", "veinticinco", "veinticuatro",
                "veintitrés", "veintidós", "veintiuno",
                "veinte", "diecinueve", "dieciocho", "diecisiete",
                "dieciséis", "quince", "catorce", "trece", "doce",
                "once", "diez", "nueve", "ocho", "siete", "seis",
                "cinco", "cuatro", "tres", "dos", "uno", "cero",
            ),
        )
    }

    override fun merge(left: Pair<String, BigInt>, right: Pair<String, BigInt>): Pair<String, BigInt> {
        var (ctext, cnum) = left
        var (ntext, nnum) = right
        val one = BigInt.ONE
        val hundred = BigInt.fromLong(100)
        val thousand = BigInt.fromLong(1000)
        val million = BigInt.fromLong(1000000)
        if (cnum == one) {
            if (nnum < million) return right
            ctext = "un"
        } else if (cnum == hundred && nnum % thousand != BigInt.ZERO) {
            ctext += "t$genderStem"
        }
        if (nnum < cnum) {
            if (cnum < hundred) return "$ctext y $ntext" to cnum + nnum
            return "$ctext $ntext" to cnum + nnum
        } else if (nnum % million == BigInt.ZERO && cnum > one) {
            ntext = ntext.dropLast(3) + "lones"
        }
        if (nnum == hundred) {
            if (cnum == BigInt.fromLong(5)) {
                ctext = "quinien"
                ntext = ""
            } else if (cnum == BigInt.fromLong(7)) {
                ctext = "sete"
            } else if (cnum == BigInt.fromLong(9)) {
                ctext = "nove"
            }
            ntext += "t${genderStem}s"
        } else {
            ntext = " $ntext"
        }
        return (ctext + ntext to cnum * nnum)
    }

    fun toOrdinalEs(value: NumValue, gender: String = "m"): String {
        val stem = if (gender == "f") "a" else "o"
        verifyOrdinal(value)
        val v = wholeOf(value)
        val ten = BigInt.fromLong(10)
        val hundred = BigInt.fromLong(100)
        val thousand = BigInt.fromLong(1000)
        val text = when {
            v.isZero() -> ""
            v <= ten -> "${requireNotNull(ords[v])}$stem"
            v <= BigInt.fromLong(29) -> {
                val dec = v / ten * ten
                "${requireNotNull(ords[dec]).replace('é', 'e')}o${toOrdinalEs(NumValue.Whole(v % ten), gender)}"
            }

            v <= hundred -> {
                val dec = v / ten * ten
                "${requireNotNull(ords[dec])}$stem ${toOrdinalEs(NumValue.Whole(v - dec), gender)}"
            }

            v <= thousand -> {
                val cen = v / hundred * hundred
                "${requireNotNull(ords[cen])}$stem ${toOrdinalEs(NumValue.Whole(v - cen), gender)}"
            }

            v < BigInt.pow10(18) -> {
                val dec = pow1000Floor(v)
                val (highPart, lowPart) = v.divmod(dec)
                val cardinal = if (highPart == BigInt.ONE) "" else toCardinal(NumValue.Whole(highPart))
                "$cardinal${requireNotNull(ords[dec])}$stem ${toOrdinalEs(NumValue.Whole(lowPart), gender)}"
            }

            else -> toCardinal(value)
        }
        return text.trim().replace("oo", "o")
    }

    override fun toOrdinal(value: NumValue): String = toOrdinalEs(value)

    fun toOrdinalNumEs(value: NumValue, gender: String = "m"): Any {
        val stem = if (gender == "f") "a" else "o"
        verifyOrdinal(value)
        return "${wholeTextOf(value)}${if (stem == "o") "º" else "ª"}"
    }

    override fun toOrdinalNum(value: NumValue): Any = toOrdinalNumEs(value)

    fun toCurrencyEs(
        value: NumValue,
        currency: String = "EUR",
        cents: Boolean = true,
        separator: String = " con",
        adjective: Boolean = false,
    ): String {
        val result = super.toCurrency(value, currency, cents, separator, adjective)
        val parts = result.split(separator + " ")
        val dollars = parts[0]
            .let { if (currency in ES_CURRENCIES_UNA) it.replace("uno", "una").replace("cientos", "cientas") else it }
            .replace("veintiuno", "veintiún")
            .replace("uno", "un")
        if (parts.size < 2) return dollars
        val centsPart = parts[1]
            .let { if (currency in ES_CENTS_UNA) it.replace("uno", "una") else it }
            .replace("veintiuno", "veintiún")
            .replace("uno", "un")
        return dollars + separator + " " + centsPart
    }

    override fun toCurrency(
        value: NumValue,
        currency: String,
        cents: Boolean,
        separator: String,
        adjective: Boolean,
    ): String = toCurrencyEs(value, currency, cents, separator, adjective)

    private fun wholeOf(value: NumValue): BigInt = when (value) {
        is NumValue.Whole -> value.v
        is NumValue.Decimal -> integerIfWhole(value.v) ?: BigInt.ZERO
        is NumValue.FloatVal -> doubleToBigInt(value.v)
    }

    private fun wholeTextOf(value: NumValue): String = when (value) {
        is NumValue.Whole -> value.v.toString()
        is NumValue.Decimal -> value.v.toPlainString()
        is NumValue.FloatVal -> trimDouble(value.v)
    }

    /** Largest 1000^n <= v, mirroring 1000 ** int(math.log(v, 1000)). */
    private fun pow1000Floor(v: BigInt): BigInt {
        var p = BigInt.fromLong(1000)
        while (p * BigInt.fromLong(1000) <= v) p *= BigInt.fromLong(1000)
        return p
    }
}

private fun esCurrencyForms(): Map<String, Pair<List<String>, List<String>>> = mapOf(
    "EUR" to (listOf("euro", "euros") to listOf("céntimo", "céntimos")),
    "ESP" to (listOf("peseta", "pesetas") to listOf("céntimo", "céntimos")),
    "USD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "PEN" to (listOf("sol", "soles") to listOf("céntimo", "céntimos")),
    "CRC" to (listOf("colón", "colones") to ES_GENERIC_CENTS),
    "AUD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "CAD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "GBP" to (listOf("libra", "libras") to listOf("penique", "peniques")),
    "RUB" to (listOf("rublo", "rublos") to listOf("kopeyka", "kopeykas")),
    "SEK" to (listOf("corona", "coronas") to listOf("öre", "öre")),
    "NOK" to (listOf("corona", "coronas") to listOf("øre", "øre")),
    "PLN" to (listOf("zloty", "zlotys") to listOf("grosz", "groszy")),
    "MXN" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "RON" to (listOf("leu", "leus") to listOf("ban", "bani")),
    "INR" to (listOf("rupia", "rupias") to listOf("paisa", "paisas")),
    "HUF" to (listOf("florín", "florines") to listOf("fillér", "fillér")),
    "FRF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "CNY" to (listOf("yuan", "yuanes") to listOf("fen", "jiaos")),
    "CZK" to (listOf("corona", "coronas") to listOf("haléř", "haléř")),
    "NIO" to (listOf("córdoba", "córdobas") to ES_GENERIC_CENTS),
    "VES" to (listOf("bolívar", "bolívares") to listOf("céntimo", "céntimos")),
    "BRL" to (listOf("real", "reales") to ES_GENERIC_CENTS),
    "CHF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "JPY" to (listOf("yen", "yenes") to listOf("sen", "sen")),
    "KRW" to (listOf("won", "wones") to listOf("jeon", "jeon")),
    "KPW" to (listOf("won", "wones") to listOf("chon", "chon")),
    "TRY" to (listOf("lira", "liras") to listOf("kuruş", "kuruş")),
    "ZAR" to (listOf("rand", "rands") to listOf("céntimo", "céntimos")),
    "KZT" to (listOf("tenge", "tenges") to listOf("tïın", "tïın")),
    "UAH" to (listOf("hryvnia", "hryvnias") to listOf("kopiyka", "kopiykas")),
    "THB" to (listOf("baht", "bahts") to listOf("satang", "satang")),
    "AED" to (listOf("dirham", "dirhams") to listOf("fils", "fils")),
    "AFN" to (listOf("afghani", "afghanis") to listOf("pul", "puls")),
    "ALL" to (listOf("lek ", "leke") to listOf("qindarkë", "qindarka")),
    "AMD" to (listOf("dram", "drams") to listOf("luma", "lumas")),
    "ANG" to (listOf("florín", "florines") to ES_GENERIC_CENTS),
    "AOA" to (listOf("kwanza", "kwanzas") to listOf("céntimo", "céntimos")),
    "ARS" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "AWG" to (listOf("florín", "florines") to ES_GENERIC_CENTS),
    "AZN" to (listOf("manat", "manat") to listOf("qəpik", "qəpik")),
    "BBD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "BDT" to (listOf("taka", "takas") to listOf("paisa", "paisas")),
    "BGN" to (listOf("lev", "leva") to listOf("stotinka", "stotinki")),
    "BHD" to (listOf("dinar", "dinares") to listOf("fils", "fils")),
    "BIF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "BMD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "BND" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "BOB" to (listOf("boliviano", "bolivianos") to ES_GENERIC_CENTS),
    "BSD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "BTN" to (listOf("ngultrum", "ngultrum") to listOf("chetrum", "chetrum")),
    "BWP" to (listOf("pula", "pulas") to listOf("thebe", "thebes")),
    "BYN" to (listOf("rublo", "rublos") to listOf("kópek", "kópeks")),
    "BYR" to (listOf("rublo", "rublos") to listOf("kópek", "kópeks")),
    "BZD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "CDF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "CLP" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "COP" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "CUP" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "CVE" to (listOf("escudo", "escudos") to ES_GENERIC_CENTS),
    "CYP" to (listOf("libra", "libras") to listOf("céntimo", "céntimos")),
    "DJF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "DKK" to (listOf("corona", "coronas") to listOf("øre", "øre")),
    "DOP" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "DZD" to (listOf("dinar", "dinares") to listOf("céntimo", "céntimos")),
    "ECS" to (listOf("sucre", "sucres") to ES_GENERIC_CENTS),
    "EGP" to (listOf("libra", "libras") to listOf("piastra", "piastras")),
    "ERN" to (listOf("nakfa", "nakfas") to listOf("céntimo", "céntimos")),
    "ETB" to (listOf("birr", "birrs") to listOf("céntimo", "céntimos")),
    "FJD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "FKP" to (listOf("libra", "libras") to listOf("penique", "peniques")),
    "GEL" to (listOf("lari", "laris") to listOf("tetri", "tetris")),
    "GHS" to (listOf("cedi", "cedis") to listOf("pesewa", "pesewas")),
    "GIP" to (listOf("libra", "libras") to listOf("penique", "peniques")),
    "GMD" to (listOf("dalasi", "dalasis") to listOf("butut", "bututs")),
    "GNF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "GTQ" to (listOf("quetzal", "quetzales") to ES_GENERIC_CENTS),
    "GYD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "HKD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "HNL" to (listOf("lempira", "lempiras") to ES_GENERIC_CENTS),
    "HRK" to (listOf("kuna", "kunas") to listOf("lipa", "lipas")),
    "HTG" to (listOf("gourde", "gourdes") to listOf("céntimo", "céntimos")),
    "IDR" to (listOf("rupia", "rupias") to listOf("céntimo", "céntimos")),
    "ILS" to (listOf("séquel", "séqueles") to listOf("agora", "agoras")),
    "IQD" to (listOf("dinar", "dinares") to listOf("fils", "fils")),
    "IRR" to (listOf("rial", "riales") to listOf("dinar", "dinares")),
    "ISK" to (listOf("corona", "coronas") to listOf("eyrir", "aurar")),
    "ITL" to (listOf("lira", "liras") to listOf("céntimo", "céntimos")),
    "JMD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "JOD" to (listOf("dinar", "dinares") to listOf("piastra", "piastras")),
    "KES" to (listOf("chelín", "chelines") to listOf("céntimo", "céntimos")),
    "KGS" to (listOf("som", "som") to listOf("tyiyn", "tyiyn")),
    "KHR" to (listOf("riel", "rieles") to listOf("céntimo", "céntimos")),
    "KMF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "KWD" to (listOf("dinar", "dinares") to listOf("fils", "fils")),
    "KYD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "LAK" to (listOf("kip", "kips") to listOf("att", "att")),
    "LBP" to (listOf("libra", "libras") to listOf("piastra", "piastras")),
    "LKR" to (listOf("rupia", "rupias") to listOf("céntimo", "céntimos")),
    "LRD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "LSL" to (listOf("loti", "lotis") to listOf("céntimo", "céntimos")),
    "LTL" to (listOf("lita", "litas") to listOf("céntimo", "céntimos")),
    "LVL" to (listOf("lat", "lats") to listOf("céntimo", "céntimos")),
    "LYD" to (listOf("dinar", "dinares") to listOf("dírham", "dírhams")),
    "MAD" to (listOf("dírham", "dirhams") to listOf("céntimo", "céntimos")),
    "MDL" to (listOf("leu", "lei") to listOf("ban", "bani")),
    "MGA" to (listOf("ariary", "ariaris") to listOf("iraimbilanja", "iraimbilanja")),
    "MKD" to (listOf("denar", "denares") to listOf("deni", "denis")),
    "MMK" to (listOf("kiat", "kiats") to listOf("pya", "pyas")),
    "MNT" to (listOf("tugrik", "tugriks") to listOf("möngö", "möngö")),
    "MOP" to (listOf("pataca", "patacas") to listOf("avo", "avos")),
    "MRO" to (listOf("ouguiya", "ouguiyas") to listOf("khoums", "khoums")),
    "MRU" to (listOf("ouguiya", "ouguiyas") to listOf("khoums", "khoums")),
    "MUR" to (listOf("rupia", "rupias") to listOf("céntimo", "céntimos")),
    "MVR" to (listOf("rufiyaa", "rufiyaas") to listOf("laari", "laari")),
    "MWK" to (listOf("kuacha", "kuachas") to listOf("tambala", "tambalas")),
    "MYR" to (listOf("ringgit", "ringgit") to listOf("céntimo", "céntimos")),
    "MZN" to (listOf("metical", "metical") to ES_GENERIC_CENTS),
    "NAD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "NGN" to (listOf("naira", "nairas") to listOf("kobo", "kobo")),
    "NPR" to (listOf("rupia", "rupias") to listOf("paisa", "paisas")),
    "NZD" to (ES_GENERIC_DOLLARS to ES_GENERIC_CENTS),
    "OMR" to (listOf("rial", "riales") to listOf("baisa", "baisa")),
    "PAB" to (listOf("balboa", "balboas") to listOf("centésimo", "centésimos")),
    "PGK" to (listOf("kina", "kinas") to listOf("toea", "toea")),
    "PHP" to (listOf("peso", "pesos") to ES_GENERIC_CENTS),
    "PKR" to (listOf("rupia", "rupias") to listOf("paisa", "paisas")),
    "PLZ" to (listOf("zloty", "zlotys") to listOf("grosz", "groszy")),
    "PYG" to (listOf("guaraní", "guaranís") to listOf("céntimo", "céntimos")),
    "QAR" to (listOf("rial", "riales") to listOf("dírham", "dírhams")),
    "QTQ" to (listOf("quetzal", "quetzales") to ES_GENERIC_CENTS),
    "RSD" to (listOf("dinar", "dinares") to listOf("para", "para")),
    "RUR" to (listOf("rublo", "rublos") to listOf("kopek", "kopeks")),
    "RWF" to (listOf("franco", "francos") to listOf("céntimo", "céntimos")),
    "SAR" to (listOf("riyal", "riales") to listOf("halala", "halalas")),
    "SBD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "SCR" to (listOf("rupia", "rupias") to listOf("céntimo", "céntimos")),
    "SDG" to (listOf("libra", "libras") to listOf("piastra", "piastras")),
    "SGD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "SHP" to (listOf("libra", "libras") to listOf("penique", "peniques")),
    "SKK" to (listOf("corona", "coronas") to listOf("halier", "haliers")),
    "SLL" to (listOf("leona", "leonas") to listOf("céntimo", "céntimos")),
    "SRD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "SSP" to (listOf("libra", "libras") to listOf("piastra", "piastras")),
    "STD" to (listOf("dobra", "dobras") to listOf("céntimo", "céntimos")),
    "SVC" to (listOf("colón", "colones") to ES_GENERIC_CENTS),
    "SYP" to (listOf("libra", "libras") to listOf("piastra", "piastras")),
    "SZL" to (listOf("lilangeni", "emalangeni") to listOf("céntimo", "céntimos")),
    "TJS" to (listOf("somoni", "somonis") to listOf("dirame", "dirames")),
    "TMT" to (listOf("manat", "manat") to listOf("tenge", "tenge")),
    "TND" to (listOf("dinar", "dinares") to listOf("milésimo", "milésimos")),
    "TOP" to (listOf("paanga", "paangas") to listOf("céntimo", "céntimos")),
    "TTD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "TWD" to (listOf("nuevo dólar", "nuevos dólares") to listOf("céntimo", "céntimos")),
    "TZS" to (listOf("chelín", "chelines") to listOf("céntimo", "céntimos")),
    "UAG" to (listOf("hryvnia", "hryvnias") to listOf("kopiyka", "kopiykas")),
    "UGX" to (listOf("chelín", "chelines") to listOf("céntimo", "céntimos")),
    "UYU" to (listOf("peso", "pesos") to listOf("centésimo", "centésimos")),
    "UZS" to (listOf("sum", "sum") to listOf("tiyin", "tiyin")),
    "VEF" to (listOf("bolívar fuerte", "bolívares fuertes") to listOf("céntimo", "céntimos")),
    "VND" to (listOf("dong", "dongs") to listOf("xu", "xu")),
    "VUV" to (listOf("vatu", "vatu") to listOf("nenhum", "nenhum")),
    "WST" to (listOf("tala", "tala") to ES_GENERIC_CENTS),
    "XAF" to (listOf("franco CFA", "francos CFA") to listOf("céntimo", "céntimos")),
    "XCD" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
    "XOF" to (listOf("franco CFA", "francos CFA") to listOf("céntimo", "céntimos")),
    "XPF" to (listOf("franco CFP", "francos CFP") to listOf("céntimo", "céntimos")),
    "YER" to (listOf("rial", "riales") to listOf("fils", "fils")),
    "YUM" to (listOf("dinar", "dinares") to listOf("para", "para")),
    "ZMW" to (listOf("kwacha", "kwachas") to listOf("ngwee", "ngwee")),
    "ZRZ" to (listOf("zaire", "zaires") to listOf("likuta", "makuta")),
    "ZWL" to (ES_GENERIC_DOLLARS to listOf("céntimo", "céntimos")),
)
