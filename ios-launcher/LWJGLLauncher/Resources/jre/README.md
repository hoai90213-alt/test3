# Bundled OpenJDK Runtime Placeholder

This directory must contain an **iOS arm64** OpenJDK runtime build.

Minimum expected files:

- `lib/jli/libjli.dylib` (or `lib/libjli.dylib`)
- `lib/server/libjvm.dylib` (runtime-dependent path)
- `lib/modules` (or equivalent module image)
- `conf/`

Notes:

- Use an iOS-compatible OpenJDK build, not Linux arm64 binaries.
- The launcher enforces interpreter mode with `-Xint` to avoid JIT restrictions.
- Copy your runtime here by running:
  `./scripts/prepare_jre.sh /absolute/path/to/openjdk-ios-arm64`
