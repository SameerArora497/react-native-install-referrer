# react-native-install-referrer

Exposes the [Google Play Install Referrer API](https://developer.android.com/google/play/installreferrer) to React Native via a **Turbo Module**. Retrieve UTM attribution data, click/install timestamps, and instant-experience status directly from Play Store at runtime.

> **iOS** — Not supported by Google Play. All methods resolve with `null` on iOS.

---

## Installation

```sh
npm install react-native-install-referrer
# or
yarn add react-native-install-referrer
```

### Android

No manual linking needed (auto-linked). The library adds `com.android.installreferrer:installreferrer:2.2` to your Android build automatically via `build.gradle`.

Make sure Google Play Store is installed on the test device — the API is backed by a Play Store service.

### iOS

No additional setup needed. All methods return `null` on iOS. Check for `null` before using results.

---

## Usage

```ts
import {
  getInstallReferrer,
  getReferrerDetails,
  parseReferrer,
} from 'react-native-install-referrer';

// Raw referrer string
const referrer = await getInstallReferrer();
// e.g. "utm_source=google&utm_medium=cpc&utm_campaign=launch"

// Full details (Android only)
const details = await getReferrerDetails();
// {
//   referrer: 'utm_source=google&utm_medium=cpc',
//   clickTimestamp: 1700000000000,   // ms since epoch
//   installTimestamp: 1700000010000, // ms since epoch
//   instantExperience: false,
// }

// Parse UTM parameters
const utm = parseReferrer(referrer ?? '');
// { utm_source: 'google', utm_medium: 'cpc', utm_campaign: 'launch' }
```

---

## API Reference

### `getInstallReferrer(): Promise<string | null>`

Returns the raw install referrer string as provided by Google Play.

- **Android** — Resolves with the referrer string (empty string `""` if no referrer was set).
- **iOS** — Resolves with `null`.
- **Throws** — Rejects with a standardized error code (see [Error Codes](#error-codes)) on failure.

Results are **cached** after the first successful call — subsequent calls return immediately without a native round-trip.

---

### `getReferrerDetails(): Promise<ReferrerDetails | null>`

Returns the full referrer details object.

```ts
type ReferrerDetails = {
  referrer: string;
  /** Unix timestamp in milliseconds */
  clickTimestamp: number;
  /** Unix timestamp in milliseconds */
  installTimestamp: number;
  instantExperience: boolean;
};
```

- **Android** — Resolves with the full details.
- **iOS** — Resolves with `null`.
- **Throws** — Rejects with a standardized error code on failure.

Results are **cached** after the first successful call.

---

### `parseReferrer(referrer: string): Record<string, string>`

Parses a URL-encoded referrer string into a plain key/value object. Handles percent-encoding and `+`-as-space correctly.

```ts
parseReferrer('utm_source=google&utm_medium=cpc&utm_campaign=Q4+launch');
// → { utm_source: 'google', utm_medium: 'cpc', utm_campaign: 'Q4 launch' }
```

---

### `clearCache(): void`

Clears the in-memory cache so the next call to `getInstallReferrer` or `getReferrerDetails` fetches fresh data from native. Useful in tests.

---

## Error Codes

When a native call fails, the Promise is rejected with an `Error` whose `code` property is one of:

| Code | Cause |
|---|---|
| `SERVICE_UNAVAILABLE` | Play Store is not installed, or the referrer service is unavailable / disconnected |
| `FEATURE_NOT_SUPPORTED` | The installed Play Store version does not support the Install Referrer API |
| `UNKNOWN_ERROR` | An unexpected response code was returned |

```ts
try {
  const referrer = await getInstallReferrer();
} catch (e) {
  if ((e as any).code === 'SERVICE_UNAVAILABLE') {
    // Handle gracefully — e.g. sideloaded APK, non-Play device
  }
}
```

---

## Android Notes

- The referrer string is set by Google Play at install time and is only available for **90 days** after installation.
- Sideloaded APKs (not installed via Play Store) will receive an empty referrer string.
- The API requires the device to have Google Play Services installed.
- Timestamps returned by the native API are in **seconds**; this library converts them to **milliseconds** for JavaScript consistency.

---

## iOS Limitation

Google Play Install Referrer is an Android-only API. There is no equivalent mechanism on iOS. All methods in this library resolve with `null` on iOS. To attribute iOS installs, use the [SKAdNetwork API](https://developer.apple.com/documentation/storekit/skadnetwork) or a third-party attribution SDK.

---

## Example App

See [example/src/App.tsx](example/src/App.tsx) for a complete working example that:

- Fetches the referrer string and full details on mount
- Displays click/install timestamps as ISO date strings
- Renders parsed UTM parameters in a list

---

## License

MIT
