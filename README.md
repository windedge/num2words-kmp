# num2words

Kotlin Multiplatform port of [num2words](https://github.com/savoirfairelinux/num2words): convert numbers to words in 13 languages, byte-for-byte identical to the original Python output.

Targets: JVM, JS, wasmJs, Android (via JVM), Linux, macOS, Windows, iOS. No third-party dependencies.

## Installation

```kotlin
// version catalog (libs.versions.toml)
[versions]
num2words = "0.1.2"

[libraries]
num2words = { module = "io.github.windedge.num2words:num2words", version.ref = "num2words" }
```

```kotlin
// or directly in build.gradle.kts
implementation("io.github.windedge.num2words:num2words:0.1.2")
```

Kotlin Multiplatform resolves the platform artifact automatically; no `-jvm` / `-js` suffix needed.

## Usage

```kotlin
import io.github.windedge.num2words.num2words

num2words(42)                                    // forty-two
num2words(1234, lang = "zh")                      // 一千二百三十四
num2words(150, lang = "ja", to = "currency",
          options = mapOf("currency" to "JPY"))   // 百五十円
num2words(2021, lang = "ja", to = "year")         // 令和三年
num2words(10, lang = "zh", to = "ordinal",
          options = mapOf("counter" to ""))       // 第十
```

`to` accepts `"cardinal"` (default), `"ordinal"`, `"ordinal_num"`, `"year"`, `"currency"`. `lang` accepts the codes below; unknown options in the map are ignored.

## Languages

| Code | Language | Variants |
| --- | --- | --- |
| `en` | English | |
| `zh` | Chinese | `zh_CN` / `zh_HK` / `zh_TW` |
| `de` | German | |
| `fr` | French | |
| `es` | Spanish | |
| `it` | Italian | |
| `pt` | Portuguese | |
| `ja` | Japanese | |
| `ko` | Korean | |
| `ar` | Arabic | |

## Errors

All errors throw `Num2WordsException` subtypes: `Num2WordsOverflowError` (exceeds `MAXVAL`), `Num2WordsValueError` (bad input), `Num2WordsNotImplemented` (unsupported language/currency, e.g. decimals for JPY).

## Compatibility

- Kotlin 2.1+ (both build and consumer)
- JVM artifact is Java 17 bytecode, needs JRE 17+ at runtime
- JS / wasmJs: no Node or DOM dependencies, runs in browsers and Node; native targets via standard KMP dependency

## Development

```bash
./gradlew build      # compile all targets
./gradlew jvmTest    # 178 golden-sample tests migrated from Python tests
python3 scripts/verify_against_python.py   # diff against real Python num2words
```

## License

LGPL-2.1, see LICENSE. Ported from num2words (LGPL-2.1).
