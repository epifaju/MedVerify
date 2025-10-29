# 📦 Installation des dépendances pour la fonctionnalité Pharmacies

## Dépendances à installer

Exécutez les commandes suivantes dans le dossier `MedVerifyApp/MedVerifyExpo/` :

```bash
cd MedVerifyApp/MedVerifyExpo

# Installer react-native-maps pour les cartes
npx expo install react-native-maps

# Installer geolib pour calculs de distance
npm install geolib

# Redux toolkit (si pas déjà installé)
npm install @reduxjs/toolkit react-redux

# Types
npm install --save-dev @types/geolib
```

## ✅ Dépendances déjà présentes

Ces packages sont déjà dans votre `package.json` :

- ✅ `expo-location` - Pour la géolocalisation
- ✅ `@react-navigation/native` - Pour la navigation
- ✅ `axios` - Pour les appels API

## 📋 Configuration requise

### Android (android/app/src/main/AndroidManifest.xml)

```xml
<!-- Permissions localisation -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Google Maps API Key -->
<application>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="VOTRE_GOOGLE_MAPS_API_KEY"/>
</application>
```

### iOS (ios/MedVerifyApp/Info.plist)

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>Nous avons besoin de votre position pour trouver les pharmacies proches de vous</string>

<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>Nous utilisons votre position pour trouver les pharmacies proches</string>
```

### Expo (app.json)

Ajoutez dans `app.json` :

```json
{
  "expo": {
    "plugins": [
      [
        "expo-location",
        {
          "locationAlwaysAndWhenInUsePermission": "Nous utilisons votre position pour trouver les pharmacies proches de vous."
        }
      ]
    ],
    "android": {
      "config": {
        "googleMaps": {
          "apiKey": "VOTRE_GOOGLE_MAPS_API_KEY"
        }
      }
    },
    "ios": {
      "config": {
        "googleMapsApiKey": "VOTRE_GOOGLE_MAPS_API_KEY"
      }
    }
  }
}
```

## 🔑 Google Maps API Key

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créer un projet ou sélectionner un projet existant
3. Activer l'API "Maps SDK for Android" et "Maps SDK for iOS"
4. Créer une clé API dans "APIs & Services" > "Credentials"
5. Remplacer `VOTRE_GOOGLE_MAPS_API_KEY` par votre clé

## 📝 Note

Pour un projet Expo géré (Expo Go), vous n'avez pas besoin de configurer les fichiers natifs Android/iOS.
Les configurations se font directement dans `app.json`.

Si vous utilisez un build natif (EAS Build ou eject), vous devrez configurer les fichiers natifs manuellement.

