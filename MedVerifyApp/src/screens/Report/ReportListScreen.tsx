/**
 * Écran de liste des signalements
 */

import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';
import { useTranslation } from 'react-i18next';
import { ReportDetails, ReportStatus } from '@types/report.types';
import ReportService from '@services/ReportService';
import { COLORS } from '@config/constants';
import Toast from 'react-native-toast-message';

interface ReportListScreenProps {
  navigation: any;
}

const ReportListScreen: React.FC<ReportListScreenProps> = ({ navigation }) => {
  const { t } = useTranslation();
  const [reports, setReports] = useState<ReportDetails[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadReports();
  }, []);

  const loadReports = async () => {
    try {
      setIsLoading(true);
      const data = await ReportService.getMyReports();
      setReports(data);
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: t('common.error'),
        text2: error.message || 'Erreur lors du chargement des signalements',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusColor = (status: ReportStatus): string => {
    switch (status) {
      case ReportStatus.SUBMITTED:
        return COLORS.gray[500];
      case ReportStatus.UNDER_REVIEW:
        return COLORS.warning;
      case ReportStatus.CONFIRMED:
        return COLORS.danger;
      case ReportStatus.REJECTED:
        return COLORS.gray[400];
      case ReportStatus.CLOSED:
        return COLORS.success;
      default:
        return COLORS.gray[500];
    }
  };

  const getStatusLabel = (status: ReportStatus): string => {
    switch (status) {
      case ReportStatus.SUBMITTED:
        return 'Soumis';
      case ReportStatus.UNDER_REVIEW:
        return 'En examen';
      case ReportStatus.CONFIRMED:
        return 'Confirmé';
      case ReportStatus.REJECTED:
        return 'Rejeté';
      case ReportStatus.CLOSED:
        return 'Clos';
      default:
        return status;
    }
  };

  const renderReportCard = ({ item }: { item: ReportDetails }) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() => navigation.navigate('ReportDetails', { report: item })}>
      <View style={styles.cardHeader}>
        <Text style={styles.referenceNumber}>{item.referenceNumber}</Text>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(item.status) + '20' }]}>
          <Text style={[styles.statusText, { color: getStatusColor(item.status) }]}>
            {getStatusLabel(item.status)}
          </Text>
        </View>
      </View>

      {item.medicationName && (
        <Text style={styles.medicationName}>{item.medicationName}</Text>
      )}

      <Text style={styles.description} numberOfLines={2}>
        {item.description}
      </Text>

      <Text style={styles.date}>
        {new Date(item.createdAt).toLocaleDateString('fr-FR', {
          day: '2-digit',
          month: 'long',
          year: 'numeric',
        })}
      </Text>
    </TouchableOpacity>
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

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Mes signalements</Text>
        <Button
          title={t('report.newReport')}
          onPress={() => navigation.navigate('ReportCreate')}
          size="small"
        />
      </View>

      {reports.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>Aucun signalement</Text>
          <Text style={styles.emptySubtext}>
            Vous n'avez pas encore créé de signalement
          </Text>
        </View>
      ) : (
        <FlatList
          data={reports}
          renderItem={renderReportCard}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.listContent}
          onRefresh={loadReports}
          refreshing={isLoading}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.gray[50],
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: COLORS.white,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.gray[200],
  },
  title: {
    fontSize: 20,
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
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  emptyText: {
    fontSize: 18,
    fontWeight: '600',
    color: COLORS.gray[700],
    marginBottom: 8,
  },
  emptySubtext: {
    fontSize: 14,
    color: COLORS.gray[500],
    textAlign: 'center',
  },
  listContent: {
    padding: 16,
  },
  card: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: COLORS.gray[200],
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  referenceNumber: {
    fontSize: 12,
    fontWeight: '600',
    color: COLORS.primary,
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 11,
    fontWeight: '600',
  },
  medicationName: {
    fontSize: 16,
    fontWeight: '600',
    color: COLORS.gray[900],
    marginBottom: 4,
  },
  description: {
    fontSize: 14,
    color: COLORS.gray[700],
    marginBottom: 8,
    lineHeight: 20,
  },
  date: {
    fontSize: 12,
    color: COLORS.gray[500],
  },
});

export default ReportListScreen;

