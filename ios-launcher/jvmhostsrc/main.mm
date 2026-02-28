#import <Foundation/Foundation.h>
#import <dlfcn.h>
#import <sys/stat.h>
#import <sys/types.h>
#import <limits.h>
#import <unistd.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <vector>

typedef unsigned char jboolean;
typedef int jint;

typedef int (*JLI_LaunchFn)(
	int argc,
	char **argv,
	int jargc,
	const char **jargv,
	int appclassc,
	const char **appclassv,
	const char *fullversion,
	const char *dotversion,
	const char *pname,
	const char *lname,
	jboolean javaargs,
	jboolean cpwildcard,
	jboolean javaw,
	jint ergo
);

static BOOL EnsureDirectory(NSString *path, NSError **error) {
	return [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:error];
}

static NSString *DocumentsDirectory(void) {
	NSArray<NSString *> *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	return paths.firstObject ?: NSTemporaryDirectory();
}

static void RedirectStdIOToLogs(NSString *logsPath) {
	NSString *stdoutPath = [logsPath stringByAppendingPathComponent:@"stdout.log"];
	NSString *stderrPath = [logsPath stringByAppendingPathComponent:@"stderr.log"];

	freopen(stdoutPath.fileSystemRepresentation, "a+", stdout);
	freopen(stderrPath.fileSystemRepresentation, "a+", stderr);
	setvbuf(stdout, NULL, _IOLBF, 0);
	setvbuf(stderr, NULL, _IOLBF, 0);
}

static NSString *ResolvePath(const char *rawPath) {
	if (rawPath == nullptr || rawPath[0] == '\0') {
		return nil;
	}

	char resolved[PATH_MAX] = {0};
	if (realpath(rawPath, resolved) != nullptr) {
		return [NSString stringWithUTF8String:resolved];
	}

	NSString *candidate = [NSString stringWithUTF8String:rawPath];
	if ([candidate hasPrefix:@"/"]) {
		return candidate;
	}

	NSString *cwd = [[NSFileManager defaultManager] currentDirectoryPath];
	return [cwd stringByAppendingPathComponent:candidate];
}

static NSString *FindJLIPath(NSString *jreRoot) {
	NSArray<NSString *> *candidates = @[
		[jreRoot stringByAppendingPathComponent:@"lib/jli/libjli.dylib"],
		[jreRoot stringByAppendingPathComponent:@"lib/libjli.dylib"],
		[jreRoot stringByAppendingPathComponent:@"lib/jli/libjli.so"]
	];

	NSFileManager *manager = [NSFileManager defaultManager];
	for (NSString *candidate in candidates) {
		if ([manager fileExistsAtPath:candidate]) {
			return candidate;
		}
	}
	return nil;
}

static void SetOrAppendEnv(NSString *name, NSString *value) {
	if (name.length == 0 || value.length == 0) {
		return;
	}

	const char *current = getenv(name.UTF8String);
	if (current == nullptr || current[0] == '\0') {
		setenv(name.UTF8String, value.UTF8String, 1);
		return;
	}

	NSString *merged = [NSString stringWithFormat:@"%@:%s", value, current];
	setenv(name.UTF8String, merged.UTF8String, 1);
}

static int LaunchInHelper(NSString *mainClass) {
	NSString *execPath = ResolvePath(NSProcessInfo.processInfo.arguments.firstObject.UTF8String);
	if (execPath.length == 0) {
		fprintf(stderr, "[host] failed to resolve helper path\n");
		return -101;
	}

	NSString *appRoot = [execPath stringByDeletingLastPathComponent];
	NSString *jarPath = [appRoot stringByAppendingPathComponent:@"java/game-ios.jar"];
	NSString *nativePath = [appRoot stringByAppendingPathComponent:@"lwjgl-natives"];
	NSString *jreRoot = [appRoot stringByAppendingPathComponent:@"jre"];
	NSString *jliPath = FindJLIPath(jreRoot);

	NSFileManager *manager = [NSFileManager defaultManager];
	if (![manager fileExistsAtPath:jarPath]) {
		fprintf(stderr, "[host] missing Java payload: %s\n", jarPath.UTF8String ?: "(null)");
		return -102;
	}

	if (jliPath == nil) {
		fprintf(stderr, "[host] missing JLI under: %s\n", jreRoot.UTF8String ?: "(null)");
		return -103;
	}

	NSString *documents = DocumentsDirectory();
	NSString *launcherRoot = [documents stringByAppendingPathComponent:@"LWJGLLauncher"];
	NSString *homePath = [launcherRoot stringByAppendingPathComponent:@"home"];
	NSString *tmpPath = [launcherRoot stringByAppendingPathComponent:@"tmp"];
	NSString *logsPath = [launcherRoot stringByAppendingPathComponent:@"logs"];

	NSError *error = nil;
	for (NSString *path in @[launcherRoot, homePath, tmpPath, logsPath]) {
		if (!EnsureDirectory(path, &error)) {
			fprintf(stderr, "[host] create directory failed: %s (%s)\n",
					path.UTF8String ?: "(null)",
					error.localizedDescription.UTF8String ?: "unknown");
			return -104;
		}
	}

	RedirectStdIOToLogs(logsPath);
	fprintf(stdout, "[host] helper started\n");
	fprintf(stdout, "[host] appRoot=%s\n", appRoot.UTF8String ?: "(null)");
	fprintf(stdout, "[host] jreRoot=%s\n", jreRoot.UTF8String ?: "(null)");

	setenv("HOME", homePath.fileSystemRepresentation, 1);
	setenv("TMPDIR", tmpPath.fileSystemRepresentation, 1);
	setenv("JAVA_HOME", jreRoot.fileSystemRepresentation, 1);
	setenv("CLASSPATH", jarPath.fileSystemRepresentation, 1);
	chdir(homePath.fileSystemRepresentation);

	NSString *dyldPath = [NSString stringWithFormat:@"%@:%@:%@:%@",
								 [jreRoot stringByAppendingPathComponent:@"lib"],
								 [jreRoot stringByAppendingPathComponent:@"lib/jli"],
								 [jreRoot stringByAppendingPathComponent:@"lib/zero"],
								 nativePath];
	SetOrAppendEnv(@"DYLD_LIBRARY_PATH", dyldPath);
	SetOrAppendEnv(@"DYLD_FALLBACK_LIBRARY_PATH", dyldPath);
	SetOrAppendEnv(@"JAVA_LIBRARY_PATH", nativePath);
	setenv("LD_LIBRARY_PATH", dyldPath.UTF8String, 1);

	void *dummyHandle = dlopen([nativePath stringByAppendingPathComponent:@"liblwjgldummy.dylib"].fileSystemRepresentation,
							 RTLD_NOW | RTLD_GLOBAL);
	if (!dummyHandle) {
		fprintf(stderr, "[host] warning: dummy native preload failed: %s\n", dlerror());
	}

	void *jliHandle = dlopen(jliPath.fileSystemRepresentation, RTLD_NOW | RTLD_GLOBAL);
	if (!jliHandle) {
		fprintf(stderr, "[host] dlopen JLI failed: %s\n", dlerror());
		return -105;
	}

	JLI_LaunchFn launch = reinterpret_cast<JLI_LaunchFn>(dlsym(jliHandle, "JLI_Launch"));
	if (!launch) {
		fprintf(stderr, "[host] dlsym JLI_Launch failed: %s\n", dlerror());
		return -106;
	}

	NSArray<NSString *> *javaArgs = @[
		@"java",
		@"-Xint",
		@"-Xrs",
		@"-Djava.awt.headless=true",
		[NSString stringWithFormat:@"-Djava.class.path=%@", jarPath],
		[NSString stringWithFormat:@"-Djava.library.path=%@", nativePath],
		[NSString stringWithFormat:@"-Dorg.lwjgl.librarypath=%@", nativePath],
		[NSString stringWithFormat:@"-Duser.home=%@", homePath],
		[NSString stringWithFormat:@"-Djava.io.tmpdir=%@", tmpPath],
		@"-Dlwjgl.stub.maxFrames=900",
		mainClass ?: @"game.GameMain"
	];

	std::vector<char *> argv;
	argv.reserve(javaArgs.count);
	for (NSString *argument in javaArgs) {
		argv.push_back(strdup(argument.UTF8String ?: ""));
	}

	fprintf(stdout, "[host] launching JVM main=%s\n", (mainClass ?: @"game.GameMain").UTF8String ?: "game.GameMain");
	int exitCode = launch(
		static_cast<int>(argv.size()),
		argv.data(),
		0,
		nullptr,
		0,
		nullptr,
		"1.8.0",
		"1.8",
		"java",
		"LWJGLJVMHost",
		static_cast<jboolean>(0),
		static_cast<jboolean>(0),
		static_cast<jboolean>(0),
		0
	);

	for (char *argument : argv) {
		free(argument);
	}

	fprintf(stdout, "[host] JVM exited with code %d\n", exitCode);
	return exitCode;
}

int main(int argc, char *argv[]) {
	@autoreleasepool {
		NSString *mainClass = @"game.GameMain";
		if (argc > 1 && argv[1] != nullptr && argv[1][0] != '\0') {
			mainClass = [NSString stringWithUTF8String:argv[1]];
		}
		return LaunchInHelper(mainClass);
	}
}
