#import "JVMRunner.h"
#import <dlfcn.h>
#import <dispatch/dispatch.h>
#import <sys/stat.h>
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

typedef int (*GLFWInitProbeFn)(void);

static NSString *DocumentsDirectory(void) {
	NSArray<NSString *> *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	return paths.firstObject ?: NSTemporaryDirectory();
}

static BOOL EnsureDirectory(NSString *path, NSError **error) {
	return [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:error];
}

static NSString *LauncherLogFilePath(void) {
	NSString *documents = DocumentsDirectory();
	NSString *logsPath = [[documents stringByAppendingPathComponent:@"LWJGLLauncher"] stringByAppendingPathComponent:@"logs"];
	[[NSFileManager defaultManager] createDirectoryAtPath:logsPath withIntermediateDirectories:YES attributes:nil error:nil];
	return [logsPath stringByAppendingPathComponent:@"launcher.log"];
}

static void AppendLauncherLog(NSString *line) {
	NSString *path = LauncherLogFilePath();
	NSString *full = [NSString stringWithFormat:@"%@\n", line ?: @"(null)"];
	NSFileHandle *fh = [NSFileHandle fileHandleForWritingAtPath:path];
	if (fh == nil) {
		[full writeToFile:path atomically:YES encoding:NSUTF8StringEncoding error:nil];
		return;
	}
	@try {
		[fh seekToEndOfFile];
		[fh writeData:[full dataUsingEncoding:NSUTF8StringEncoding]];
		[fh closeFile];
	} @catch (__unused NSException *exception) {
	}
}

static void RedirectStdIOToLogs(NSString *logsPath) {
	NSString *stdoutPath = [logsPath stringByAppendingPathComponent:@"stdout.log"];
	NSString *stderrPath = [logsPath stringByAppendingPathComponent:@"stderr.log"];

	freopen(stdoutPath.fileSystemRepresentation, "a+", stdout);
	freopen(stderrPath.fileSystemRepresentation, "a+", stderr);
	setvbuf(stdout, NULL, _IOLBF, 0);
	setvbuf(stderr, NULL, _IOLBF, 0);
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

static NSString *ProbeDummyNativeLibrary(NSString *nativePath) {
	NSString *dummyPath = [nativePath stringByAppendingPathComponent:@"liblwjgldummy.dylib"];
	void *dummyHandle = dlopen(dummyPath.fileSystemRepresentation, RTLD_NOW | RTLD_LOCAL);
	if (!dummyHandle) {
		const char *detail = dlerror();
		return [NSString stringWithFormat:@"Dummy native library not loaded: %s", detail ? detail : "unknown"];
	}

	GLFWInitProbeFn initProbe = reinterpret_cast<GLFWInitProbeFn>(dlsym(dummyHandle, "glfwInit"));
	if (!initProbe) {
		const char *detail = dlerror();
		dlclose(dummyHandle);
		return [NSString stringWithFormat:@"dlsym(glfwInit) failed: %s", detail ? detail : "unknown"];
	}

	const int probeResult = initProbe();
	dlclose(dummyHandle);
	return [NSString stringWithFormat:@"dlopen/dlsym probe ok (glfwInit=%d) at %@", probeResult, dummyPath];
}

static void SetPathEnv(NSString *name, NSArray<NSString *> *entries) {
	NSMutableArray<NSString *> *existing = [NSMutableArray array];
	NSFileManager *fm = [NSFileManager defaultManager];
	for (NSString *entry in entries) {
		BOOL isDir = NO;
		if ([fm fileExistsAtPath:entry isDirectory:&isDir] && isDir) {
			[existing addObject:entry];
		}
	}
	if (existing.count > 0) {
		NSString *joined = [existing componentsJoinedByString:@":"];
		setenv(name.UTF8String, joined.UTF8String, 1);
	}
}

static int LaunchJVMInProcess(NSString **messageOut) {
	AppendLauncherLog(@"[launcher] LaunchJVMInProcess start");

	NSFileManager *manager = [NSFileManager defaultManager];
	NSString *documents = DocumentsDirectory();
	NSString *launcherRoot = [documents stringByAppendingPathComponent:@"LWJGLLauncher"];
	NSString *homePath = [launcherRoot stringByAppendingPathComponent:@"home"];
	NSString *tmpPath = [launcherRoot stringByAppendingPathComponent:@"tmp"];
	NSString *logsPath = [launcherRoot stringByAppendingPathComponent:@"logs"];

	NSError *error = nil;
	for (NSString *path in @[launcherRoot, homePath, tmpPath, logsPath]) {
		if (!EnsureDirectory(path, &error)) {
			*messageOut = [NSString stringWithFormat:@"Failed to create %@: %@", path, error.localizedDescription];
			AppendLauncherLog([NSString stringWithFormat:@"[launcher] mkdir failed: %@", *messageOut]);
			return -10;
		}
	}

	RedirectStdIOToLogs(logsPath);
	setenv("HOME", homePath.fileSystemRepresentation, 1);
	setenv("TMPDIR", tmpPath.fileSystemRepresentation, 1);
	chdir(homePath.fileSystemRepresentation);

	NSString *resourcesPath = NSBundle.mainBundle.resourcePath;
	NSString *jarPath = [resourcesPath stringByAppendingPathComponent:@"java/game-ios.jar"];
	NSString *nativePath = [resourcesPath stringByAppendingPathComponent:@"lwjgl-natives"];
	NSString *jreRoot = [resourcesPath stringByAppendingPathComponent:@"jre"];
	NSString *jliPath = FindJLIPath(jreRoot);
	NSString *nativeProbe = ProbeDummyNativeLibrary(nativePath);

	AppendLauncherLog([NSString stringWithFormat:@"[launcher] resourcePath=%@", resourcesPath]);
	AppendLauncherLog([NSString stringWithFormat:@"[launcher] jreRoot=%@", jreRoot]);
	AppendLauncherLog([NSString stringWithFormat:@"[launcher] jliPath=%@", jliPath ?: @"(nil)"]);
	AppendLauncherLog([NSString stringWithFormat:@"[launcher] nativeProbe=%@", nativeProbe]);

	if (![manager fileExistsAtPath:jarPath]) {
		*messageOut = [NSString stringWithFormat:@"Missing Java payload: %@", jarPath];
		AppendLauncherLog([NSString stringWithFormat:@"[launcher] %@", *messageOut]);
		return -11;
	}

	if (jliPath == nil) {
		*messageOut = [NSString stringWithFormat:@"Missing JLI library under %@", jreRoot];
		AppendLauncherLog([NSString stringWithFormat:@"[launcher] %@", *messageOut]);
		return -12;
	}

	setenv("JAVA_HOME", jreRoot.fileSystemRepresentation, 1);
	setenv("CLASSPATH", jarPath.fileSystemRepresentation, 1);
	SetPathEnv(@"DYLD_LIBRARY_PATH", @[
		[jreRoot stringByAppendingPathComponent:@"lib"],
		[jreRoot stringByAppendingPathComponent:@"lib/jli"],
		[jreRoot stringByAppendingPathComponent:@"lib/server"],
		[jreRoot stringByAppendingPathComponent:@"lib/zero"],
		nativePath
	]);
	const char *dyld = getenv("DYLD_LIBRARY_PATH");
	if (dyld != nullptr) {
		setenv("DYLD_FALLBACK_LIBRARY_PATH", dyld, 1);
		setenv("LD_LIBRARY_PATH", dyld, 1);
	}

	void *jliHandle = dlopen(jliPath.fileSystemRepresentation, RTLD_NOW | RTLD_GLOBAL);
	if (!jliHandle) {
		const char *detail = dlerror();
		*messageOut = [NSString stringWithFormat:@"dlopen failed for %@: %s", jliPath, detail ? detail : "unknown"];
		AppendLauncherLog([NSString stringWithFormat:@"[launcher] %@", *messageOut]);
		return -13;
	}

	JLI_LaunchFn launch = reinterpret_cast<JLI_LaunchFn>(dlsym(jliHandle, "JLI_Launch"));
	if (!launch) {
		const char *detail = dlerror();
		dlclose(jliHandle);
		*messageOut = [NSString stringWithFormat:@"dlsym(JLI_Launch) failed: %s", detail ? detail : "unknown"];
		AppendLauncherLog([NSString stringWithFormat:@"[launcher] %@", *messageOut]);
		return -14;
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
		@"game.GameMain"
	];

	std::vector<char *> argv;
	argv.reserve(javaArgs.count);
	for (NSString *argument in javaArgs) {
		argv.push_back(strdup(argument.UTF8String ?: ""));
	}

	int exitCode = -99;
	@try {
		AppendLauncherLog(@"[launcher] calling JLI_Launch");
		exitCode = launch(
			static_cast<int>(argv.size()),
			argv.data(),
			0,
			nullptr,
			0,
			nullptr,
			"1.8.0",
			"1.8",
			"java",
			"LWJGLLauncher",
			static_cast<jboolean>(0),
			static_cast<jboolean>(0),
			static_cast<jboolean>(0),
			0
		);
	} @catch (NSException *exception) {
		*messageOut = [NSString stringWithFormat:@"Exception while launching JVM: %@ - %@", exception.name, exception.reason];
		AppendLauncherLog([NSString stringWithFormat:@"[launcher] exception: %@", *messageOut]);
		exitCode = -150;
	}

	for (char *argument : argv) {
		free(argument);
	}
	dlclose(jliHandle);

	if (messageOut != nullptr && (exitCode != -150 || *messageOut == nil || (*messageOut).length == 0)) {
		*messageOut = [NSString stringWithFormat:
			@"JVM launch returned %d.\nNative probe: %@\nLogs: %@\nLauncher log: %@\nClasspath: %@",
			exitCode,
			nativeProbe,
			logsPath,
			LauncherLogFilePath(),
			jarPath
		];
	}
	AppendLauncherLog([NSString stringWithFormat:@"[launcher] done exitCode=%d", exitCode]);
	return exitCode;
}

@implementation JVMRunner

+ (void)launchGameWithCompletion:(JVMCompletion)completion {
	dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0), ^{
		NSString *message = @"Launcher finished.";
		int exitCode = -299;
		@try {
			exitCode = LaunchJVMInProcess(&message);
		} @catch (NSException *exception) {
			message = [NSString stringWithFormat:@"Fatal launcher exception: %@ - %@", exception.name, exception.reason];
			exitCode = -298;
			AppendLauncherLog([NSString stringWithFormat:@"[launcher] fatal exception: %@", message]);
		}
		dispatch_async(dispatch_get_main_queue(), ^{
			completion(exitCode, message);
		});
	});
}

@end
