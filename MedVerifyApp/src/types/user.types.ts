/**
 * Types pour les utilisateurs et l'authentification
 */

export enum UserRole {
  PATIENT = 'PATIENT',
  PHARMACIST = 'PHARMACIST',
  AUTHORITY = 'AUTHORITY',
  ADMIN = 'ADMIN',
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  isVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: UserRole;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface MessageResponse {
  message: string;
  timestamp: string;
}


