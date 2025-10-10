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
      const response = await AuthService.login(email, password);
      return response.user;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Login failed');
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
      return rejectWithValue(error.response?.data?.message || 'Registration failed');
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


