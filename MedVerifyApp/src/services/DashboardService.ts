/**
 * Service pour le dashboard
 */

import apiClient from './ApiClient';
import { DashboardStats } from '@types/dashboard.types';

class DashboardService {
  /**
   * Récupérer les statistiques du dashboard
   */
  async getStats(period = '30d'): Promise<DashboardStats> {
    const response = await apiClient.get<DashboardStats>(
      `/admin/dashboard/stats?period=${period}`
    );
    return response.data;
  }
}

export default new DashboardService();

