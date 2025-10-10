/**
 * Configuration i18next pour l'internationalisation
 */

import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { getLocales } from 'react-native-localize';

import fr from './fr.json';
import pt from './pt.json';

const deviceLanguage = getLocales()[0]?.languageCode || 'fr';

i18n.use(initReactI18next).init({
  compatibilityJSON: 'v3',
  resources: {
    fr: { translation: fr },
    pt: { translation: pt },
  },
  lng: deviceLanguage,
  fallbackLng: 'fr',
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;


