package com.installreferrer

import android.os.RemoteException
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import java.util.concurrent.atomic.AtomicBoolean

class InstallReferrerModule(reactContext: ReactApplicationContext) :
  NativeInstallReferrerSpec(reactContext) {

  companion object {
    const val NAME = NativeInstallReferrerSpec.NAME
  }

  /**
   * Builds an InstallReferrerClient connection, guards against double-settlement,
   * and delegates success to [onReady]. All error paths reject [promise] directly.
   */
  private fun connect(promise: Promise, onReady: (InstallReferrerClient) -> Unit) {
    val settled = AtomicBoolean(false)
    val client = InstallReferrerClient.newBuilder(reactApplicationContext).build()

    client.startConnection(object : InstallReferrerStateListener {

      override fun onInstallReferrerSetupFinished(responseCode: Int) {
        if (!settled.compareAndSet(false, true)) return

        when (responseCode) {
          InstallReferrerClient.InstallReferrerResponse.OK -> onReady(client)

          InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
            client.endConnection()
            promise.reject(
              "FEATURE_NOT_SUPPORTED",
              "Play Store does not support the Install Referrer API on this device"
            )
          }

          InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
            client.endConnection()
            promise.reject(
              "SERVICE_UNAVAILABLE",
              "Install Referrer service is unavailable — ensure Play Store is installed"
            )
          }

          else -> {
            client.endConnection()
            promise.reject("UNKNOWN_ERROR", "Unexpected response code: $responseCode")
          }
        }
      }

      override fun onInstallReferrerServiceDisconnected() {
        if (!settled.compareAndSet(false, true)) return
        promise.reject(
          "SERVICE_UNAVAILABLE",
          "Install Referrer service disconnected before the response was received"
        )
      }
    })
  }

  override fun getInstallReferrer(promise: Promise) {
    connect(promise) { client ->
      try {
        val referrer = client.installReferrer.installReferrer
        promise.resolve(referrer)
      } catch (e: RemoteException) {
        promise.reject("SERVICE_UNAVAILABLE", e.message ?: "RemoteException reading referrer")
      } finally {
        client.endConnection()
      }
    }
  }

  override fun getReferrerDetails(promise: Promise) {
    connect(promise) { client ->
      try {
        val ref = client.installReferrer
        val map = WritableNativeMap().apply {
          putString("referrer", ref.installReferrer)
          // API returns seconds; convert to milliseconds for JavaScript
          putDouble("clickTimestamp", ref.referrerClickTimestampSeconds * 1_000.0)
          putDouble("installTimestamp", ref.installBeginTimestampSeconds * 1_000.0)
          putBoolean("instantExperience", ref.googlePlayInstantParam)
        }
        promise.resolve(map)
      } catch (e: RemoteException) {
        promise.reject("SERVICE_UNAVAILABLE", e.message ?: "RemoteException reading referrer details")
      } finally {
        client.endConnection()
      }
    }
  }
}
