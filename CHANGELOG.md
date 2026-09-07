# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.1.0] - 2026-04-27

### Added
- Core framework with self-contained BigInt and SimpleDecimal (no java.math).
- 13 language implementations: en, zh (+zh_CN, zh_HK, zh_TW), de, fr, es, it, pt, ja, ko, ar.
- KMP-compatible error types for identical behavior across JVM / JS / Native.
- Golden-sample test suite (17 classes, 100+ assertions) migrated from Python.
- Verification script for byte-for-byte comparison with Python.
- Maven Central publishing via vanniktech plugin and GitHub Actions workflow.
