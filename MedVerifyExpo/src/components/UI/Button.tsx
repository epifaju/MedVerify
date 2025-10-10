/**
 * Composant Button réutilisable
 */

import React from 'react';
import {
  TouchableOpacity,
  Text,
  ActivityIndicator,
  StyleSheet,
  ViewStyle,
  TextStyle,
} from 'react-native';
import { COLORS } from '@config/constants';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'outline' | 'danger';
  size?: 'small' | 'medium' | 'large';
  loading?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
  textStyle?: TextStyle;
}

const Button: React.FC<ButtonProps> = ({
  title,
  onPress,
  variant = 'primary',
  size = 'medium',
  loading = false,
  disabled = false,
  style,
  textStyle,
}) => {
  const getButtonStyle = (): ViewStyle => {
    const baseStyle: ViewStyle = {
      ...styles.button,
      ...styles[`button${size.charAt(0).toUpperCase() + size.slice(1)}` as keyof typeof styles],
    };

    const variantStyles: Record<string, ViewStyle> = {
      primary: { backgroundColor: COLORS.primary },
      secondary: { backgroundColor: COLORS.secondary },
      outline: { backgroundColor: 'transparent', borderWidth: 1, borderColor: COLORS.primary },
      danger: { backgroundColor: COLORS.danger },
    };

    return { ...baseStyle, ...variantStyles[variant] };
  };

  const getTextStyle = (): TextStyle => {
    const baseTextStyle: TextStyle = {
      ...styles.buttonText,
      ...styles[`buttonText${size.charAt(0).toUpperCase() + size.slice(1)}` as keyof typeof styles],
    };

    const variantTextStyle: Record<string, TextStyle> = {
      primary: { color: COLORS.white },
      secondary: { color: COLORS.white },
      outline: { color: COLORS.primary },
      danger: { color: COLORS.white },
    };

    return { ...baseTextStyle, ...variantTextStyle[variant] };
  };

  return (
    <TouchableOpacity
      style={[getButtonStyle(), (disabled || loading) && styles.buttonDisabled, style]}
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.7}>
      {loading ? (
        <ActivityIndicator color={variant === 'outline' ? COLORS.primary : COLORS.white} />
      ) : (
        <Text style={[getTextStyle(), textStyle]}>{title}</Text>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonSmall: {
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  buttonMedium: {
    paddingHorizontal: 24,
    paddingVertical: 12,
  },
  buttonLarge: {
    paddingHorizontal: 32,
    paddingVertical: 16,
  },
  buttonText: {
    fontWeight: '600',
  },
  buttonTextSmall: {
    fontSize: 14,
  },
  buttonTextMedium: {
    fontSize: 16,
  },
  buttonTextLarge: {
    fontSize: 18,
  },
  buttonDisabled: {
    opacity: 0.5,
  },
});

export default Button;

