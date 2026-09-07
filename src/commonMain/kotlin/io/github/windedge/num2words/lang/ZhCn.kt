package io.github.windedge.num2words.lang

import io.github.windedge.num2words.BigInt

/** Mirrors num2words/lang_ZH_CN.py Num2Word_ZH_CN. */
class Num2WordZhCn : Num2WordZh() {
    override val currencyForms: Map<String, Pair<List<String>, List<String>>> = mapOf(
        "XXX" to (listOf("元") to listOf("元")),
        "CNY" to (listOf("人民币") to listOf("人民币")),
        "NTD" to (listOf("新台币") to listOf("新台币")),
        "HKD" to (listOf("港币") to listOf("港币")),
        "MOP" to (listOf("澳门币") to listOf("澳门币")),
        "SGD" to (listOf("新加坡元") to listOf("新加坡元")),
        "MYR" to (listOf("马来西亚令吉") to listOf("马来西亚令吉")),
        "USD" to (listOf("美元") to listOf("美元")),
        "EUR" to (listOf("欧元") to listOf("欧元")),
        "GBP" to (listOf("英镑") to listOf("英镑")),
        "JPY" to (listOf("日元") to listOf("日元")),
        "CHF" to (listOf("瑞士法郎") to listOf("瑞士法郎")),
        "CAD" to (listOf("加元") to listOf("加元")),
        "AUD" to (listOf("澳币") to listOf("澳币")),
        "NZD" to (listOf("纽西兰元") to listOf("纽西兰元")),
        "THB" to (listOf("泰铢") to listOf("泰铢")),
        "KRW" to (listOf("韩元") to listOf("韩元")),
    )

    override val capMap: List<Pair<String, String>> = listOf(
        "千" to "仟", "百" to "佰", "十" to "拾",
        "九" to "玖", "八" to "捌", "七" to "柒",
        "六" to "陆", "五" to "伍", "四" to "肆",
        "三" to "叁", "二" to "贰", "一" to "壹",
        "元" to "圆",
    )

    override fun setup() {
        super.setup()
        negword = "负"
        pointword = "点"
        excludeTitle.clear()
        excludeTitle.addAll(listOf(negword, pointword))
        useNumwords(
            high = listOf(
                "万", "亿", "兆", "京", "垓", "秭", "穣", "沟",
                "涧", "正", "载", "极", "恒河沙", "阿僧祇",
                "那由他", "不可思议", "无量", "不可说",
            ).reversed(),
            mid = listOf(
                BigInt.fromLong(1000) to "千",
                BigInt.fromLong(100) to "百",
                BigInt.fromLong(10) to "十",
            ),
            low = listOf("九", "八", "七", "六", "五", "四", "三", "二", "一", "零"),
        )
    }
}
