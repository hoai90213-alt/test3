#import "LauncherViewController.h"
#import "JVMRunner.h"

@interface LauncherViewController ()
@property (nonatomic, strong) UILabel *statusLabel;
@property (nonatomic, strong) UIButton *launchButton;
@property (nonatomic, strong) UITextView *logView;
@property (nonatomic, assign) BOOL launchedOnce;
@end

@implementation LauncherViewController

- (void)viewDidLoad {
	[super viewDidLoad];
	self.title = @"LWJGL Launcher";
	self.view.backgroundColor = [UIColor systemBackgroundColor];

	self.statusLabel = [[UILabel alloc] init];
	self.statusLabel.translatesAutoresizingMaskIntoConstraints = NO;
	self.statusLabel.text = @"Ready";
	self.statusLabel.numberOfLines = 0;
	self.statusLabel.font = [UIFont systemFontOfSize:15 weight:UIFontWeightSemibold];

	self.launchButton = [UIButton buttonWithType:UIButtonTypeSystem];
	self.launchButton.translatesAutoresizingMaskIntoConstraints = NO;
	[self.launchButton setTitle:@"Launch JVM" forState:UIControlStateNormal];
	[self.launchButton addTarget:self action:@selector(startLaunch) forControlEvents:UIControlEventTouchUpInside];

	self.logView = [[UITextView alloc] init];
	self.logView.translatesAutoresizingMaskIntoConstraints = NO;
	self.logView.editable = NO;
	self.logView.font = [UIFont monospacedSystemFontOfSize:12 weight:UIFontWeightRegular];
	self.logView.text = @"Launcher initialized.\n";
	self.logView.backgroundColor = [UIColor secondarySystemBackgroundColor];

	UIStackView *stack = [[UIStackView alloc] initWithArrangedSubviews:@[self.statusLabel, self.launchButton, self.logView]];
	stack.translatesAutoresizingMaskIntoConstraints = NO;
	stack.axis = UILayoutConstraintAxisVertical;
	stack.spacing = 12.0;
	[self.view addSubview:stack];

	[NSLayoutConstraint activateConstraints:@[
		[stack.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:16.0],
		[stack.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16.0],
		[stack.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16.0],
		[stack.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor constant:-16.0],
	]];
}

- (void)viewDidAppear:(BOOL)animated {
	[super viewDidAppear:animated];
	if (!self.launchedOnce) {
		self.launchedOnce = YES;
		[self startLaunch];
	}
}

- (void)appendLog:(NSString *)message {
	NSString *line = [NSString stringWithFormat:@"%@\n", message];
	self.logView.text = [self.logView.text stringByAppendingString:line];
	NSRange bottom = NSMakeRange(self.logView.text.length, 0);
	[self.logView scrollRangeToVisible:bottom];
}

- (void)startLaunch {
	self.launchButton.enabled = NO;
	self.statusLabel.text = @"Launching bundled JVM in interpreter mode (-Xint)...";
	[self appendLog:@"Preparing runtime and filesystem redirection."];

	[JVMRunner launchGameWithCompletion:^(int exitCode, NSString *message) {
		self.launchButton.enabled = YES;
		self.statusLabel.text = [NSString stringWithFormat:@"JVM exited with code %d", exitCode];
		[self appendLog:message];
		[self appendLog:[NSString stringWithFormat:@"Process ended with code %d.", exitCode]];
	}];
}

@end
