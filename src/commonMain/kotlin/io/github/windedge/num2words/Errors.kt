package io.github.windedge.num2words

/**
 * Portable error types.
 *
 * java.lang exceptions (NumberFormatException, ArithmeticException, ...) are
 * unavailable or ambiguous on JS/Native, so the library uses its own hierarchy
 * mirroring the Python exceptions it replaces:
 * OverflowError -> [Num2WordsOverflowError],
 * TypeError -> [Num2WordsTypeError],
 * NotImplementedError -> [Num2WordsNotImplemented],
 * ValueError -> [Num2WordsValueError].
 */
open class Num2WordsException(message: String) : Exception(message)

class Num2WordsOverflowError(message: String) : Num2WordsException(message)

class Num2WordsTypeError(message: String) : Num2WordsException(message)

class Num2WordsNotImplemented(message: String = "Not implemented") : Num2WordsException(message)

class Num2WordsValueError(message: String) : Num2WordsException(message)

/** Portable argument check replacing kotlin.require (which throws JDK exceptions on JVM). */
fun checkValue(condition: Boolean, lazyMessage: () -> String = { "Check failed" }) {
    if (!condition) throw Num2WordsValueError(lazyMessage())
}

/** Minimal "%s"-only formatter replacing String.format (unavailable in common code). */
fun fmt(template: String, vararg args: Any?): String {
    val sb = StringBuilder()
    var i = 0
    var k = 0
    while (k < template.length) {
        if (k + 1 < template.length && template[k] == '%' && template[k + 1] == 's' && i < args.size) {
            sb.append(args[i++].toString())
            k += 2
        } else {
            sb.append(template[k])
            k++
        }
    }
    return sb.toString()
}
