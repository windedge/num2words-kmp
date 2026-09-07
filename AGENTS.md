## Project

num2words-kmp: Kotlin Multiplatform port of Python num2words. Converts numbers to words in 13 languages, output byte-identical to the Python original.

- Package: `io.github.windedge.num2words`
- Maven coordinates: `io.github.windedge.num2words:num2words`
- Source layout: all implementation in `src/commonMain/kotlin/io/github/windedge/num2words/` (no platform-specific sources, no expect/actual)
- Language implementations live under `lang/` (En, De, Fr, Es, It, Pt, Ja, Ko, Ar, Zh + ZhCn/ZhHk/ZhTw)
- Tests: `src/commonTest/kotlin/`, 178 golden-sample assertions migrated from Python `tests/test_*.py`

## Build & Toolchain

- Kotlin 2.1.20, Gradle Kotlin DSL, wrapper 8.12
- JVM toolchain 21 for building; JVM artifact targets Java 17 bytecode (`jvmTarget.set(JvmTarget.JVM_17)`)
- JS: `js(IR) { nodejs() }` (tests run on Node; artifact is browser-compatible)
- wasmJs: `nodejs()` test environment
- Native: linuxX64, macosX64, macosArm64, mingwX64, iosX64, iosArm64, iosSimulatorArm64
- Publishing: vanniktech maven-publish 0.34.0, version/coordinates in `gradle.properties`

## Key Constraints

- commonMain must stay pure Kotlin: no `java.*`, no `BigDecimal`, no third-party deps. BigInt and SimpleDecimal are self-contained.
- Output must match Python exactly (whitespace, hyphenation, case). Golden-sample tests are the source of truth.
- JS: `is Int` matches any Number; `Double`/`Float` dispatch must come before `Int` in `when` blocks.
- Constructor field init in `Num2WordBase` runs before `setup()`; non-null fields used in `setup()` must be initialized at declaration.

## Verification

- `./gradlew jvmTest` runs the main test suite.
- `python3 scripts/verify_against_python.py` builds `VerifyShimTest`, logs JSON lines, then diffs against real Python `num2words` (reference at `C:/src/python/num2words`). Exit 0 = all match.

## Release

- Tag `v*` triggers `.github/workflows/release.yml` (macos-latest, GPG signing via GitHub secrets, publish to Maven Central, then `gh release create`).
- GPG key is S2K-encrypted Ed25519; `SIGNING_PASSWORD` secret must be the Java-unescaped value from `~/.gradle/gradle.properties` (backslash in the raw value is a Properties escape).
- `GPG_KEY_CONTENTS` secret must be base64 of the binary secring (754 bytes), not base64 of the armored text.
- After CI creates the GitHub Release, replace its auto-generated body with the CHANGELOG version section via `gh release edit vX.Y.Z --notes-file`.

## Notes

- Use English for code comments.
- Git commit messages: ASCII only (no em-dash, no Unicode chars).
- Keep code comments minimal: explain "why", never restate what the code does.
