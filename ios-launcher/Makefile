ARCHS = arm64
TARGET = iphone:clang:latest:14.0

include $(THEOS)/makefiles/common.mk

APPLICATION_NAME = LWJGLLauncher
LIBRARY_NAME = lwjgldummy
TOOL_NAME = LWJGLJVMHost

LWJGLLauncher_FILES = \
	LWJGLLauncher/main.mm \
	LWJGLLauncher/AppDelegate.mm \
	LWJGLLauncher/LauncherViewController.mm \
	LWJGLLauncher/JVMRunner.mm

LWJGLLauncher_FRAMEWORKS = UIKit Foundation
LWJGLLauncher_CFLAGS = -fobjc-arc -Wno-nullability-completeness
LWJGLLauncher_INSTALL_PATH = /Applications
LWJGLLauncher_RESOURCE_DIRS = LWJGLLauncher/Resources
LWJGLLauncher_BUNDLE_ID = com.example.lwjgllauncher
LWJGLLauncher_BUNDLE_NAME = LWJGLLauncher
LWJGLLauncher_BUNDLE_DISPLAY_NAME = LWJGL Launcher
LWJGLLauncher_BUNDLE_VERSION = 1
LWJGLLauncher_BUNDLE_SHORT_VERSION_STRING = 0.1.0

lwjgldummy_FILES = native-lwjgl-dummy/dummy_lwjgl.mm
lwjgldummy_FRAMEWORKS = Foundation
lwjgldummy_CFLAGS = -fobjc-arc -fvisibility=hidden
lwjgldummy_INSTALL_PATH = /Applications/LWJGLLauncher.app/lwjgl-natives
lwjgldummy_LDFLAGS = -Wl,-install_name,@rpath/liblwjgldummy.dylib

LWJGLJVMHost_FILES = jvmhostsrc/main.mm
LWJGLJVMHost_FRAMEWORKS = Foundation
LWJGLJVMHost_CFLAGS = -fobjc-arc -Wno-nullability-completeness
LWJGLJVMHost_INSTALL_PATH = /Applications/LWJGLLauncher.app

include $(THEOS_MAKE_PATH)/application.mk
include $(THEOS_MAKE_PATH)/library.mk
include $(THEOS_MAKE_PATH)/tool.mk

.PHONY: build-java prepare-jre ipa

build-java:
	./scripts/build_game_jar.sh

prepare-jre:
	./scripts/prepare_jre.sh

before-stage:: build-java

ipa: stage
	./scripts/make_ipa.sh
