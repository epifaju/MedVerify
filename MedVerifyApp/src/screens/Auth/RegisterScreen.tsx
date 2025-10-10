/**
 * Écran d'inscription
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
import { registerAsync, clearError } from '@store/slices/authSlice';
import Button from '@components/UI/Button';
import Input from '@components/UI/Input';
import { COLORS } from '@config/constants';
import { UserRole } from '@types/user.types';
import Toast from 'react-native-toast-message';

interface RegisterScreenProps {
  navigation: any;
}

const RegisterScreen: React.FC<RegisterScreenProps> = ({ navigation }) => {
  const { t } = useTranslation();
  const dispatch = useAppDispatch();
  const { isLoading, error } = useAppSelector(state => state.auth);

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    phone: '',
    role: UserRole.PATIENT,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

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

  const updateField = (field: string, value: string | UserRole) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email) {
      newErrors.email = t('auth.requiredField');
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = t('auth.invalidEmail');
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = t('auth.requiredField');
    } else if (formData.password.length < 8) {
      newErrors.password = t('auth.passwordTooShort');
    }

    // Confirm password
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = t('auth.passwordsDontMatch');
    }

    // First name
    if (!formData.firstName) {
      newErrors.firstName = t('auth.requiredField');
    }

    // Last name
    if (!formData.lastName) {
      newErrors.lastName = t('auth.requiredField');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async () => {
    if (!validateForm()) {
      return;
    }

    try {
      await dispatch(
        registerAsync({
          email: formData.email,
          password: formData.password,
          firstName: formData.firstName,
          lastName: formData.lastName,
          phone: formData.phone || undefined,
          role: formData.role,
        })
      ).unwrap();

      Toast.show({
        type: 'success',
        text1: t('auth.registerSuccess'),
      });

      // Navigate to login after successful registration
      setTimeout(() => {
        navigation.navigate('Login');
      }, 2000);
    } catch (err) {
      // Error already handled by Redux
    }
  };

  const handleNavigateToLogin = () => {
    navigation.navigate('Login');
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
            <Text style={styles.subtitle}>{t('auth.register')}</Text>

            <Input
              label={t('auth.firstName')}
              placeholder={t('auth.firstName')}
              value={formData.firstName}
              onChangeText={value => updateField('firstName', value)}
              error={errors.firstName}
              editable={!isLoading}
            />

            <Input
              label={t('auth.lastName')}
              placeholder={t('auth.lastName')}
              value={formData.lastName}
              onChangeText={value => updateField('lastName', value)}
              error={errors.lastName}
              editable={!isLoading}
            />

            <Input
              label={t('auth.email')}
              placeholder={t('auth.email')}
              value={formData.email}
              onChangeText={value => updateField('email', value)}
              keyboardType="email-address"
              autoCapitalize="none"
              error={errors.email}
              editable={!isLoading}
            />

            <Input
              label={t('auth.phone')}
              placeholder={t('auth.phone')}
              value={formData.phone}
              onChangeText={value => updateField('phone', value)}
              keyboardType="phone-pad"
              editable={!isLoading}
            />

            <Input
              label={t('auth.password')}
              placeholder={t('auth.password')}
              value={formData.password}
              onChangeText={value => updateField('password', value)}
              secureTextEntry
              showPasswordToggle
              error={errors.password}
              editable={!isLoading}
            />

            <Input
              label={t('auth.confirmPassword')}
              placeholder={t('auth.confirmPassword')}
              value={formData.confirmPassword}
              onChangeText={value => updateField('confirmPassword', value)}
              secureTextEntry
              showPasswordToggle
              error={errors.confirmPassword}
              editable={!isLoading}
            />

            <View style={styles.roleContainer}>
              <Text style={styles.roleLabel}>{t('auth.role')}</Text>
              <View style={styles.roleButtons}>
                <TouchableOpacity
                  style={[
                    styles.roleButton,
                    formData.role === UserRole.PATIENT && styles.roleButtonActive,
                  ]}
                  onPress={() => updateField('role', UserRole.PATIENT)}
                  disabled={isLoading}>
                  <Text
                    style={[
                      styles.roleButtonText,
                      formData.role === UserRole.PATIENT && styles.roleButtonTextActive,
                    ]}>
                    {t('auth.patient')}
                  </Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.roleButton,
                    formData.role === UserRole.PHARMACIST && styles.roleButtonActive,
                  ]}
                  onPress={() => updateField('role', UserRole.PHARMACIST)}
                  disabled={isLoading}>
                  <Text
                    style={[
                      styles.roleButtonText,
                      formData.role === UserRole.PHARMACIST && styles.roleButtonTextActive,
                    ]}>
                    {t('auth.pharmacist')}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>

            <Button
              title={t('auth.signUp')}
              onPress={handleRegister}
              loading={isLoading}
              style={styles.registerButton}
            />

            <TouchableOpacity onPress={handleNavigateToLogin} disabled={isLoading}>
              <Text style={styles.loginText}>
                {t('auth.hasAccount')} <Text style={styles.loginLink}>{t('auth.signIn')}</Text>
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
    marginTop: 20,
  },
  subtitle: {
    fontSize: 18,
    color: COLORS.gray[600],
    textAlign: 'center',
    marginBottom: 32,
  },
  roleContainer: {
    marginBottom: 24,
  },
  roleLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: COLORS.gray[700],
    marginBottom: 8,
  },
  roleButtons: {
    flexDirection: 'row',
    gap: 12,
  },
  roleButton: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: COLORS.gray[300],
    backgroundColor: COLORS.white,
    alignItems: 'center',
  },
  roleButtonActive: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  roleButtonText: {
    fontSize: 14,
    fontWeight: '500',
    color: COLORS.gray[700],
  },
  roleButtonTextActive: {
    color: COLORS.white,
  },
  registerButton: {
    marginTop: 8,
  },
  loginText: {
    textAlign: 'center',
    marginTop: 24,
    fontSize: 14,
    color: COLORS.gray[600],
    marginBottom: 20,
  },
  loginLink: {
    color: COLORS.primary,
    fontWeight: '600',
  },
});

export default RegisterScreen;


