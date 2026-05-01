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

No manual linking needed (auto-linked). The library adds `com.android.installreferrer:installreferrer:2.2` to your Android build automatically.

Make sure Google Play Store is installed on the test device — the API is backed by a Play Store service.

### iOS

No additional setup needed. All methods return `null` on iOS. Always null-check before using results.

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

// Parse UTM parameters from the referrer string
const utm = parseReferrer(referrer ?? '');
// { utm_source: 'google', utm_medium: 'cpc', utm_campaign: 'launch' }
```

---

## API Reference

### `getInstallReferrer(): Promise<string | null>`

Returns the raw install referrer string from Google Play.

- **Android** — Resolves with the referrer string. Returns `""` if no referrer was set.
- **iOS** — Resolves with `null`.
- **Throws** — Rejects with a standardized error code on failure (see [Error Codes](#error-codes)).

Result is **cached** after the first successful call — subsequent calls return immediately without a native round-trip.

---

### `getReferrerDetails(): Promise<ReferrerDetails | null>`

Returns full referrer details including timestamps and instant-experience flag.

```ts
type ReferrerDetails = {
  referrer: string;
  clickTimestamp: number;    // Unix timestamp in milliseconds
  installTimestamp: number;  // Unix timestamp in milliseconds
  instantExperience: boolean;
};
```

- **Android** — Resolves with the full details object.
- **iOS** — Resolves with `null`.
- **Throws** — Rejects with a standardized error code on failure.

Result is **cached** after the first successful call.

---

### `parseReferrer(referrer: string): Record<string, string>`

Parses a URL-encoded referrer string into a plain key/value object. Handles percent-encoding and `+`-as-space.

```ts
parseReferrer('utm_source=google&utm_medium=cpc&utm_campaign=Q4+launch');
// → { utm_source: 'google', utm_medium: 'cpc', utm_campaign: 'Q4 launch' }
```

---

## Error Codes

When a native call fails the Promise is rejected. The error `code` will be one of:

| Code | Cause |
|---|---|
| `SERVICE_UNAVAILABLE` | Play Store is not installed, or the referrer service disconnected |
| `FEATURE_NOT_SUPPORTED` | The installed Play Store version does not support the Install Referrer API |
| `UNKNOWN_ERROR` | An unexpected response code was returned |

```ts
try {
  const referrer = await getInstallReferrer();
} catch (e: any) {
  if (e.code === 'SERVICE_UNAVAILABLE') {
    // Sideloaded APK or non-Play device — handle gracefully
  }
}
```

---

## Android Notes

- The referrer string is available for **90 days** after installation.
- Sideloaded APKs (not installed via Play Store) will return an empty referrer string.
- Requires Google Play Store to be installed on the device.
- The native API returns timestamps in **seconds** — this library converts them to **milliseconds** so `new Date(clickTimestamp)` works directly.

---

## iOS Limitation

Google Play Install Referrer is an Android-only API. All methods in this library resolve with `null` on iOS. For iOS install attribution, refer to [SKAdNetwork](https://developer.apple.com/documentation/storekit/skadnetwork) or a third-party attribution SDK.

---

## License

MIT
