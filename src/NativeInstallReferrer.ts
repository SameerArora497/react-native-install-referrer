import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface ReferrerDetailsNative {
  referrer: string;
  clickTimestamp: number;
  installTimestamp: number;
  instantExperience: boolean;
}

export interface Spec extends TurboModule {
  getInstallReferrer(): Promise<string>;
  getReferrerDetails(): Promise<Object>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('InstallReferrer');
