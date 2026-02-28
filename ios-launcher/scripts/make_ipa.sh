#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_PATH="$PROJECT_ROOT/.theos/_/Applications/LWJGLLauncher.app"
WORK_PATH="$PROJECT_ROOT/.theos/ipa-work"
OUTPUT_DIR="$PROJECT_ROOT/packages"
OUTPUT_IPA="$OUTPUT_DIR/LWJGLLauncher-trollstore.ipa"

if [[ ! -d "$APP_PATH" ]]; then
	echo "Staged app not found at $APP_PATH"
	echo "Run: make stage"
	exit 1
fi

rm -rf "$WORK_PATH"
mkdir -p "$WORK_PATH/Payload" "$OUTPUT_DIR"
cp -a "$APP_PATH" "$WORK_PATH/Payload/"

(
	cd "$WORK_PATH"
	zip -qry "$OUTPUT_IPA" Payload
)

echo "Created TrollStore IPA: $OUTPUT_IPA"
