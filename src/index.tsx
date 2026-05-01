import NativeInstallReferrer from './NativeInstallReferrer';

export type ReferrerDetails = {
  referrer: string;
  /** Unix timestamp in milliseconds */
  clickTimestamp: number;
  /** Unix timestamp in milliseconds */
  installTimestamp: number;
  instantExperience: boolean;
};

export type InstallReferrerError =
  | 'SERVICE_UNAVAILABLE'
  | 'FEATURE_NOT_SUPPORTED'
  | 'SERVICE_DISCONNECTED'
  | 'UNKNOWN_ERROR';

// Module-level cache — populated on first native call, reused thereafter.
let cachedReferrer: string | null = null;
let cachedDetails: ReferrerDetails | null = null;

/**
 * Returns the raw install referrer string from Google Play.
 * Result is cached after the first successful fetch.
 */
export async function getInstallReferrer(): Promise<string> {
  if (cachedReferrer !== null) {
    return cachedReferrer;
  }
  const result = await NativeInstallReferrer.getInstallReferrer();
  cachedReferrer = result;
  return result;
}

/**
 * Returns full referrer details including timestamps and instant-experience flag.
 * Result is cached after the first successful fetch.
 */
export async function getReferrerDetails(): Promise<ReferrerDetails> {
  if (cachedDetails !== null) {
    return cachedDetails;
  }
  const result = (await NativeInstallReferrer.getReferrerDetails()) as ReferrerDetails;
  cachedDetails = result;
  return result;
}

/**
 * Parses a URL-encoded referrer string (e.g. UTM params) into a key/value map.
 *
 * Example:
 *   parseReferrer('utm_source=google&utm_medium=cpc&utm_campaign=launch')
 *   // → { utm_source: 'google', utm_medium: 'cpc', utm_campaign: 'launch' }
 */
export function parseReferrer(referrer: string): Record<string, string> {
  const params: Record<string, string> = {};
  if (!referrer) return params;

  try {
    for (const pair of referrer.split('&')) {
      const idx = pair.indexOf('=');
      if (idx === -1) continue;
      const key = decodeURIComponent(pair.slice(0, idx).replace(/\+/g, ' '));
      const val = decodeURIComponent(pair.slice(idx + 1).replace(/\+/g, ' '));
      if (key) params[key] = val;
    }
  } catch {
    // Return whatever was successfully parsed
  }

  return params;
}

