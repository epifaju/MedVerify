/**
 * Service pour la vérification de médicaments
 */

import apiClient from './ApiClient';
import { VerificationRequest, VerificationResponse } from '@types/medication.types';
import { getCachedMedication, cacheMedication } from '@database/schema';

class ScanService {
  /**
   * Vérifie un médicament scanné
   */
  async verifyScan(request: VerificationRequest): Promise<VerificationResponse> {
    try {
      // 1. Vérifier le cache local d'abord
      const cachedResult = await this.checkCache(request.gtin);
      if (cachedResult) {
        console.log('Medication found in local cache');
        return cachedResult;
      }

      // 2. Appeler l'API backend
      console.log('Verifying medication via API:', request.gtin);
      const response = await apiClient.post<VerificationResponse>(
        '/medications/verify',
        request
      );

      // 3. Mettre en cache si authentique
      if (response.data.status === 'AUTHENTIC' && response.data.medication) {
        await this.cacheResult(response.data);
      }

      return response.data;
    } catch (error: any) {
      console.error('Error verifying medication:', error);
      throw new Error(
        error.response?.data?.message || 'Failed to verify medication'
      );
    }
  }

  /**
   * Vérifie le cache local
   */
  private async checkCache(gtin: string): Promise<VerificationResponse | null> {
    try {
      const cached = await getCachedMedication(gtin);
      if (!cached) {
        return null;
      }

      // Retourner les données depuis le cache
      return {
        ...cached.data,
        verificationSource: 'CACHE_LOCAL',
      };
    } catch (error) {
      console.error('Error checking cache:', error);
      return null;
    }
  }

  /**
   * Met en cache le résultat de vérification
   */
  private async cacheResult(response: VerificationResponse): Promise<void> {
    try {
      if (!response.medication) {
        return;
      }

      await cacheMedication(
        response.medication.gtin,
        response.medication.name,
        response.medication.manufacturer || '',
        response.medication.dosage || '',
        response.status === 'AUTHENTIC',
        response,
        24 * 60 * 60 * 1000 // 24h TTL
      );
    } catch (error) {
      console.error('Error caching result:', error);
    }
  }

  /**
   * Récupère les médicaments essentiels
   */
  async getEssentialMedications() {
    try {
      const response = await apiClient.get('/medications/essential');
      return response.data;
    } catch (error) {
      console.error('Error fetching essential medications:', error);
      throw error;
    }
  }

  /**
   * Recherche de médicaments par nom
   */
  async searchMedications(name: string) {
    try {
      const response = await apiClient.get(`/medications/search?name=${encodeURIComponent(name)}`);
      return response.data;
    } catch (error) {
      console.error('Error searching medications:', error);
      throw error;
    }
  }
}

export default new ScanService();


