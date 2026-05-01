#import <InstallReferrerSpec/InstallReferrerSpec.h>

// iOS does not have access to Google Play Install Referrer API.
// This implementation is a no-op stub that resolves all promises with nil.
@interface InstallReferrer : NSObject <NativeInstallReferrerSpec>

@end
