/**
 * Types pour les signalements
 */

export enum ReportType {
  COUNTERFEIT = 'COUNTERFEIT',
  QUALITY_ISSUE = 'QUALITY_ISSUE',
  EXPIRED = 'EXPIRED',
  PACKAGING_DEFECT = 'PACKAGING_DEFECT',
  ADVERSE_REACTION = 'ADVERSE_REACTION',
  OTHER = 'OTHER',
}

export enum ReportStatus {
  SUBMITTED = 'SUBMITTED',
  UNDER_REVIEW = 'UNDER_REVIEW',
  CONFIRMED = 'CONFIRMED',
  REJECTED = 'REJECTED',
  CLOSED = 'CLOSED',
}

export interface ReportRequest {
  medicationId?: string;
  gtin?: string;
  serialNumber?: string;
  reportType: ReportType;
  description: string;
  purchaseLocation?: PurchaseLocation;
  photoUrls?: string[];
  anonymous?: boolean;
}

export interface PurchaseLocation {
  name?: string;
  address?: string;
  city?: string;
  region?: string;
  latitude?: number;
  longitude?: number;
}

export interface ReportResponse {
  reportId: string;
  status: ReportStatus;
  referenceNumber: string;
  message: string;
  estimatedProcessingTime: string;
  createdAt: string;
}

export interface ReportDetails {
  id: string;
  referenceNumber: string;
  reportType: ReportType;
  description: string;
  status: ReportStatus;
  medicationName?: string;
  gtin?: string;
  reporterName?: string;
  anonymous: boolean;
  createdAt: string;
  reviewedAt?: string;
  reviewNotes?: string;
}

