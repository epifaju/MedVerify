/**
 * Composant KPI Card pour le dashboard
 */

import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS } from '@config/constants';

interface KPICardProps {
  title: string;
  value: string | number;
  trend?: string;
  icon?: string;
  color?: string;
}

const KPICard: React.FC<KPICardProps> = ({
  title,
  value,
  trend,
  icon,
  color = COLORS.primary,
}) => {
  const getTrendColor = () => {
    if (!trend) return COLORS.gray[500];
    return trend.startsWith('+') ? COLORS.success : COLORS.danger;
  };

  return (
    <View style={styles.card}>
      <View style={styles.iconContainer}>
        {icon && <Text style={styles.icon}>{icon}</Text>}
      </View>
      <Text style={styles.title}>{title}</Text>
      <Text style={[styles.value, { color }]}>{value}</Text>
      {trend && (
        <Text style={[styles.trend, { color: getTrendColor() }]}>
          {trend}
        </Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: COLORS.white,
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: COLORS.gray[200],
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  iconContainer: {
    marginBottom: 8,
  },
  icon: {
    fontSize: 24,
  },
  title: {
    fontSize: 13,
    color: COLORS.gray[600],
    marginBottom: 8,
    fontWeight: '500',
  },
  value: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  trend: {
    fontSize: 12,
    fontWeight: '600',
  },
});

export default KPICard;

