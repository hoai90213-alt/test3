#import "AppDelegate.h"
#import "LauncherViewController.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
	(void)application;
	(void)launchOptions;

	self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
	LauncherViewController *controller = [[LauncherViewController alloc] init];
	UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:controller];
	self.window.rootViewController = navigationController;
	[self.window makeKeyAndVisible];
	return YES;
}

@end
