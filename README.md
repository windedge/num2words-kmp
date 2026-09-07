# num2words-kmp

Kotlin Multiplatform port of [num2words](https://github.com/savoirfairelinux/num2words): convert numbers to words in 13 languages, with output that strictly aligns byte-for-byte with the original Python implementation.

Targets: JVM, JS (Node), wasmJs, Linux x64, macOS x64 / arm64, mingw x64, iOS x64 / arm64 / simulator-arm64. Common code is pure Kotlin (no third-party dependencies, no `java.*`).

## Languages

| Code | Language | Variants |
| --- | --- | --- |
| `en` | English | |
| `zh` | Chinese | `zh` / `zh_CN` (Simplified) / `zh_HK` (Cantonese forms) / `zh_TW` (Chinese + Bopomofo reading) |
| `de` | German | |
| `fr` | French | |
| `es` | Spanish | |
| `it` | Italian | |
| `pt` | Portuguese | |
| `ja` | Japanese | kanji / hiragana, rendaku, era calendar |
| `ko` | Korean | |
| `ar` | Arabic | |

## Build and test

```bash
./gradlew build         # compile all targets
./gradlew jvmTest       # JVM unit tests (the main test target)
./gradlew jsTest        # JS Node tests
```

Test count: 17 classes, 100+ golden-sample cases, all migrated from the Python `tests/test_*.py`. Targets cover cardinal / ordinal / ordinal-num / year / currency.

## Usage

```kotlin
import io.github.windedge.num2words.num2words

println(num2words(42))                                          // forty-two
println(num2words(1234, lang = "zh"))                            // 一千二百三十四
println(num2words(1.50, lang = "ja", to = "currency",
                    options = mapOf("currency" to "JPY")))         // 百五十円
println(num2words(2021, lang = "ja", to = "year"))                // 令和三年
println(num2words(10, lang = "zh", to = "ordinal",
                    options = mapOf("counter" to "")))              // 第十
```

For type-safe calls, use `io.github.windedge.num2words.Num2Words.convert(...)`, which accepts `Long`, `Int`, `Double`, `String` (parsed via `SimpleDecimal`), or the `NumValue` sealed interface (Whole / Decimal / FloatVal).

The `options` map keys (all languages accept unknown keys silently):

- `currency`: ISO code (default per language).
- `separator`, `cents`, `adjective`: currency form control.
- `prefer`, `reading`, `counter`, `stuff_zero`, `gender`, `era`, `longval`, `prefix`, `suffix`: language-specific form selection.

## Errors

Throws `Num2WordsException` subtypes, defined in common code (so they are portable to JS / Native where `java.lang.Throwable` is unreliable):

- `Num2WordsOverflowError` (value exceeds `MAXVAL`).
- `Num2WordsValueError` (bad input: non-integral for ordinal, unknown language, etc.).
- `Num2WordsTypeError` (reserved).
- `Num2WordsNotImplemented` (unsupported currency, etc.).

## Verify against Python

```bash
python3 scripts/verify_against_python.py
```

Builds and runs `VerifyShimTest` (logs JSON lines to stdout), then asks the original Python `num2words` to compute the same inputs and diffs them. Exit code `0` = all match.

## Source alignment

This port preserves the Python output exactly, including whitespace, hyphenation, and case. The Python tests were the source of truth: every assertion in `tests/test_*.py` was migrated as a Kotlin golden-sample assertion in `commonTest/`. The shim in `VerifyShimTest` provides a wider regression sweep against future changes.

## Design notes

- `BigInt` and `SimpleDecimal` are self-contained (no `java.math`) so the same code runs unchanged on JS and Native.
- Constructor-time field initialization in `Num2WordBase` runs before `setup()` is called; languages that need non-null fields in `setup()` must initialize them at declaration (a Kotlin/Java gotcha documented in `PLAN.md`).
- `is Int` on Kotlin/JS matches any `Number` (every number is a `Double`); dispatch order in `when` blocks must place `Double` / `Float` first.
- `Map<String, Any?>` carries per-call options to language hooks; unknown keys are ignored, matching Python's `**kwargs` semantics.
## License

LGPL-2.1, see LICENSE. Ported from num2words (LGPL-2.1).
