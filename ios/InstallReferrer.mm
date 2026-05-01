#import "InstallReferrer.h"

// iOS does not support the Google Play Install Referrer API.
// All methods resolve with nil — check for null in JS before using the result.

@implementation InstallReferrer

+ (NSString *)moduleName
{
  return @"InstallReferrer";
}

- (void)getInstallReferrer:(RCTPromiseResolveBlock)resolve
                    reject:(RCTPromiseRejectBlock)reject
{
  resolve(nil);
}

- (void)getReferrerDetails:(RCTPromiseResolveBlock)resolve
                    reject:(RCTPromiseRejectBlock)reject
{
  resolve(nil);
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativeInstallReferrerSpecJSI>(params);
}

@end
