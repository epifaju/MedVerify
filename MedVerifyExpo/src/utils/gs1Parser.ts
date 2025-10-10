/**
 * Parser pour les codes GS1 Data Matrix
 * 
 * Application Identifiers (AI) standards:
 * - (01) : GTIN - 14 chiffres
 * - (17) : Date d'expiration - AAMMJJ
 * - (10) : Numéro de lot
 * - (21) : Numéro de série
 */

import { GS1Data } from '@types/scan.types';

interface ApplicationIdentifier {
  ai: string;
  length?: number;
  variable?: boolean;
}

const AI_DEFINITIONS: ApplicationIdentifier[] = [
  { ai: '01', length: 14 },       // GTIN
  { ai: '17', length: 6 },        // Expiry date
  { ai: '10', variable: true },   // Batch number
  { ai: '21', variable: true },   // Serial number
  { ai: '11', length: 6 },        // Production date (AAMMJJ)
  { ai: '13', length: 6 },        // Packaging date (AAMMJJ)
  { ai: '15', length: 6 },        // Best before date (AAMMJJ)
  { ai: '240', variable: true },  // Additional product identification
  { ai: '241', variable: true },  // Customer part number
  { ai: '250', variable: true },  // Secondary serial number
  { ai: '251', variable: true },  // Reference to source entity
  { ai: '400', variable: true },  // Customer's purchase order number
  { ai: '410', length: 13 },      // Ship to - Deliver to Global Location Number
  { ai: '420', length: 13 },      // Ship to - Deliver to postal code
];

/**
 * Parse un code GS1 Data Matrix
 * Supporte format standard (01)GTIN(17)DATE... et format sans parenthèses
 */
export const parseGS1 = (rawData: string): GS1Data | null => {
  try {
    if (!rawData || rawData.length === 0) {
      return null;
    }

    const result: GS1Data = {
      raw: rawData,
      gtin: '',
    };

    // Nettoyer les caractères spéciaux au début et à la fin
    let cleanData = rawData.trim();
    
    // Supprimer les caractères non ASCII et non alphanumériques au début
    cleanData = cleanData.replace(/^[^\x20-\x7E]+/, '');
    
    // Supprimer les caractères de contrôle et non imprimables
    cleanData = cleanData.replace(/[\x00-\x1F\x7F-\x9F]/g, '');
    
    console.log('Raw data:', rawData);
    console.log('Cleaned data:', cleanData);

    // Détecter le format (avec ou sans parenthèses)
    const hasParentheses = cleanData.includes('(');
    
    if (hasParentheses) {
      // Format standard avec parenthèses: (01)GTIN(17)DATE...
      parseWithParentheses(cleanData, result);
    } else {
      // Format compact sans parenthèses (plus courant pour Data Matrix)
      parseWithoutParentheses(cleanData, result);
    }

    // Valider que nous avons au moins le GTIN
    if (!result.gtin || result.gtin.length < 13) {
      console.warn('GTIN invalide ou manquant:', result.gtin);
      return null;
    }

    // Valider le GTIN avec checksum
    if (!isValidGTIN(result.gtin)) {
      console.warn('GTIN checksum invalide:', result.gtin);
      // On retourne quand même le résultat car certains codes peuvent avoir des checksums non standard
    }

    console.log('Parsed GS1 data:', result);
    return result;
  } catch (error) {
    console.error('Error parsing GS1 code:', error);
    return null;
  }
};

/**
 * Parse GS1 avec parenthèses (format standard)
 */
const parseWithParentheses = (rawData: string, result: GS1Data): void => {
  let position = 0;

  while (position < rawData.length) {
    let aiFound = false;

    for (const aiDef of AI_DEFINITIONS) {
      if (rawData.substring(position).startsWith(`(${aiDef.ai})`)) {
        position += aiDef.ai.length + 2; // Sauter (AI)

        let value: string;

        if (aiDef.variable) {
          // Pour les AI de longueur variable, chercher le prochain '(' ou la fin
          const nextAI = rawData.indexOf('(', position);
          value = nextAI > 0 
            ? rawData.substring(position, nextAI)
            : rawData.substring(position);
        } else {
          // Pour les AI de longueur fixe
          value = rawData.substring(position, position + (aiDef.length || 0));
        }

        // Assigner à la bonne propriété
        assignAIValue(aiDef.ai, value, result);

        position += value.length;
        aiFound = true;
        break;
      }
    }

    if (!aiFound) {
      position++;
    }
  }
};

/**
 * Parse GS1 sans parenthèses (format compact)
 */
const parseWithoutParentheses = (rawData: string, result: GS1Data): void => {
  let position = 0;
  
  console.log('Parsing without parentheses, length:', rawData.length);

  while (position < rawData.length) {
    let aiFound = false;

    // Essayer de trouver un AI au début de la position actuelle
    // Trier par longueur décroissante pour éviter les conflits (ex: '240' vs '24')
    const sortedAIs = [...AI_DEFINITIONS].sort((a, b) => b.ai.length - a.ai.length);
    
    for (const aiDef of sortedAIs) {
      if (rawData.substring(position).startsWith(aiDef.ai)) {
        console.log(`Found AI: ${aiDef.ai} at position ${position}`);
        position += aiDef.ai.length;

        let value: string;

        if (aiDef.variable) {
          // Pour les AI variables, chercher le prochain AI ou la fin
          let nextPosition = rawData.length;
          
          // Chercher le prochain AI connu
          for (const nextAI of AI_DEFINITIONS) {
            const nextIndex = rawData.indexOf(nextAI.ai, position);
            if (nextIndex > position && nextIndex < nextPosition) {
              nextPosition = nextIndex;
            }
          }
          
          value = rawData.substring(position, nextPosition);
          
          // Supprimer les caractères non alphanumériques à la fin
          value = value.replace(/[^a-zA-Z0-9]+$/, '');
        } else {
          // Pour les AI de longueur fixe
          value = rawData.substring(position, position + (aiDef.length || 0));
        }

        console.log(`AI ${aiDef.ai} value:`, value);
        assignAIValue(aiDef.ai, value, result);

        position += value.length;
        
        aiFound = true;
        break;
      }
    }

    if (!aiFound) {
      position++;
    }
  }
};

/**
 * Assigne une valeur d'AI au résultat
 */
const assignAIValue = (ai: string, value: string, result: GS1Data): void => {
  switch (ai) {
    case '01':
      result.gtin = value;
      break;
    case '17':
    case '15': // Best before date
      result.expiryDate = parseExpiryDate(value);
      break;
    case '11': // Production date
    case '13': // Packaging date
      // Ces dates peuvent être utiles mais ne sont pas critiques
      if (!result.expiryDate) {
        result.expiryDate = parseExpiryDate(value);
      }
      break;
    case '10':
      result.batchNumber = value;
      break;
    case '21':
      result.serialNumber = value;
      break;
    // Les autres AI sont ignorés pour l'instant
  }
};

/**
 * Parse la date d'expiration au format AAMMJJ
 */
const parseExpiryDate = (dateStr: string): string => {
  try {
    if (dateStr.length !== 6) {
      return dateStr;
    }

    const year = parseInt(dateStr.substring(0, 2), 10);
    const month = parseInt(dateStr.substring(2, 4), 10);
    const day = parseInt(dateStr.substring(4, 6), 10);

    // Assumer 20XX pour les années 00-99
    const fullYear = 2000 + year;

    // Format ISO: YYYY-MM-DD
    return `${fullYear}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
  } catch (error) {
    console.error('Error parsing expiry date:', error);
    return dateStr;
  }
};

/**
 * Valide un code GTIN
 */
export const isValidGTIN = (gtin: string): boolean => {
  if (!gtin || (gtin.length !== 13 && gtin.length !== 14)) {
    return false;
  }

  // Vérifier que ce sont tous des chiffres
  if (!/^\d+$/.test(gtin)) {
    return false;
  }

  // Validation checksum (algorithme GS1)
  const digits = gtin.split('').map(Number);
  let sum = 0;

  for (let i = digits.length - 2; i >= 0; i--) {
    const multiplier = (digits.length - 1 - i) % 2 === 0 ? 3 : 1;
    sum += digits[i] * multiplier;
  }

  const checkDigit = (10 - (sum % 10)) % 10;
  return checkDigit === digits[digits.length - 1];
};

export default {
  parseGS1,
  isValidGTIN,
};

