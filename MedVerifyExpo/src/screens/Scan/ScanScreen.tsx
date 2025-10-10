/**
 * Écran de scan de médicament
 */

import React, { useState } from 'react';
import { View, StyleSheet, TouchableOpacity, Text, SafeAreaView, ActivityIndicator } from 'react-native';
import { useTranslation } from 'react-i18next';
import DataMatrixScanner from '@components/Scanner/DataMatrixScanner';
import { GS1Data } from '@types/scan.types';
import { VerificationRequest, DeviceInfo } from '@types/medication.types';
import ScanService from '@services/ScanService';
import { COLORS } from '@config/constants';
import Toast from 'react-native-toast-message';
import { Platform } from 'react-native';

interface ScanScreenProps {
  navigation: any;
}

const ScanScreen: React.FC<ScanScreenProps> = ({ navigation }) => {
  const { t } = useTranslation();
  const [isVerifying, setIsVerifying] = useState(false);
  const [isScanning, setIsScanning] = useState(true);

  const handleScanSuccess = async (data: GS1Data) => {
    console.log('Scan successful:', data);
    setIsScanning(false);
    setIsVerifying(true);

    try {
      // Préparer la requête de vérification
      const deviceInfo: DeviceInfo = {
        platform: Platform.OS,
        osVersion: Platform.Version.toString(),
        appVersion: '1.0.0',
        deviceModel: Platform.OS === 'ios' ? 'iOS Device' : 'Android Device',
      };

      const request: VerificationRequest = {
        gtin: data.gtin,
        serialNumber: data.serialNumber,
        batchNumber: data.batchNumber,
        expiryDate: data.expiryDate,
        deviceInfo,
      };

      // Appeler le service de vérification
      const result = await ScanService.verifyScan(request);

      // Naviguer vers l'écran de résultat
      navigation.navigate('ScanResult', { result });
    } catch (error: any) {
      console.error('Verification error:', error);
      
      Toast.show({
        type: 'error',
        text1: t('common.error'),
        text2: error.message || t('scan.scanError'),
      });

      // Réactiver le scan après quelques secondes
      setTimeout(() => {
        setIsScanning(true);
        setIsVerifying(false);
      }, 3000);
    }
  };

  const handleScanError = (error: string) => {
    console.error('Scan error:', error);
    
    Toast.show({
      type: 'error',
      text1: t('scan.scanError'),
      text2: error,
    });

    setTimeout(() => {
      setIsScanning(true);
    }, 2000);
  };

  const handleClose = () => {
    navigation.goBack();
  };

  return (
    <SafeAreaView style={styles.container}>
      <DataMatrixScanner
        onScanSuccess={handleScanSuccess}
        onError={handleScanError}
        active={isScanning && !isVerifying}
      />

      {/* Close button */}
      <TouchableOpacity style={styles.closeButton} onPress={handleClose}>
        <Text style={styles.closeButtonText}>✕</Text>
      </TouchableOpacity>

      {/* Verification overlay */}
      {isVerifying && (
        <View style={styles.verifyingOverlay}>
          <ActivityIndicator size="large" color={COLORS.primary} />
          <Text style={styles.verifyingText}>{t('scan.verifying')}</Text>
        </View>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.black,
  },
  closeButton: {
    position: 'absolute',
    top: 50,
    right: 20,
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 10,
  },
  closeButtonText: {
    color: COLORS.white,
    fontSize: 24,
    fontWeight: 'bold',
  },
  verifyingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  verifyingText: {
    color: COLORS.white,
    fontSize: 18,
    marginTop: 16,
    fontWeight: '500',
  },
});

export default ScanScreen;

