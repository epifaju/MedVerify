/**
 * Composant Scanner Data Matrix
 * Utilise react-native-vision-camera pour scanner les codes
 */

import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Vibration } from 'react-native';
import { Camera, useCameraDevice, useCodeScanner } from 'react-native-vision-camera';
import { useCameraPermission } from 'react-native-vision-camera';
import { parseGS1 } from '@utils/gs1Parser';
import { GS1Data } from '@types/scan.types';
import { COLORS } from '@config/constants';

interface DataMatrixScannerProps {
  onScanSuccess: (data: GS1Data) => void;
  onError?: (error: string) => void;
  active?: boolean;
}

const DataMatrixScanner: React.FC<DataMatrixScannerProps> = ({
  onScanSuccess,
  onError,
  active = true,
}) => {
  const device = useCameraDevice('back');
  const { hasPermission, requestPermission } = useCameraPermission();
  const [isScanning, setIsScanning] = useState(true);
  const [flashEnabled, setFlashEnabled] = useState(false);

  useEffect(() => {
    if (!hasPermission) {
      requestPermission();
    }
  }, [hasPermission, requestPermission]);

  const codeScanner = useCodeScanner({
    // Data Matrix en priorité selon PRD, puis QR comme fallback
    codeTypes: ['data-matrix', 'qr', 'ean-13', 'ean-8', 'code-128', 'code-39'],
    onCodeScanned: codes => {
      if (!isScanning || !active) {
        return;
      }

      const code = codes[0];
      if (code && code.value) {
        console.log('🔍 [SCANNER] Code scanned:', code.type);
        console.log('🔍 [SCANNER] Raw value:', code.value);

        // Parse le code GS1
        const parsedData = parseGS1(code.value);
        
        console.log('🔍 [SCANNER] Parsed data:', parsedData);

        if (parsedData && parsedData.gtin) {
          console.log('✅ [SCANNER] GTIN extracted:', parsedData.gtin);
          
          // Vibration de feedback
          Vibration.vibrate(100);

          // Désactiver le scan temporairement
          setIsScanning(false);

          // Appeler le callback
          onScanSuccess(parsedData);

          // Réactiver le scan après 3 secondes
          setTimeout(() => {
            setIsScanning(true);
          }, 3000);
        } else {
          console.error('❌ [SCANNER] Parse failed or no GTIN. Raw:', code.value);
          console.error('❌ [SCANNER] Parsed result:', parsedData);
          onError?.('Code invalide ou non reconnu. Veuillez scanner un code Data Matrix GS1.');
        }
      }
    },
  });

  if (!hasPermission) {
    return (
      <View style={styles.container}>
        <Text style={styles.permissionText}>
          Permission caméra requise pour scanner les médicaments
        </Text>
        <TouchableOpacity style={styles.permissionButton} onPress={requestPermission}>
          <Text style={styles.permissionButtonText}>Autoriser la caméra</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (!device) {
    return (
      <View style={styles.container}>
        <Text style={styles.errorText}>Caméra non disponible</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Camera
        style={StyleSheet.absoluteFill}
        device={device}
        isActive={active && isScanning}
        codeScanner={codeScanner}
        torch={flashEnabled ? 'on' : 'off'}
        enableAutoFocusSystem={true}
      />
      
      {/* Overlay avec cadre de scan */}
      <View style={styles.overlay}>
        <View style={styles.topOverlay}>
          {/* Bouton Flash */}
          <TouchableOpacity 
            style={styles.flashButton}
            onPress={() => setFlashEnabled(!flashEnabled)}
          >
            <Text style={styles.flashButtonText}>
              {flashEnabled ? '⚡ Flash ON' : '⚡ Flash OFF'}
            </Text>
          </TouchableOpacity>
        </View>
        <View style={styles.middleContainer}>
          <View style={styles.sideOverlay} />
          <View style={styles.scanFrame}>
            <View style={[styles.corner, styles.topLeft]} />
            <View style={[styles.corner, styles.topRight]} />
            <View style={[styles.corner, styles.bottomLeft]} />
            <View style={[styles.corner, styles.bottomRight]} />
          </View>
          <View style={styles.sideOverlay} />
        </View>
        <View style={styles.bottomOverlay}>
          <Text style={styles.instructionText}>
            Pointez la caméra vers le code Data Matrix sur l'emballage du médicament
          </Text>
          <Text style={styles.tipText}>
            💡 Pour une meilleure détection, assurez-vous d'avoir un bon éclairage
          </Text>
        </View>
      </View>
    </View>
  );
};

const OVERLAY_COLOR = 'rgba(0, 0, 0, 0.6)';
const FRAME_SIZE = 250;
const CORNER_SIZE = 40;
const CORNER_WIDTH = 4;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.black,
  },
  permissionText: {
    color: COLORS.white,
    fontSize: 16,
    textAlign: 'center',
    marginTop: 100,
    paddingHorizontal: 24,
  },
  permissionButton: {
    backgroundColor: COLORS.primary,
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
    marginTop: 20,
    marginHorizontal: 48,
  },
  permissionButtonText: {
    color: COLORS.white,
    fontSize: 16,
    fontWeight: '600',
    textAlign: 'center',
  },
  errorText: {
    color: COLORS.danger,
    fontSize: 16,
    textAlign: 'center',
    marginTop: 100,
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
  },
  topOverlay: {
    flex: 1,
    backgroundColor: OVERLAY_COLOR,
    alignItems: 'flex-end',
    padding: 20,
  },
  flashButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 20,
    marginTop: 40,
  },
  flashButtonText: {
    color: COLORS.white,
    fontSize: 14,
    fontWeight: '600',
  },
  middleContainer: {
    flexDirection: 'row',
  },
  sideOverlay: {
    flex: 1,
    backgroundColor: OVERLAY_COLOR,
  },
  scanFrame: {
    width: FRAME_SIZE,
    height: FRAME_SIZE,
    position: 'relative',
  },
  corner: {
    position: 'absolute',
    width: CORNER_SIZE,
    height: CORNER_SIZE,
    borderColor: COLORS.success,
  },
  topLeft: {
    top: 0,
    left: 0,
    borderTopWidth: CORNER_WIDTH,
    borderLeftWidth: CORNER_WIDTH,
  },
  topRight: {
    top: 0,
    right: 0,
    borderTopWidth: CORNER_WIDTH,
    borderRightWidth: CORNER_WIDTH,
  },
  bottomLeft: {
    bottom: 0,
    left: 0,
    borderBottomWidth: CORNER_WIDTH,
    borderLeftWidth: CORNER_WIDTH,
  },
  bottomRight: {
    bottom: 0,
    right: 0,
    borderBottomWidth: CORNER_WIDTH,
    borderRightWidth: CORNER_WIDTH,
  },
  bottomOverlay: {
    flex: 1,
    backgroundColor: OVERLAY_COLOR,
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  instructionText: {
    color: COLORS.white,
    fontSize: 14,
    textAlign: 'center',
  },
  tipText: {
    color: COLORS.white,
    fontSize: 12,
    textAlign: 'center',
    marginTop: 8,
    opacity: 0.8,
  },
});

export default DataMatrixScanner;


