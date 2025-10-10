/**
 * Dashboard pour les autorités sanitaires
 */

import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  ActivityIndicator,
  TouchableOpacity,
} from 'react-native';
import { useTranslation } from 'react-i18next';
import DashboardService from '@services/DashboardService';
import { DashboardStats } from '@types/dashboard.types';
import KPICard from '@components/Dashboard/KPICard';
import { COLORS } from '@config/constants';
import Toast from 'react-native-toast-message';

interface AuthorityDashboardProps {
  navigation: any;
}

const AuthorityDashboard: React.FC<AuthorityDashboardProps> = ({ navigation }) => {
  const { t } = useTranslation();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [period, setPeriod] = useState('30d');

  useEffect(() => {
    loadStats();
  }, [period]);

  const loadStats = async () => {
    try {
      setIsLoading(true);
      const data = await DashboardService.getStats(period);
      setStats(data);
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: t('common.error'),
        text2: error.message || 'Erreur lors du chargement des statistiques',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const renderPeriodSelector = () => (
    <View style={styles.periodSelector}>
      {['7d', '30d', '90d'].map(p => (
        <TouchableOpacity
          key={p}
          style={[styles.periodButton, period === p && styles.periodButtonActive]}
          onPress={() => setPeriod(p)}>
          <Text
            style={[
              styles.periodButtonText,
              period === p && styles.periodButtonTextActive,
            ]}>
            {p === '7d' ? '7 jours' : p === '30d' ? '30 jours' : '90 jours'}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  if (isLoading) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={styles.loadingText}>{t('common.loading')}</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (!stats) {
    return null;
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Dashboard Autorités</Text>
      </View>

      {renderPeriodSelector()}

      <ScrollView style={styles.scrollView} contentContainerStyle={styles.content}>
        {/* KPIs Grid */}
        <View style={styles.kpisGrid}>
          <View style={styles.kpiRow}>
            <View style={styles.kpiHalf}>
              <KPICard
                title="Total Scans"
                value={stats.kpis.totalScans.toLocaleString()}
                trend={stats.trends.scansGrowth}
                icon="📊"
                color={COLORS.primary}
              />
            </View>
            <View style={styles.kpiHalf}>
              <KPICard
                title="Taux d'Authenticité"
                value={`${stats.kpis.authenticityRate.toFixed(1)}%`}
                icon="✅"
                color={COLORS.success}
              />
            </View>
          </View>

          <View style={styles.kpiRow}>
            <View style={styles.kpiHalf}>
              <KPICard
                title="Médicaments Suspects"
                value={stats.kpis.suspiciousMedications.toLocaleString()}
                icon="🚫"
                color={COLORS.danger}
              />
            </View>
            <View style={styles.kpiHalf}>
              <KPICard
                title="Signalements"
                value={stats.kpis.totalReports.toLocaleString()}
                trend={stats.trends.reportsGrowth}
                icon="📝"
                color={COLORS.warning}
              />
            </View>
          </View>

          <View style={styles.kpiRow}>
            <View style={styles.kpiHalf}>
              <KPICard
                title="Utilisateurs Actifs"
                value={stats.kpis.activeUsers.toLocaleString()}
                icon="👥"
                color={COLORS.secondary}
              />
            </View>
            <View style={styles.kpiHalf}>
              <KPICard
                title="Nouveaux Utilisateurs"
                value={stats.kpis.newUsers.toLocaleString()}
                trend={stats.trends.usersGrowth}
                icon="✨"
                color={COLORS.success}
              />
            </View>
          </View>
        </View>

        {/* Alertes Récentes */}
        {stats.recentAlerts.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>🚨 Alertes Récentes</Text>
            {stats.recentAlerts.map(alert => (
              <View key={alert.id} style={styles.alertCard}>
                <View
                  style={[
                    styles.alertIndicator,
                    { backgroundColor: getSeverityColor(alert.severity) },
                  ]}
                />
                <View style={styles.alertContent}>
                  <Text style={styles.alertMessage}>{alert.message}</Text>
                  <Text style={styles.alertMedication}>{alert.medicationName}</Text>
                  <Text style={styles.alertTime}>
                    {new Date(alert.timestamp).toLocaleString('fr-FR')}
                  </Text>
                </View>
              </View>
            ))}
          </View>
        )}

        {/* Top Médicaments Contrefaits */}
        {stats.topCounterfeitMedications.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>🔝 Médicaments les Plus Signalés</Text>
            {stats.topCounterfeitMedications.map((med, index) => (
              <View key={index} style={styles.topMedCard}>
                <View style={styles.rankBadge}>
                  <Text style={styles.rankText}>{index + 1}</Text>
                </View>
                <View style={styles.topMedContent}>
                  <Text style={styles.topMedName}>{med.medicationName}</Text>
                  <Text style={styles.topMedCount}>{med.reportCount} signalements</Text>
                </View>
              </View>
            ))}
          </View>
        )}

        {/* Distribution Géographique */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>🗺️ Distribution Géographique</Text>
          {stats.geographicDistribution.map((geo, index) => (
            <View key={index} style={styles.geoCard}>
              <View style={styles.geoHeader}>
                <Text style={styles.geoRegion}>{geo.region}</Text>
                <Text style={styles.geoRate}>{geo.suspiciousRate.toFixed(1)}% suspects</Text>
              </View>
              <View style={styles.geoStats}>
                <Text style={styles.geoStat}>📊 {geo.scans} scans</Text>
                <Text style={styles.geoStat}>📝 {geo.reports} signalements</Text>
              </View>
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const getSeverityColor = (severity: string): string => {
  switch (severity) {
    case 'CRITICAL':
    case 'HIGH':
      return COLORS.danger;
    case 'MEDIUM':
      return COLORS.warning;
    case 'LOW':
      return COLORS.gray[500];
    default:
      return COLORS.gray[400];
  }
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.gray[50],
  },
  header: {
    backgroundColor: COLORS.white,
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.gray[200],
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: COLORS.gray[900],
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    color: COLORS.gray[600],
  },
  periodSelector: {
    flexDirection: 'row',
    backgroundColor: COLORS.white,
    padding: 12,
    gap: 8,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.gray[200],
  },
  periodButton: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: COLORS.gray[300],
    backgroundColor: COLORS.white,
    alignItems: 'center',
  },
  periodButtonActive: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  periodButtonText: {
    fontSize: 13,
    fontWeight: '500',
    color: COLORS.gray[700],
  },
  periodButtonTextActive: {
    color: COLORS.white,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 16,
  },
  kpisGrid: {
    marginBottom: 16,
  },
  kpiRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 12,
  },
  kpiHalf: {
    flex: 1,
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: COLORS.gray[900],
    marginBottom: 12,
  },
  alertCard: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
    flexDirection: 'row',
    borderWidth: 1,
    borderColor: COLORS.gray[200],
  },
  alertIndicator: {
    width: 4,
    borderRadius: 2,
    marginRight: 12,
  },
  alertContent: {
    flex: 1,
  },
  alertMessage: {
    fontSize: 14,
    fontWeight: '600',
    color: COLORS.gray[900],
    marginBottom: 4,
  },
  alertMedication: {
    fontSize: 13,
    color: COLORS.gray[700],
    marginBottom: 4,
  },
  alertTime: {
    fontSize: 11,
    color: COLORS.gray[500],
  },
  topMedCard: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.gray[200],
  },
  rankBadge: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: COLORS.danger + '20',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  rankText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: COLORS.danger,
  },
  topMedContent: {
    flex: 1,
  },
  topMedName: {
    fontSize: 14,
    fontWeight: '600',
    color: COLORS.gray[900],
    marginBottom: 2,
  },
  topMedCount: {
    fontSize: 12,
    color: COLORS.gray[600],
  },
  geoCard: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: COLORS.gray[200],
  },
  geoHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  geoRegion: {
    fontSize: 15,
    fontWeight: '600',
    color: COLORS.gray[900],
  },
  geoRate: {
    fontSize: 13,
    fontWeight: '600',
    color: COLORS.danger,
  },
  geoStats: {
    flexDirection: 'row',
    gap: 16,
  },
  geoStat: {
    fontSize: 13,
    color: COLORS.gray[600],
  },
});

export default AuthorityDashboard;

