#import <Foundation/Foundation.h>
#import <dispatch/dispatch.h>
#include <atomic>
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <mutex>
#include <string>
#include <unordered_map>

#ifndef JNIEXPORT
#define JNIEXPORT __attribute__((visibility("default")))
#endif

#ifndef JNICALL
#define JNICALL
#endif

typedef int jint;
typedef long long jlong;
typedef float jfloat;
typedef void *jclass;
typedef void *jstring;

#define EXPORT extern "C" __attribute__((visibility("default")))

namespace {

struct DummyWindow {
	uint64_t id;
	uint64_t polls;
	int width;
	int height;
};

static std::atomic<uint64_t> gNextWindowId(1);
static std::mutex gWindowMutex;
static std::unordered_map<uint64_t, DummyWindow> gWindows;
static std::mutex gLogMutex;

static uint64_t MaxFramesBeforeClose() {
	static uint64_t cached = 0;
	if (cached != 0) {
		return cached;
	}

	const char *env = getenv("LWJGL_DUMMY_MAX_FRAMES");
	if (env != nullptr && env[0] != '\0') {
		char *end = nullptr;
		unsigned long parsed = strtoul(env, &end, 10);
		if (end != env && parsed > 0UL) {
			cached = static_cast<uint64_t>(parsed);
			return cached;
		}
	}

	cached = 900;
	return cached;
}

static std::string LogFilePath() {
	static std::string path;
	static dispatch_once_t onceToken;
	dispatch_once(&onceToken, ^{
		@autoreleasepool {
			NSArray<NSString *> *docPaths =
				NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
			NSString *documents = docPaths.firstObject ?: NSTemporaryDirectory();
			NSString *logsDir = [documents stringByAppendingPathComponent:@"LWJGLLauncher/logs"];
			[[NSFileManager defaultManager] createDirectoryAtPath:logsDir
									  withIntermediateDirectories:YES
												   attributes:nil
														error:nil];
			NSString *logFile = [logsDir stringByAppendingPathComponent:@"gl_calls.txt"];
			path = std::string(logFile.UTF8String ?: "");
		}
	});
	return path;
}

static void LogCall(const char *format, ...) {
	std::lock_guard<std::mutex> lock(gLogMutex);
	const std::string path = LogFilePath();
	if (path.empty()) {
		return;
	}

	FILE *file = fopen(path.c_str(), "a");
	if (file == nullptr) {
		return;
	}

	va_list args;
	va_start(args, format);
	vfprintf(file, format, args);
	va_end(args);
	fprintf(file, "\n");
	fclose(file);
}

static uint64_t WindowIdFromHandle(void *window) {
	return static_cast<uint64_t>(reinterpret_cast<uintptr_t>(window));
}

static void *WindowHandleFromId(uint64_t id) {
	return reinterpret_cast<void *>(static_cast<uintptr_t>(id));
}

} // namespace

EXPORT int glfwInit(void) {
	LogCall("glfwInit() -> 1");
	return 1;
}

EXPORT void *glfwCreateWindow(int width, int height, const char *title, void *monitor, void *share) {
	(void)title;
	(void)monitor;
	(void)share;

	const uint64_t id = gNextWindowId.fetch_add(1);
	DummyWindow window{id, 0, width, height};

	{
		std::lock_guard<std::mutex> lock(gWindowMutex);
		gWindows[id] = window;
	}

	LogCall("glfwCreateWindow(%d, %d, ...) -> 0x%llx", width, height, static_cast<unsigned long long>(id));
	return WindowHandleFromId(id);
}

EXPORT void glfwMakeContextCurrent(void *window) {
	LogCall("glfwMakeContextCurrent(0x%llx)", static_cast<unsigned long long>(WindowIdFromHandle(window)));
}

EXPORT void glfwPollEvents(void) {
	std::lock_guard<std::mutex> lock(gWindowMutex);
	for (auto &entry : gWindows) {
		entry.second.polls += 1;
	}
	LogCall("glfwPollEvents()");
}

EXPORT int glfwWindowShouldClose(void *window) {
	const uint64_t id = WindowIdFromHandle(window);
	const uint64_t maxFrames = MaxFramesBeforeClose();
	uint64_t polls = 0;
	bool found = false;

	{
		std::lock_guard<std::mutex> lock(gWindowMutex);
		auto it = gWindows.find(id);
		if (it != gWindows.end()) {
			polls = it->second.polls;
			found = true;
		}
	}

	const int shouldClose = (!found || polls >= maxFrames) ? 1 : 0;
	LogCall("glfwWindowShouldClose(0x%llx) -> %d (polls=%llu, limit=%llu)",
		static_cast<unsigned long long>(id),
		shouldClose,
		static_cast<unsigned long long>(polls),
		static_cast<unsigned long long>(maxFrames));
	return shouldClose;
}

EXPORT void glClear(unsigned int mask) {
	LogCall("glClear(0x%x)", mask);
}

EXPORT void glClearColor(float red, float green, float blue, float alpha) {
	LogCall("glClearColor(%.3f, %.3f, %.3f, %.3f)", red, green, blue, alpha);
}

EXPORT void glDrawArrays(unsigned int mode, int first, int count) {
	LogCall("glDrawArrays(mode=0x%x, first=%d, count=%d)", mode, first, count);
}

EXPORT void glViewport(int x, int y, int width, int height) {
	LogCall("glViewport(%d, %d, %d, %d)", x, y, width, height);
}

EXPORT JNIEXPORT jint JNICALL Java_org_lwjgl_glfw_GLFW_nGlfwInit(void *, jclass) {
	return glfwInit();
}

EXPORT JNIEXPORT jlong JNICALL Java_org_lwjgl_glfw_GLFW_nGlfwCreateWindow(void *, jclass, jint width, jint height, jstring, jlong monitor, jlong share) {
	void *window = glfwCreateWindow(width, height, nullptr, reinterpret_cast<void *>(static_cast<uintptr_t>(monitor)), reinterpret_cast<void *>(static_cast<uintptr_t>(share)));
	return static_cast<jlong>(reinterpret_cast<uintptr_t>(window));
}

EXPORT JNIEXPORT void JNICALL Java_org_lwjgl_glfw_GLFW_nGlfwMakeContextCurrent(void *, jclass, jlong window) {
	glfwMakeContextCurrent(reinterpret_cast<void *>(static_cast<uintptr_t>(window)));
}

EXPORT JNIEXPORT void JNICALL Java_org_lwjgl_glfw_GLFW_nGlfwPollEvents(void *, jclass) {
	glfwPollEvents();
}

EXPORT JNIEXPORT jint JNICALL Java_org_lwjgl_glfw_GLFW_nGlfwWindowShouldClose(void *, jclass, jlong window) {
	return glfwWindowShouldClose(reinterpret_cast<void *>(static_cast<uintptr_t>(window)));
}

EXPORT JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL11_nGlClear(void *, jclass, jint mask) {
	glClear(static_cast<unsigned int>(mask));
}

EXPORT JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL11_nGlClearColor(void *, jclass, jfloat red, jfloat green, jfloat blue, jfloat alpha) {
	glClearColor(red, green, blue, alpha);
}

EXPORT JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL11_nGlDrawArrays(void *, jclass, jint mode, jint first, jint count) {
	glDrawArrays(static_cast<unsigned int>(mode), first, count);
}

EXPORT JNIEXPORT void JNICALL Java_org_lwjgl_opengl_GL11_nGlViewport(void *, jclass, jint x, jint y, jint width, jint height) {
	glViewport(x, y, width, height);
}
