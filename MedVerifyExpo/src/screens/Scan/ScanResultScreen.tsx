/**
 * Écran de résultat de scan
 */

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  SafeAreaView,
  TouchableOpacity,
} from 'react-native';
import { useTranslation } from 'react-i18next';
import { VerificationResponse, VerificationStatus } from '@types/medication.types';
import Button from '@components/UI/Button';
import { COLORS } from '@config/constants';

interface ScanResultScreenProps {
  navigation: any;
  route: {
    params: {
      result: VerificationResponse;
    };
  };
}

const ScanResultScreen: React.FC<ScanResultScreenProps> = ({ navigation, route }) => {
  const { t } = useTranslation();
  const { result } = route.params;

  const getStatusColor = () => {
    switch (result.status) {
      case VerificationStatus.AUTHENTIC:
        return COLORS.success;
      case VerificationStatus.SUSPICIOUS:
        return COLORS.danger;
      case VerificationStatus.UNKNOWN:
        return COLORS.warning;
      default:
        return COLORS.gray[500];
    }
  };

  const getStatusIcon = () => {
    switch (result.status) {
      case VerificationStatus.AUTHENTIC:
        return '✅';
      case VerificationStatus.SUSPICIOUS:
        return '🚫';
      case VerificationStatus.UNKNOWN:
        return '⚠️';
      default:
        return '❓';
    }
  };

  const getStatusText = () => {
    switch (result.status) {
      case VerificationStatus.AUTHENTIC:
        return t('result.authentic');
      case VerificationStatus.SUSPICIOUS:
        return t('result.suspicious');
      case VerificationStatus.UNKNOWN:
        return t('result.unknown');
      default:
        return 'Unknown';
    }
  };

  const getSourceLabel = (source?: string): string => {
    if (!source) return t('result.sourceUnknown');
    
    const labels: Record<string, string> = {
      'API_MEDICAMENTS_FR': t('result.sourceFrenchDatabase'),
      'CACHE_LOCAL': t('result.sourceLocalCache'),
      'DB_LOCAL': t('result.sourceLocalDatabase'),
      'DB_LOCAL_FALLBACK': t('result.sourceLocalDatabaseFallback'),
      'UNKNOWN': t('result.sourceUnknownLabel'),
      'NONE': t('result.sourceNone')
    };
    return labels[source] || source;
  };

  const getSourceColor = (source?: string): string => {
    if (!source) return COLORS.gray[500];
    
    if (source === 'API_MEDICAMENTS_FR') return '#0066CC';
    if (source.includes('CACHE')) return '#10B981';
    if (source.includes('LOCAL')) return '#F59E0B';
    return COLORS.gray[500];
  };

  const handleNewScan = () => {
    navigation.goBack();
  };

  const handleReport = () => {
    // Navigate to report screen (to be implemented)
    console.log('Report medication');
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.content}>
        {/* Status Badge */}
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor() }]}>
          <Text style={styles.statusIcon}>{getStatusIcon()}</Text>
          <Text style={styles.statusText}>{getStatusText()}</Text>
        </View>

        {/* Confidence */}
        <View style={styles.confidenceContainer}>
          <Text style={styles.confidenceLabel}>{t('result.confidence')}</Text>
          <View style={styles.confidenceBar}>
            <View
              style={[
                styles.confidenceFill,
                {
                  width: `${result.confidence * 100}%`,
                  backgroundColor: getStatusColor(),
                },
              ]}
            />
          </View>
          <Text style={styles.confidenceValue}>{Math.round(result.confidence * 100)}%</Text>
        </View>

        {/* Verification Source Badge */}
        {result.verificationSource && (
          <View style={styles.sourceContainer}>
            <Text style={styles.sourceLabel}>{t('result.verificationSource')}</Text>
            <View style={[styles.sourceBadge, { backgroundColor: getSourceColor(result.verificationSource) + '15' }]}>
              <Text style={[styles.sourceText, { color: getSourceColor(result.verificationSource) }]}>
                {getSourceLabel(result.verificationSource)}
              </Text>
            </View>
          </View>
        )}

        {/* Medication Details */}
        {result.medication && (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>{result.medication.name}</Text>
            {result.medication.genericName && (
              <Text style={styles.genericName}>{result.medication.genericName}</Text>
            )}
            
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>{t('result.manufacturer')}</Text>
              <Text style={styles.detailValue}>{result.medication.manufacturer}</Text>
            </View>

            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>{t('result.dosage')}</Text>
              <Text style={styles.detailValue}>{result.medication.dosage}</Text>
            </View>

            {result.medication.isEssential && (
              <View style={styles.essentialBadge}>
                <Text style={styles.essentialText}>⭐ {t('result.essentialMedicine')}</Text>
              </View>
            )}
          </View>
        )}

        {/* Alerts */}
        {result.alerts && result.alerts.length > 0 && (
          <View style={styles.card}>
            <Text style={[styles.cardTitle, styles.alertsTitle]}>{t('result.alerts')}</Text>
            {result.alerts.map((alert, index) => (
              <View
                key={index}
                style={[
                  styles.alertItem,
                  { borderLeftColor: getSeverityColor(alert.severity) },
                ]}>
                <Text style={styles.alertSeverity}>{alert.severity}</Text>
                <Text style={styles.alertMessage}>{alert.message}</Text>
              </View>
            ))}
          </View>
        )}

        {/* Medication Details Accordion */}
        {result.medication && (
          <>
            {result.medication.indications && result.medication.indications.length > 0 && (
              <View style={styles.card}>
                <Text style={styles.cardTitle}>{t('result.indications')}</Text>
                {result.medication.indications.map((indication, index) => (
                  <Text key={index} style={styles.listItem}>
                    • {indication}
                  </Text>
                ))}
              </View>
            )}

            {result.medication.sideEffects && result.medication.sideEffects.length > 0 && (
              <View style={styles.card}>
                <Text style={styles.cardTitle}>{t('result.sideEffects')}</Text>
                {result.medication.sideEffects.map((effect, index) => (
                  <Text key={index} style={styles.listItem}>
                    • {effect}
                  </Text>
                ))}
              </View>
            )}

            {result.medication.posology && (
              <View style={styles.card}>
                <Text style={styles.cardTitle}>{t('result.posology')}</Text>
                {result.medication.posology.adult && (
                  <View style={styles.posologyRow}>
                    <Text style={styles.posologyLabel}>{t('result.adult')} :</Text>
                    <Text style={styles.posologyValue}>{result.medication.posology.adult}</Text>
                  </View>
                )}
                {result.medication.posology.child && (
                  <View style={styles.posologyRow}>
                    <Text style={styles.posologyLabel}>{t('result.child')} :</Text>
                    <Text style={styles.posologyValue}>{result.medication.posology.child}</Text>
                  </View>
                )}
              </View>
            )}
          </>
        )}

        {/* Action Buttons */}
        <View style={styles.actions}>
          <Button title={t('result.newScan')} onPress={handleNewScan} />
          
          {result.status === VerificationStatus.SUSPICIOUS && (
            <Button
              title={t('result.report')}
              onPress={handleReport}
              variant="danger"
              style={styles.reportButton}
            />
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const getSeverityColor = (severity: string): string => {
  switch (severity.toUpperCase()) {
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
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 16,
  },
  statusBadge: {
    borderRadius: 12,
    padding: 24,
    alignItems: 'center',
    marginBottom: 16,
  },
  statusIcon: {
    fontSize: 48,
    marginBottom: 8,
  },
  statusText: {
    color: COLORS.white,
    fontSize: 24,
    fontWeight: 'bold',
  },
  confidenceContainer: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 16,
    marginBottom: 16,
  },
  confidenceLabel: {
    fontSize: 14,
    color: COLORS.gray[600],
    marginBottom: 8,
  },
  confidenceBar: {
    height: 8,
    backgroundColor: COLORS.gray[200],
    borderRadius: 4,
    overflow: 'hidden',
    marginBottom: 4,
  },
  confidenceFill: {
    height: '100%',
    borderRadius: 4,
  },
  confidenceValue: {
    fontSize: 16,
    fontWeight: '600',
    color: COLORS.gray[900],
    textAlign: 'right',
  },
  sourceContainer: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 16,
    marginBottom: 16,
  },
  sourceLabel: {
    fontSize: 12,
    color: COLORS.gray[600],
    marginBottom: 8,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  sourceBadge: {
    borderRadius: 6,
    padding: 10,
    alignItems: 'center',
  },
  sourceText: {
    fontSize: 14,
    fontWeight: '600',
  },
  card: {
    backgroundColor: COLORS.white,
    borderRadius: 8,
    padding: 16,
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: COLORS.gray[900],
    marginBottom: 12,
  },
  genericName: {
    fontSize: 14,
    color: COLORS.gray[600],
    marginBottom: 12,
    fontStyle: 'italic',
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  detailLabel: {
    fontSize: 14,
    color: COLORS.gray[600],
  },
  detailValue: {
    fontSize: 14,
    fontWeight: '500',
    color: COLORS.gray[900],
  },
  essentialBadge: {
    backgroundColor: COLORS.warning + '20',
    borderRadius: 6,
    padding: 8,
    marginTop: 12,
  },
  essentialText: {
    fontSize: 12,
    fontWeight: '600',
    color: COLORS.warning,
    textAlign: 'center',
  },
  alertsTitle: {
    color: COLORS.danger,
  },
  alertItem: {
    borderLeftWidth: 4,
    paddingLeft: 12,
    marginBottom: 12,
  },
  alertSeverity: {
    fontSize: 12,
    fontWeight: '600',
    color: COLORS.gray[600],
    marginBottom: 4,
  },
  alertMessage: {
    fontSize: 14,
    color: COLORS.gray[900],
  },
  listItem: {
    fontSize: 14,
    color: COLORS.gray[700],
    marginBottom: 8,
    lineHeight: 20,
  },
  posologyRow: {
    marginBottom: 8,
  },
  posologyLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: COLORS.gray[700],
    marginBottom: 4,
  },
  posologyValue: {
    fontSize: 14,
    color: COLORS.gray[600],
  },
  actions: {
    marginTop: 8,
    marginBottom: 24,
  },
  reportButton: {
    marginTop: 12,
  },
});

export default ScanResultScreen;

