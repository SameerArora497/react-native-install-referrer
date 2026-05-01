package com.installreferrer

import com.facebook.react.bridge.ReactApplicationContext

class InstallReferrerModule(reactContext: ReactApplicationContext) :
  NativeInstallReferrerSpec(reactContext) {

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = NativeInstallReferrerSpec.NAME
  }
}
