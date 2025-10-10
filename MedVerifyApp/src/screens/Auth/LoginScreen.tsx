/**
 * Écran de connexion
 */

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  TouchableOpacity,
} from 'react-native';
import { useTranslation } from 'react-i18next';
import { useAppDispatch, useAppSelector } from '@store/store';
import { loginAsync, clearError } from '@store/slices/authSlice';
import Button from '@components/UI/Button';
import Input from '@components/UI/Input';
import { COLORS } from '@config/constants';
import Toast from 'react-native-toast-message';

interface LoginScreenProps {
  navigation: any;
}

const LoginScreen: React.FC<LoginScreenProps> = ({ navigation }) => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();
  const { isLoading, error } = useAppSelector(state => state.auth);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});

  useEffect(() => {
    if (error) {
      Toast.show({
        type: 'error',
        text1: t('common.error'),
        text2: error,
      });
    }
  }, [error, t]);

  useEffect(() => {
    return () => {
      dispatch(clearError());
    };
  }, [dispatch]);

  const validateForm = (): boolean => {
    const newErrors: { email?: string; password?: string } = {};

    // Validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
      newErrors.email = t('auth.requiredField');
    } else if (!emailRegex.test(email)) {
      newErrors.email = t('auth.invalidEmail');
    }

    // Validate password
    if (!password) {
      newErrors.password = t('auth.requiredField');
    } else if (password.length < 8) {
      newErrors.password = t('auth.passwordTooShort');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async () => {
    if (!validateForm()) {
      return;
    }

    try {
      await dispatch(loginAsync({ email, password })).unwrap();
      Toast.show({
        type: 'success',
        text1: t('auth.loginSuccess'),
      });
    } catch (err) {
      // Error already handled by Redux
    }
  };

  const handleNavigateToRegister = () => {
    navigation.navigate('Register');
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled">
          <View style={styles.content}>
            <Text style={styles.title}>MedVerify</Text>
            <Text style={styles.subtitle}>{t('auth.login')}</Text>

            <Input
              label={t('auth.email')}
              placeholder={t('auth.email')}
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              error={errors.email}
              editable={!isLoading}
            />

            <Input
              label={t('auth.password')}
              placeholder={t('auth.password')}
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              showPasswordToggle
              error={errors.password}
              editable={!isLoading}
            />

            <Button
              title={t('auth.signIn')}
              onPress={handleLogin}
              loading={isLoading}
              style={styles.loginButton}
            />

            <TouchableOpacity onPress={handleNavigateToRegister} disabled={isLoading}>
              <Text style={styles.registerText}>
                {t('auth.noAccount')} <Text style={styles.registerLink}>{t('auth.signUp')}</Text>
              </Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.white,
  },
  keyboardView: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
  },
  content: {
    padding: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: COLORS.primary,
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 18,
    color: COLORS.gray[600],
    textAlign: 'center',
    marginBottom: 32,
  },
  loginButton: {
    marginTop: 8,
  },
  registerText: {
    textAlign: 'center',
    marginTop: 24,
    fontSize: 14,
    color: COLORS.gray[600],
  },
  registerLink: {
    color: COLORS.primary,
    fontWeight: '600',
  },
});

export default LoginScreen;


