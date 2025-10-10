/**
 * Service pour les signalements
 */

import apiClient from './ApiClient';
import { ReportRequest, ReportResponse, ReportDetails, ReportStatus } from '@types/report.types';

class ReportService {
  /**
   * Créer un signalement
   */
  async createReport(data: ReportRequest): Promise<ReportResponse> {
    const response = await apiClient.post<ReportResponse>('/reports', data);
    return response.data;
  }

  /**
   * Récupérer mes signalements
   */
  async getMyReports(): Promise<ReportDetails[]> {
    const response = await apiClient.get<ReportDetails[]>('/reports/my-reports');
    return response.data;
  }

  /**
   * Récupérer un signalement par ID
   */
  async getReportById(id: string): Promise<ReportDetails> {
    const response = await apiClient.get<ReportDetails>(`/reports/${id}`);
    return response.data;
  }

  /**
   * Récupérer tous les signalements (autorités)
   */
  async getAllReports(status?: ReportStatus, page = 0, size = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    
    if (status) {
      params.append('status', status);
    }

    const response = await apiClient.get(`/reports?${params.toString()}`);
    return response.data;
  }

  /**
   * Mettre à jour le statut d'un signalement (autorités)
   */
  async updateReportStatus(
    id: string,
    status: ReportStatus,
    notes?: string
  ): Promise<ReportDetails> {
    const params = new URLSearchParams({ status });
    if (notes) {
      params.append('notes', notes);
    }

    const response = await apiClient.put<ReportDetails>(
      `/admin/reports/${id}/review?${params.toString()}`
    );
    return response.data;
  }
}

export default new ReportService();

