/**
 * Types pour le scan de codes
 */

export interface GS1Data {
  gtin: string;
  serialNumber?: string;
  batchNumber?: string;
  expiryDate?: string;
  raw: string;
}

export interface ScanResult {
  success: boolean;
  data?: GS1Data;
  error?: string;
}

