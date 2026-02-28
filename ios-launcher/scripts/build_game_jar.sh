#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BUILD_ROOT="$PROJECT_ROOT/ios-launcher/.build/java"
CLASS_DIR="$BUILD_ROOT/classes"
SOURCE_LIST="$BUILD_ROOT/sources.txt"
OUTPUT_JAR="$PROJECT_ROOT/ios-launcher/LWJGLLauncher/Resources/java/game-ios.jar"

rm -rf "$BUILD_ROOT"
mkdir -p "$CLASS_DIR"
mkdir -p "$(dirname "$OUTPUT_JAR")"

find "$PROJECT_ROOT/src" "$PROJECT_ROOT/ios-launcher/java-stubs/src" -name '*.java' | sort > "$SOURCE_LIST"

javac --release 8 -d "$CLASS_DIR" @"$SOURCE_LIST"
jar --create --file "$OUTPUT_JAR" -C "$CLASS_DIR" .

echo "Built $OUTPUT_JAR"
