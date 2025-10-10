/**
 * Écran de création de signalement
 */

import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  TouchableOpacity,
  TextInput,
} from 'react-native';
import { useTranslation } from 'react-i18next';
import Button from '@components/UI/Button';
import Input from '@components/UI/Input';
import { COLORS } from '@config/constants';
import { ReportType } from '@types/report.types';
import ReportService from '@services/ReportService';
import Toast from 'react-native-toast-message';

interface ReportCreateScreenProps {
  navigation: any;
  route: {
    params?: {
      gtin?: string;
      serialNumber?: string;
      medicationId?: string;
    };
  };
}

const ReportCreateScreen: React.FC<ReportCreateScreenProps> = ({ navigation, route }) => {
  const { t } = useTranslation();
  const { gtin, serialNumber, medicationId } = route.params || {};

  const [reportType, setReportType] = useState<ReportType>(ReportType.COUNTERFEIT);
  const [description, setDescription] = useState('');
  const [purchaseLocation, setPurchaseLocation] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const reportTypes = [
    { value: ReportType.COUNTERFEIT, label: 'Contrefaçon' },
    { value: ReportType.QUALITY_ISSUE, label: 'Problème de qualité' },
    { value: ReportType.EXPIRED, label: 'Médicament périmé' },
    { value: ReportType.PACKAGING_DEFECT, label: 'Défaut d\'emballage' },
    { value: ReportType.ADVERSE_REACTION, label: 'Réaction indésirable' },
    { value: ReportType.OTHER, label: 'Autre' },
  ];

  const handleSubmit = async () => {
    if (description.length < 10) {
      Toast.show({
        type: 'error',
        text1: 'Erreur',
        text2: 'La description doit contenir au moins 10 caractères',
      });
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await ReportService.createReport({
        medicationId,
        gtin,
        serialNumber,
        reportType,
        description,
        purchaseLocation: purchaseLocation
          ? { name: purchaseLocation }
          : undefined,
        anonymous,
      });

      Toast.show({
        type: 'success',
        text1: 'Signalement envoyé',
        text2: `Référence: ${response.referenceNumber}`,
      });

      navigation.goBack();
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: 'Erreur',
        text2: error.message || 'Erreur lors de l\'envoi du signalement',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.content}>
        <Text style={styles.title}>Signaler un médicament</Text>
        <Text style={styles.subtitle}>Aidez-nous à lutter contre la contrefaçon</Text>

        {/* Type de signalement */}
        <Text style={styles.label}>Type de signalement</Text>
        <View style={styles.typeContainer}>
          {reportTypes.map(type => (
            <TouchableOpacity
              key={type.value}
              style={[
                styles.typeButton,
                reportType === type.value && styles.typeButtonActive,
              ]}
              onPress={() => setReportType(type.value)}>
              <Text
                style={[
                  styles.typeButtonText,
                  reportType === type.value && styles.typeButtonTextActive,
                ]}>
                {type.label}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Description */}
        <Text style={styles.label}>Description *</Text>
        <TextInput
          style={styles.textArea}
          placeholder="Décrivez le problème en détail (min 10 caractères)..."
          placeholderTextColor={COLORS.gray[400]}
          value={description}
          onChangeText={setDescription}
          multiline
          numberOfLines={6}
          textAlignVertical="top"
        />
        <Text style={styles.charCount}>{description.length} / 2000</Text>

        {/* Lieu d'achat */}
        <Input
          label="Lieu d'achat (optionnel)"
          placeholder="Nom de la pharmacie ou du magasin"
          value={purchaseLocation}
          onChangeText={setPurchaseLocation}
        />

        {/* Signalement anonyme */}
        <TouchableOpacity
          style={styles.checkboxContainer}
          onPress={() => setAnonymous(!anonymous)}>
          <View style={[styles.checkbox, anonymous && styles.checkboxChecked]}>
            {anonymous && <Text style={styles.checkboxCheck}>✓</Text>}
          </View>
          <Text style={styles.checkboxLabel}>Signalement anonyme</Text>
        </TouchableOpacity>

        <Text style={styles.infoText}>
          💡 Les autorités sanitaires seront notifiées automatiquement
        </Text>

        {/* Boutons */}
        <View style={styles.actions}>
          <Button
            title="Envoyer le signalement"
            onPress={handleSubmit}
            loading={isSubmitting}
            variant="danger"
          />
          <Button
            title="Annuler"
            onPress={() => navigation.goBack()}
            variant="outline"
            style={styles.cancelButton}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.white,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 24,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: COLORS.gray[900],
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: COLORS.gray[600],
    marginBottom: 24,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: COLORS.gray[700],
    marginBottom: 8,
  },
  typeContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 24,
  },
  typeButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: COLORS.gray[300],
    backgroundColor: COLORS.white,
  },
  typeButtonActive: {
    backgroundColor: COLORS.danger,
    borderColor: COLORS.danger,
  },
  typeButtonText: {
    fontSize: 13,
    color: COLORS.gray[700],
  },
  typeButtonTextActive: {
    color: COLORS.white,
    fontWeight: '600',
  },
  textArea: {
    borderWidth: 1,
    borderColor: COLORS.gray[300],
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    minHeight: 120,
    backgroundColor: COLORS.white,
    color: COLORS.gray[900],
  },
  charCount: {
    fontSize: 12,
    color: COLORS.gray[500],
    textAlign: 'right',
    marginTop: 4,
    marginBottom: 16,
  },
  checkboxContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 16,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderWidth: 2,
    borderColor: COLORS.gray[400],
    borderRadius: 4,
    marginRight: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  checkboxChecked: {
    backgroundColor: COLORS.primary,
    borderColor: COLORS.primary,
  },
  checkboxCheck: {
    color: COLORS.white,
    fontSize: 14,
    fontWeight: 'bold',
  },
  checkboxLabel: {
    fontSize: 14,
    color: COLORS.gray[700],
  },
  infoText: {
    fontSize: 13,
    color: COLORS.gray[600],
    backgroundColor: COLORS.gray[100],
    padding: 12,
    borderRadius: 8,
    marginBottom: 24,
  },
  actions: {
    gap: 12,
  },
  cancelButton: {
    marginTop: 8,
  },
});

export default ReportCreateScreen;

