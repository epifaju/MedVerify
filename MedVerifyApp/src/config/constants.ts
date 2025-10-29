/**
 * Constantes de configuration de l'application
 */

// Pour appareil physique connecté via USB : utiliser localhost (avec adb reverse)
// Pour appareil physique sur WiFi : utiliser l'IP du PC (192.168.1.16)
// Pour émulateur Android : utiliser 10.0.2.2
export const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const API_TIMEOUT = 10000; // 10 seconds

export const STORAGE_KEYS = {
  ACCESS_TOKEN: '@medverify:access_token',
  REFRESH_TOKEN: '@medverify:refresh_token',
  USER: '@medverify:user',
  LANGUAGE: '@medverify:language',
} as const;

export const REQUEST_TIMEOUT = 30000; // 30 seconds for verification requests

export const COLORS = {
  primary: '#2563EB',
  secondary: '#64748B',
  success: '#10B981',
  warning: '#F59E0B',
  danger: '#EF4444',
  white: '#FFFFFF',
  black: '#000000',
  gray: {
    50: '#F9FAFB',
    100: '#F3F4F6',
    200: '#E5E7EB',
    300: '#D1D5DB',
    400: '#9CA3AF',
    500: '#6B7280',
    600: '#4B5563',
    700: '#374151',
    800: '#1F2937',
    900: '#111827',
  },
} as const;

