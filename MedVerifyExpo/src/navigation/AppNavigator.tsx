/**
 * Navigateur principal de l'application
 * Gère la navigation entre auth et main flows
 */

import React, { useEffect, useState } from 'react';
import { ActivityIndicator, View, StyleSheet } from 'react-native';
import { useAppSelector, useAppDispatch } from '@store/store';
import { checkAuthAsync } from '@store/slices/authSlice';
import AuthNavigator from './AuthNavigator';
import MainNavigator from './MainNavigator';
import { COLORS } from '@config/constants';

const AppNavigator: React.FC = () => {
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAppSelector(state => state.auth);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await dispatch(checkAuthAsync()).unwrap();
      } catch (error) {
        console.error('Failed to check auth status:', error);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, [dispatch]);

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={COLORS.primary} />
      </View>
    );
  }

  return isAuthenticated ? <MainNavigator /> : <AuthNavigator />;
};

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: COLORS.white,
  },
});

export default AppNavigator;

