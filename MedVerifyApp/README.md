# MedVerify Mobile App

Application mobile React Native pour la vérification d'authenticité de médicaments.

## Stack Technique

- **React Native**: 0.72.7
- **TypeScript**: 5.3+
- **Navigation**: React Navigation 6
- **State Management**: Redux Toolkit
- **Styling**: Tailwind RN
- **Scanner**: Vision Camera + Code Scanner
- **Database locale**: SQLite
- **Internationalisation**: i18next

## Prérequis

- Node.js 16+
- npm 8+ ou Yarn
- Xcode 14+ (pour iOS)
- Android Studio (pour Android)
- CocoaPods (pour iOS)

## Installation

```bash
# Installer les dépendances
npm install

# iOS uniquement - installer les pods
cd ios && pod install && cd ..
```

## Configuration

Créer un fichier `.env` à la racine:

```env
API_BASE_URL=http://localhost:8080/api/v1
```

## Lancement

```bash
# Start Metro bundler
npm start

# Run sur Android
npm run android

# Run sur iOS
npm run ios
```

## Tests

```bash
# Tests unitaires
npm test

# Tests avec coverage
npm run test:coverage

# Tests E2E (Detox)
# Voir documentation Detox séparée
```

## Structure du projet

```
src/
├── components/          # Composants réutilisables
│   ├── Scanner/        # Composants scanner
│   ├── UI/             # Boutons, Cards, etc.
│   └── Medication/     # Composants médicaments
├── screens/            # Écrans de l'app
│   ├── Auth/           # Login, Register
│   ├── Home/           # Écran d'accueil
│   ├── Scan/           # Scanner et résultats
│   ├── History/        # Historique des scans
│   ├── Report/         # Signalements
│   ├── Dashboard/      # Dashboard autorités
│   └── Profile/        # Profil utilisateur
├── navigation/         # Configuration navigation
├── services/           # API calls
├── store/              # Redux slices
├── utils/              # Utilitaires (GS1 parser, etc.)
├── database/           # SQLite schema
├── locales/            # Traductions
└── types/              # TypeScript types
```

## Permissions requises

### Android (`android/app/src/main/AndroidManifest.xml`)
- CAMERA
- INTERNET
- ACCESS_FINE_LOCATION

### iOS (`ios/MedVerifyApp/Info.plist`)
- NSCameraUsageDescription
- NSLocationWhenInUseUsageDescription

## Build Production

### Android

```bash
cd android
./gradlew assembleRelease
```

### iOS

```bash
cd ios
xcodebuild -workspace MedVerifyApp.xcworkspace -scheme MedVerifyApp -configuration Release
```

## Langues supportées

- Français (fr)
- Portugais (pt) - langue principale Guinée-Bissau
- Créole (gu) - optionnel future version

## Licence

MIT


