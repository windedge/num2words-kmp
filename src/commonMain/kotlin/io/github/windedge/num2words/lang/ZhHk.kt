package io.github.windedge.num2words.lang

/** Mirrors num2words/lang_ZH_HK.py Num2Word_ZH_HK. */
class Num2WordZhHk : Num2WordZh() {
    // Python mutates instance copies in __init__; Kotlin fixes the values up front.
    override val currencyFloats: List<String> = listOf("毫", "仙")

    private val hkExtraForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "EUR" to (listOf("歐羅") to listOf("歐羅")),
        "JPY" to (listOf("日圓") to listOf("日圓")),
        "CAD" to (listOf("加元") to listOf("加元")),
        "AUD" to (listOf("澳元") to listOf("澳元")),
        "KRW" to (listOf("韓圜") to listOf("韓圜")),
    )

    private val hkExtraCap: List<Pair<String, String>> = listOf(
        "毫" to "角",
        "仙" to "分",
    )

    override val capMap: List<Pair<String, String>>
        get() = super.capMap + hkExtraCap

    override fun zhCurrencyForms(): Map<String, Any> {
        val base = super.zhCurrencyForms().toMutableMap()
        for ((k, v) in hkExtraForms) base[k] = v.first.first()
        return base
    }
}
