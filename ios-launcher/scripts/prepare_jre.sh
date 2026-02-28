#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST="$PROJECT_ROOT/LWJGLLauncher/Resources/jre"
SRC="${1:-${JRE_SRC:-}}"

if [[ -z "$SRC" ]]; then
	echo "Usage: $0 /absolute/path/to/openjdk-ios-arm64"
	echo "Or set JRE_SRC=/absolute/path/to/openjdk-ios-arm64"
	exit 1
fi

if [[ ! -d "$SRC" ]]; then
	echo "JRE source directory does not exist: $SRC"
	exit 1
fi

rm -rf "$DEST"
mkdir -p "$DEST"
rsync -a --delete "$SRC"/ "$DEST"/

if [[ ! -f "$DEST/lib/jli/libjli.dylib" && ! -f "$DEST/lib/libjli.dylib" ]]; then
	echo "Warning: libjli was not found in copied runtime."
fi

echo "Copied OpenJDK runtime to $DEST"
