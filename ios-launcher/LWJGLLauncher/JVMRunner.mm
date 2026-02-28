#import "JVMRunner.h"
#import <dispatch/dispatch.h>
#import <errno.h>
#import <spawn.h>
#import <sys/stat.h>
#import <sys/wait.h>
#import <unistd.h>
#include <string.h>

extern char **environ;

static NSString *DocumentsDirectory(void) {
	NSArray<NSString *> *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	return paths.firstObject ?: NSTemporaryDirectory();
}

static BOOL EnsureDirectory(NSString *path, NSError **error) {
	return [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:error];
}

static int WaitForChild(pid_t pid) {
	int status = 0;
	while (waitpid(pid, &status, 0) < 0) {
		if (errno == EINTR) {
			continue;
		}
		return -203;
	}

	if (WIFEXITED(status)) {
		return WEXITSTATUS(status);
	}
	if (WIFSIGNALED(status)) {
		return 128 + WTERMSIG(status);
	}
	return -204;
}

static int LaunchJVMHost(NSString **messageOut) {
	NSFileManager *manager = [NSFileManager defaultManager];
	NSString *bundlePath = NSBundle.mainBundle.bundlePath;
	NSString *hostPath = [bundlePath stringByAppendingPathComponent:@"LWJGLJVMHost"];

	NSString *documents = DocumentsDirectory();
	NSString *launcherRoot = [documents stringByAppendingPathComponent:@"LWJGLLauncher"];
	NSString *logsPath = [launcherRoot stringByAppendingPathComponent:@"logs"];

	NSError *error = nil;
	if (!EnsureDirectory(logsPath, &error)) {
		*messageOut = [NSString stringWithFormat:@"Failed to create log directory: %@", error.localizedDescription];
		return -200;
	}

	if (![manager fileExistsAtPath:hostPath]) {
		*messageOut = [NSString stringWithFormat:@"Missing helper executable: %@", hostPath];
		return -201;
	}

	if (access(hostPath.fileSystemRepresentation, X_OK) != 0) {
		chmod(hostPath.fileSystemRepresentation, 0755);
	}

	char *argv[] = {
		const_cast<char *>(hostPath.fileSystemRepresentation),
		const_cast<char *>("game.GameMain"),
		nullptr
	};

	pid_t pid = 0;
	int spawnResult = posix_spawn(&pid, hostPath.fileSystemRepresentation, nullptr, nullptr, argv, environ);
	if (spawnResult != 0) {
		*messageOut = [NSString stringWithFormat:@"posix_spawn failed (%d): %s", spawnResult, strerror(spawnResult)];
		return -202;
	}
	if (pid <= 0) {
		*messageOut = [NSString stringWithFormat:@"posix_spawn returned invalid pid %d", pid];
		return -205;
	}

	int exitCode = WaitForChild(pid);
	*messageOut = [NSString stringWithFormat:
		@"Helper exited with code %d.\nHelper: %@\nLogs: %@\nCheck gl_calls.txt, stdout.log, stderr.log.",
		exitCode,
		hostPath,
		logsPath
	];
	return exitCode;
}

@implementation JVMRunner

+ (void)launchGameWithCompletion:(JVMCompletion)completion {
	dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0), ^{
		NSString *message = @"Launcher finished.";
		int exitCode = -299;
		@try {
			exitCode = LaunchJVMHost(&message);
		} @catch (NSException *exception) {
			message = [NSString stringWithFormat:@"Launcher exception: %@ - %@", exception.name, exception.reason];
			exitCode = -298;
		}
		dispatch_async(dispatch_get_main_queue(), ^{
			completion(exitCode, message);
		});
	});
}

@end
