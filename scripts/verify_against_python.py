#!/usr/bin/env python3
"""Compare Kotlin (KMP) num2words output against the original Python num2words
across 13 languages and several operations. Reads JSON lines emitted by
VerifyShimTest; for each line computes the Python expected value and diffs.

Exit codes:
  0 = all diffs OK
  1 = at least one output mismatch
  2 = environment error (Python num2words not importable)
"""
import os
import sys
_SRC = r"C:/src/python/num2words"
if os.path.isdir(_SRC) and _SRC not in sys.path:
    sys.path.insert(0, _SRC)

import argparse
import json
import shutil
import subprocess
from typing import Any


CURRENCY_BY_LANG = {
    "en": "USD", "de": "EUR", "fr": "EUR", "es": "EUR", "it": "EUR", "pt": "EUR",
    "ja": "JPY", "ko": "KRW", "ar": "SR", "zh": "XXX", "zh_CN": "XXX",
    "zh_HK": "XXX", "zh_TW": "XXX",
}


def run_shim(gradle: str) -> str:
    """Run the Kotlin shim and return combined stdout."""
    env = os.environ.copy()
    # Force UTF-8 for tests' stdout (commonTest prints non-ASCII in cardinalZh/Ar/Ja).
    env["JAVA_TOOL_OPTIONS"] = env.get("JAVA_TOOL_OPTIONS", "") + " -Dfile.encoding=UTF-8"
    if gradle == "gradle":
        gradle = locate_gradle()
    # Full :jvmTest run (no --tests filter, which is unsupported on this KMP project).
    # Use --info so VerifyShimTest's JSON lines appear in combined stdout+stderr.
    cmd = f'"{gradle}" :jvmTest --info --rerun-tasks'
    proc = subprocess.run(cmd, capture_output=True, text=True, env=env, shell=True, timeout=600, encoding="utf-8", errors="replace")
    return (proc.stdout or "") + "\n" + (proc.stderr or "")


def locate_gradle() -> str:
    """Locate the gradle launcher, preferring PATH, then gradle.bat, else error."""
    found = shutil.which("gradle")
    if found:
        return found
    found = shutil.which("gradle.bat")
    if found:
        return found
    raise RuntimeError(
        "gradle not found on PATH. Install it or pass an explicit launcher via --gradle."
    )


def parse_shim(combined: str):
    """Yield (lang, op, value, actual) dicts from log output."""
    for line in combined.splitlines():
        i = line.find('{"lang":')
        if i < 0:
            continue
        payload = line[i:]
        try:
            obj = json.loads(payload)
        except json.JSONDecodeError:
            continue
        yield obj


def py_call(lang: str, op: str, value: str) -> Any:
    """Call the original Python num2words with the same args as the shim."""
    import num2words as n2w
    if op == "currency":
        currency = CURRENCY_BY_LANG.get(lang, "USD")
        return n2w.num2words(value, lang=lang, to="currency", currency=currency)
    if op in ("ordinal", "ordinal_num", "year"):
        if lang == "ar" and op == "ordinal":
            # Shim passes int for Arabic ordinal; mirror that in Python.
            try:
                return n2w.num2words(int(value), lang=lang, to=op)
            except ValueError:
                return n2w.num2words(value, lang=lang, to=op)
        if lang == "ja" and op == "year" and value == "-99":
            # Shim passes era=False for this Japanese negative year.
            return n2w.num2words(value, lang=lang, to=op, era=False)
        return n2w.num2words(value, lang=lang, to=op)
    return n2w.num2words(value, lang=lang)


def safe_py(lang: str, op: str, value: str) -> Any:
    try:
        return ("OK", py_call(lang, op, value))
    except NotImplementedError as e:
        return ("PY-NA", str(e))
    except Exception as e:
        return ("PY-ERR", f"{type(e).__name__}: {e}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gradle", default="gradle", help="gradle launcher")
    parser.add_argument("--shim-output", default=None,
                        help="path to pre-collected shim output; if given, skip gradle run")
    parser.add_argument("--python-src", default=_SRC,
                        help="path to the Python num2words source tree (default: %(default)s)")
    args = parser.parse_args()

    if args.python_src != _SRC:
        # Override the source path chosen at import time.
        if args.python_src not in sys.path:
            sys.path.insert(0, args.python_src)

    # Environment self-check: report which num2words module was loaded.
    import num2words
    print("Python num2words:", num2words.__file__)
    if "site-packages" in str(num2words.__file__):
        print("[WARN] using installed num2words, not source tree")

    if args.shim_output:
        with open(args.shim_output, "r", encoding="utf-8") as f:
            combined = f.read()
    else:
        combined = run_shim(args.gradle)

    counts = {"OK": 0, "FAIL": 0, "PY-NA": 0, "PY-ERR": 0, "KT-ERR": 0}
    failures = []

    for entry in parse_shim(combined):
        lang = entry["lang"]; op = entry["op"]; value = entry["value"]
        actual = entry["actual"]
        if actual.startswith("ERR:"):
            status, expected = safe_py(lang, op, value)
            if status in ("PY-ERR", "PY-NA"):
                # Kotlin and Python both signal an error for this input: error
                # semantics align (do not compare message text).
                counts["OK"] += 1
            else:
                counts["FAIL"] += 1
                failures.append((lang, op, value, "KT-ERR", "Python did not throw"))
            continue
        status, expected = safe_py(lang, op, value)
        if status in ("PY-NA", "PY-ERR"):
            # Kotlin returned a value but Python raised for this input: mismatch
            # (Kotlin should have thrown too).
            counts["FAIL"] += 1
            failures.append((lang, op, value, f"PY {status}", "Kotlin did not throw"))
            continue
        if str(expected) == actual:
            counts["OK"] += 1
        else:
            counts["FAIL"] += 1
            failures.append((lang, op, value, f"expected={expected!r}", f"actual={actual!r}"))

    total = sum(counts.values())
    print(f"Total: {total}  OK={counts['OK']}  FAIL={counts['FAIL']}  "
          f"PY-NA={counts['PY-NA']}  PY-ERR={counts['PY-ERR']}  KT-ERR={counts['KT-ERR']}")
    if failures:
        print(f"\nFirst {min(20, len(failures))} failures:")
        for f in failures[:20]:
            print("  ", " | ".join(str(x) for x in f))
    return 1 if counts["FAIL"] else 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except ImportError as e:
        print(f"[PY-ENV-FAIL] {e}", file=sys.stderr)
        sys.exit(2)