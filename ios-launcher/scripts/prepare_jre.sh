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

if command -v ldid >/dev/null 2>&1; then
	echo "Signing Mach-O binaries in bundled JRE..."
	signed_count=0
	while IFS= read -r -d '' file_path; do
		if file -b "$file_path" | grep -q "Mach-O"; then
			chmod +x "$file_path" || true
			ldid -S "$file_path"
			signed_count=$((signed_count + 1))
		fi
	done < <(find "$DEST" -type f -print0)
	echo "Signed $signed_count Mach-O files with ldid."
else
	echo "Warning: ldid not found. JRE binaries were copied without ad-hoc signing."
fi

echo "Copied OpenJDK runtime to $DEST"
