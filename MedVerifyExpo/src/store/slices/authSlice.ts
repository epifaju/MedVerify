/**
 * Redux Slice pour l'authentification
 */

import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import AuthService from '@services/AuthService';
import { User, RegisterRequest } from '@types/user.types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

// Async Thunks
export const loginAsync = createAsyncThunk(
  'auth/login',
  async ({ email, password }: { email: string; password: string }, { rejectWithValue }) => {
    try {
      console.log('🔐 Tentative de connexion avec:', email);
      const response = await AuthService.login(email, password);
      return response.user;
    } catch (error: any) {
      console.error('❌ Erreur de connexion:', error);
      
      // Gestion spécifique des erreurs réseau améliorées
      if (error.isNetworkError || error.code === 'NO_NETWORK') {
        return rejectWithValue(
          error.message || 
          'Aucune connexion réseau disponible. Vérifiez votre connexion internet.'
        );
      }
      
      // Erreurs réseau classiques
      if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error') || error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
        // Utiliser le message amélioré de l'ApiClient si disponible
        if (error.message && error.message.includes('Impossible de se connecter au serveur')) {
          return rejectWithValue(error.message);
        }
        return rejectWithValue(
          'Impossible de se connecter au serveur. Vérifiez que:\n' +
          '• Le backend est démarré et accessible\n' +
          '• Votre téléphone et votre PC sont sur le même réseau WiFi\n' +
          '• Pour USB: exécutez "adb reverse tcp:8080 tcp:8080"\n' +
          '• Attendez quelques secondes et réessayez'
        );
      }
      
      // Erreur serveur avec réponse
      if (error.response?.data?.message) {
        return rejectWithValue(error.response.data.message);
      }
      
      // Erreur d'authentification
      if (error.response?.status === 401) {
        return rejectWithValue('Email ou mot de passe incorrect.');
      }
      
      // Erreur générique avec message d'origine si disponible
      return rejectWithValue(
        error.message || 'Erreur de connexion. Veuillez réessayer.'
      );
    }
  }
);

export const registerAsync = createAsyncThunk(
  'auth/register',
  async (data: RegisterRequest, { rejectWithValue }) => {
    try {
      const response = await AuthService.register(data);
      return response.message;
    } catch (error: any) {
      // Gestion spécifique des erreurs réseau améliorées
      if (error.isNetworkError || error.code === 'NO_NETWORK') {
        return rejectWithValue(
          error.message || 
          'Aucune connexion réseau disponible. Vérifiez votre connexion internet.'
        );
      }
      
      // Erreurs réseau classiques
      if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error') || error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
        // Utiliser le message amélioré de l'ApiClient si disponible
        if (error.message && error.message.includes('Impossible de se connecter au serveur')) {
          return rejectWithValue(error.message);
        }
        return rejectWithValue(
          'Impossible de se connecter au serveur. Vérifiez que:\n' +
          '• Le backend est démarré et accessible\n' +
          '• Votre téléphone et votre PC sont sur le même réseau WiFi\n' +
          '• Pour USB: exécutez "adb reverse tcp:8080 tcp:8080"\n' +
          '• Attendez quelques secondes et réessayez'
        );
      }
      
      // Erreur serveur avec réponse
      if (error.response?.data?.message) {
        return rejectWithValue(error.response.data.message);
      }
      
      // Erreur générique avec message d'origine si disponible
      return rejectWithValue(
        error.message || 'Erreur lors de l\'inscription. Veuillez réessayer.'
      );
    }
  }
);

export const logoutAsync = createAsyncThunk('auth/logout', async () => {
  await AuthService.logout();
});

export const checkAuthAsync = createAsyncThunk('auth/checkAuth', async () => {
  const user = await AuthService.getCurrentUser();
  const isAuthenticated = await AuthService.isAuthenticated();
  return { user, isAuthenticated };
});

// Slice
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload;
      state.isAuthenticated = true;
    },
    setAuthenticated: (state, action: PayloadAction<boolean>) => {
      state.isAuthenticated = action.payload;
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
    },
    clearError: state => {
      state.error = null;
    },
  },
  extraReducers: builder => {
    // Login
    builder.addCase(loginAsync.pending, state => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(loginAsync.fulfilled, (state, action) => {
      state.isLoading = false;
      state.user = action.payload;
      state.isAuthenticated = true;
      state.error = null;
    });
    builder.addCase(loginAsync.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.payload as string;
      state.isAuthenticated = false;
    });

    // Register
    builder.addCase(registerAsync.pending, state => {
      state.isLoading = true;
      state.error = null;
    });
    builder.addCase(registerAsync.fulfilled, state => {
      state.isLoading = false;
      state.error = null;
    });
    builder.addCase(registerAsync.rejected, (state, action) => {
      state.isLoading = false;
      state.error = action.payload as string;
    });

    // Logout
    builder.addCase(logoutAsync.fulfilled, state => {
      state.user = null;
      state.isAuthenticated = false;
      state.error = null;
    });

    // Check Auth
    builder.addCase(checkAuthAsync.fulfilled, (state, action) => {
      state.user = action.payload.user;
      state.isAuthenticated = action.payload.isAuthenticated;
    });
  },
});

export const { setUser, setAuthenticated, setError, clearError } = authSlice.actions;
export default authSlice.reducer;

