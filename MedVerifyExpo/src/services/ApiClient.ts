/**
 * Client API Axios avec gestion JWT
 */

import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_BASE_URL, API_TIMEOUT, STORAGE_KEYS } from '@config/constants';
import { AuthResponse } from '@types/user.types';

class ApiClient {
  private axiosInstance: AxiosInstance;
  private isRefreshing = false;
  private failedQueue: Array<{
    resolve: (value?: unknown) => void;
    reject: (reason?: unknown) => void;
  }> = [];

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: API_BASE_URL,
      timeout: API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - Add JWT token
    this.axiosInstance.interceptors.request.use(
      async (config: InternalAxiosRequestConfig) => {
        const token = await AsyncStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
        
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        return config;
      },
      (error: AxiosError) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor - Handle token refresh
    this.axiosInstance.interceptors.response.use(
      response => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // If 401 and not already retrying, attempt token refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
          if (this.isRefreshing) {
            // Wait for token refresh to complete
            return new Promise((resolve, reject) => {
              this.failedQueue.push({ resolve, reject });
            })
              .then(() => {
                return this.axiosInstance(originalRequest);
              })
              .catch(err => {
                return Promise.reject(err);
              });
          }

          originalRequest._retry = true;
          this.isRefreshing = true;

          try {
            const refreshToken = await AsyncStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
            
            if (!refreshToken) {
              throw new Error('No refresh token available');
            }

            const response = await axios.post<AuthResponse>(
              `${API_BASE_URL}/auth/refresh`,
              { refreshToken }
            );

            const { accessToken, refreshToken: newRefreshToken } = response.data;

            await AsyncStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
            await AsyncStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, newRefreshToken);

            // Retry all queued requests
            this.failedQueue.forEach(({ resolve }) => resolve());
            this.failedQueue = [];

            return this.axiosInstance(originalRequest);
          } catch (refreshError) {
            // Refresh failed - logout user
            this.failedQueue.forEach(({ reject }) => reject(refreshError));
            this.failedQueue = [];
            
            await this.clearAuthData();
            
            return Promise.reject(refreshError);
          } finally {
            this.isRefreshing = false;
          }
        }

        return Promise.reject(error);
      }
    );
  }

  private async clearAuthData() {
    await AsyncStorage.multiRemove([
      STORAGE_KEYS.ACCESS_TOKEN,
      STORAGE_KEYS.REFRESH_TOKEN,
      STORAGE_KEYS.USER,
    ]);
  }

  public getInstance(): AxiosInstance {
    return this.axiosInstance;
  }
}

export const apiClient = new ApiClient().getInstance();
export default apiClient;

