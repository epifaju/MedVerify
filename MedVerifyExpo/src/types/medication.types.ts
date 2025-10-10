/**
 * Types pour les médicaments et vérifications
 */

export enum VerificationStatus {
  AUTHENTIC = 'AUTHENTIC',
  SUSPICIOUS = 'SUSPICIOUS',
  UNKNOWN = 'UNKNOWN',
}

export interface Medication {
  id: string;
  gtin: string;
  name: string;
  genericName?: string;
  manufacturer?: string;
  dosage?: string;
  pharmaceuticalForm?: string;
  indications?: string[];
  sideEffects?: string[];
  contraindications?: string[];
  posology?: PosologyInfo;
  isEssential: boolean;
  imageUrl?: string;
}

export interface PosologyInfo {
  adult?: string;
  child?: string;
  maxDailyDose?: number;
  unit?: string;
  frequency?: string;
}

export interface VerificationRequest {
  gtin: string;
  serialNumber?: string;
  batchNumber?: string;
  expiryDate?: string;
  location?: LocationData;
  deviceInfo?: DeviceInfo;
}

export interface LocationData {
  latitude: number;
  longitude: number;
}

export interface DeviceInfo {
  platform: string;
  osVersion: string;
  appVersion: string;
  deviceModel: string;
}

export interface VerificationResponse {
  verificationId: string;
  status: VerificationStatus;
  confidence: number;
  medication?: Medication;
  alerts?: Alert[];
  verificationSource: string;
  verifiedAt: string;
}

export interface Alert {
  severity: 'HIGH' | 'MEDIUM' | 'LOW' | 'CRITICAL';
  code: string;
  message: string;
}

