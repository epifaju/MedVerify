/**
 * Types pour le dashboard
 */

export interface DashboardStats {
  period: Period;
  kpis: KPIs;
  trends: Trends;
  topCounterfeitMedications: TopCounterfeitMedication[];
  geographicDistribution: GeographicDistribution[];
  recentAlerts: RecentAlert[];
}

export interface Period {
  start: string;
  end: string;
}

export interface KPIs {
  totalScans: number;
  authenticMedications: number;
  suspiciousMedications: number;
  unknownMedications: number;
  authenticityRate: number;
  totalReports: number;
  confirmedCounterfeits: number;
  activeUsers: number;
  newUsers: number;
}

export interface Trends {
  scansGrowth: string;
  reportsGrowth: string;
  usersGrowth: string;
}

export interface TopCounterfeitMedication {
  medicationName: string;
  gtin: string;
  reportCount: number;
  lastReported: string;
}

export interface GeographicDistribution {
  region: string;
  scans: number;
  reports: number;
  suspiciousRate: number;
}

export interface RecentAlert {
  id: string;
  type: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW' | 'CRITICAL';
  message: string;
  medicationName: string;
  timestamp: string;
}

