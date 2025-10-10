/**
 * Composant Input réutilisable
 */

import React, { useState } from 'react';
import {
  TextInput,
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInputProps,
} from 'react-native';
import { COLORS } from '@config/constants';

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
  secureTextEntry?: boolean;
  showPasswordToggle?: boolean;
}

const Input: React.FC<InputProps> = ({
  label,
  error,
  secureTextEntry,
  showPasswordToggle,
  ...props
}) => {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const togglePasswordVisibility = () => {
    setIsPasswordVisible(!isPasswordVisible);
  };

  return (
    <View style={styles.container}>
      {label && <Text style={styles.label}>{label}</Text>}
      <View style={styles.inputContainer}>
        <TextInput
          style={[styles.input, error && styles.inputError]}
          placeholderTextColor={COLORS.gray[400]}
          secureTextEntry={secureTextEntry && !isPasswordVisible}
          autoCapitalize="none"
          {...props}
        />
        {showPasswordToggle && secureTextEntry && (
          <TouchableOpacity onPress={togglePasswordVisibility} style={styles.iconButton}>
            <Text style={styles.toggleText}>{isPasswordVisible ? '👁️' : '👁️‍🗨️'}</Text>
          </TouchableOpacity>
        )}
      </View>
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: COLORS.gray[700],
    marginBottom: 8,
  },
  inputContainer: {
    position: 'relative',
  },
  input: {
    borderWidth: 1,
    borderColor: COLORS.gray[300],
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 16,
    backgroundColor: COLORS.white,
    color: COLORS.gray[900],
  },
  inputError: {
    borderColor: COLORS.danger,
  },
  errorText: {
    color: COLORS.danger,
    fontSize: 12,
    marginTop: 4,
  },
  iconButton: {
    position: 'absolute',
    right: 12,
    top: 12,
  },
  toggleText: {
    fontSize: 20,
  },
});

export default Input;

