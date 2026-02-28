# LWJGL TrollStore Launcher (Theos)

This folder adds a Theos app target that:

- Builds an `arm64` iOS app bundle (`LWJGLLauncher.app`)
- Builds and embeds `liblwjgldummy.dylib` into `lwjgl-natives/`
- Embeds a Java payload jar generated from this repository
- Loads a bundled iOS OpenJDK runtime through `libjli`
- Forces interpreter mode (`-Xint`) to avoid JIT dependency
- Redirects runtime files/logs to the app Documents directory
- Packages a TrollStore installable `.ipa`

## Layout

- `Makefile`: Theos app build definition.
- `native-lwjgl-dummy/dummy_lwjgl.mm`: Dummy GLFW/OpenGL C exports + JNI bridge.
- `control`: Debian control metadata (for `.deb` output).
- `LWJGLLauncher/`: Objective-C++ iOS launcher sources.
- `java-stubs/`: LWJGL compatibility stubs for non-OpenGL bootstrapping.
- `scripts/build_game_jar.sh`: Compiles original game + stubs to `game-ios.jar`.
- `scripts/prepare_jre.sh`: Copies external iOS arm64 OpenJDK runtime into resources.
- `scripts/make_ipa.sh`: Builds TrollStore `.ipa` from staged app.
- `vendor/jre8-zero-aarch64-ios.tar.xz`: Bundled iOS arm64 Zero VM JRE archive used by CI.

## Ubuntu (WSL) Build Prerequisites

1. Install host dependencies:
   `sudo apt update && sudo apt install -y git make clang zip unzip rsync openjdk-17-jdk`
2. Install `ldid` (build from source if package unavailable).
3. Install Theos and SDKs:
   - `export THEOS=$HOME/theos`
   - `git clone --recursive https://github.com/theos/theos.git "$THEOS"`
   - `git clone https://github.com/theos/sdks.git "$THEOS/sdks"`
4. Ensure Theos toolchain is in `PATH` if your setup requires it.

## Build Commands (Ubuntu)

From repository root:

```bash
./ios-launcher/scripts/build_game_jar.sh
cd ios-launcher
make clean stage
make ipa
```

Optional if you have an iOS arm64 JRE build ready:

```bash
./scripts/prepare_jre.sh /absolute/path/to/openjdk-ios-arm64
```

Artifacts:

- `.theos/_/Applications/LWJGLLauncher.app`
- `packages/LWJGLLauncher-trollstore.ipa`
- `.theos/_/Applications/LWJGLLauncher.app/lwjgl-natives/liblwjgldummy.dylib`

## TrollStore Install

1. Transfer `packages/LWJGLLauncher-trollstore.ipa` to the device.
2. Open the IPA in TrollStore and install.
3. Launch `LWJGL Launcher`.
4. Check logs in app Documents:
   - `LWJGLLauncher/logs/stdout.log`
   - `LWJGLLauncher/logs/stderr.log`

## Key Runtime Notes

- Java startup entry is `game.GameMain`.
- Launcher passes:
  - `-Xint`
  - `-Djava.class.path=<...>/game-ios.jar`
  - `-Duser.home=<Documents>/LWJGLLauncher/home`
  - `-Djava.io.tmpdir=<Documents>/LWJGLLauncher/tmp`
- Current graphics path is stubbed (`org.lwjgl.*` Java stubs).
- `GLFW` and `GL11` stubs call JNI native methods in `liblwjgldummy.dylib`.
- Native calls are logged to:
  - `Documents/LWJGLLauncher/logs/gl_calls.txt`

## Known Limitations

- Desktop LWJGL native libraries are not iOS-compatible.
- GLFW/OpenGL desktop context APIs used by this game do not map directly to iOS.
- Current stubs allow basic startup/logic attempts, not real rendering.
- No Metal renderer is implemented yet.
- Input handling is stubbed (no touch-to-game mapping yet).
