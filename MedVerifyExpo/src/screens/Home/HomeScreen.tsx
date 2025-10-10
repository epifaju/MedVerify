/**
 * Écran d'accueil principal
 */

import React from 'react';
import { View, Text, StyleSheet, SafeAreaView } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useAppSelector, useAppDispatch } from '@store/store';
import { logoutAsync } from '@store/slices/authSlice';
import Button from '@components/UI/Button';
import { COLORS } from '@config/constants';

interface HomeScreenProps {
  navigation: any;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();
  const { user } = useAppSelector(state => state.auth);

  const handleLogout = async () => {
    await dispatch(logoutAsync());
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>MedVerify</Text>
        <Text style={styles.welcome}>
          Welcome, {user?.firstName} {user?.lastName}!
        </Text>
        <Text style={styles.role}>Role: {user?.role}</Text>

        <Button
          title="Scan Medication"
          onPress={() => {}}
          style={styles.button}
        />

        <Button
          title={t('auth.logout')}
          onPress={handleLogout}
          variant="outline"
          style={styles.button}
        />
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.white,
  },
  content: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: COLORS.primary,
    textAlign: 'center',
    marginBottom: 24,
  },
  welcome: {
    fontSize: 18,
    color: COLORS.gray[700],
    textAlign: 'center',
    marginBottom: 8,
  },
  role: {
    fontSize: 14,
    color: COLORS.gray[500],
    textAlign: 'center',
    marginBottom: 48,
  },
  button: {
    marginBottom: 16,
  },
});

export default HomeScreen;

