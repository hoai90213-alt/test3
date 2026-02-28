#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^JVMCompletion)(int exitCode, NSString *message);

@interface JVMRunner : NSObject
+ (void)launchGameWithCompletion:(JVMCompletion)completion;
@end

NS_ASSUME_NONNULL_END
