/**
 * Service d'authentification
 */

import AsyncStorage from '@react-native-async-storage/async-storage';
import apiClient from './ApiClient';
import { STORAGE_KEYS } from '@config/constants';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
  MessageResponse,
} from '@types/user.types';

class AuthService {
  /**
   * Login utilisateur
   */
  async login(email: string, password: string): Promise<AuthResponse> {
    const request: LoginRequest = { email, password };
    const response = await apiClient.post<AuthResponse>('/auth/login', request);
    
    const { accessToken, refreshToken, user } = response.data;

    // Stocker les tokens et les infos utilisateur
    await AsyncStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
    await AsyncStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
    await AsyncStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));

    return response.data;
  }

  /**
   * Register nouvel utilisateur
   */
  async register(data: RegisterRequest): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/register', data);
    return response.data;
  }

  /**
   * Logout utilisateur
   */
  async logout(): Promise<void> {
    await AsyncStorage.multiRemove([
      STORAGE_KEYS.ACCESS_TOKEN,
      STORAGE_KEYS.REFRESH_TOKEN,
      STORAGE_KEYS.USER,
    ]);
  }

  /**
   * Refresh le token
   */
  async refreshToken(): Promise<AuthResponse> {
    const refreshToken = await AsyncStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
    
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await apiClient.post<AuthResponse>('/auth/refresh', {
      refreshToken,
    });

    const { accessToken, refreshToken: newRefreshToken } = response.data;

    await AsyncStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
    await AsyncStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, newRefreshToken);

    return response.data;
  }

  /**
   * Récupère l'utilisateur courant depuis le stockage
   */
  async getCurrentUser(): Promise<User | null> {
    const userJson = await AsyncStorage.getItem(STORAGE_KEYS.USER);
    return userJson ? JSON.parse(userJson) : null;
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   */
  async isAuthenticated(): Promise<boolean> {
    const accessToken = await AsyncStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    return !!accessToken;
  }
}

export default new AuthService();

