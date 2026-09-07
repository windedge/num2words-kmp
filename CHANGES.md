# Changelog

## 0.1.0 -- 2025-01-15 (initial Kotlin Multiplatform port)

* Initial Kotlin Multiplatform port from num2words (Python).
* 13 languages: en, zh (+zh_CN, zh_HK, zh_TW), de, fr, es, it, pt, ja, ko, ar.
* Targets: JVM, JS (Node), wasmJs, Linux x64, macOS x64 / arm64, mingw x64, iOS x64 / arm64 / simulator-arm64.
* Self-contained `BigInt` and `SimpleDecimal` (no `java.math`); pure common code, no third-party dependencies.
* KMP-compatible error types (`Num2WordsOverflowError`, `Num2WordsValueError`, `Num2WordsTypeError`, `Num2WordsNotImplemented`) so behaviour is identical on every target.
* Output strictly aligned with Python num2words, byte-for-byte, including whitespace, hyphenation, and case.
* Golden-sample tests migrated from Python `tests/test_*.py` (17 test classes, 100+ assertions, covering cardinal / ordinal / ordinal-num / year / currency across all 13 languages).
* Verification script `scripts/verify_against_python.py` runs the Kotlin shim and diffs against the Python original.