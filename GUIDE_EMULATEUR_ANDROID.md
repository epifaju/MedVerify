# 📱 Guide : Utiliser un émulateur Android avec MedVerify

## 🎯 Pourquoi utiliser un émulateur ?

L'émulateur Android élimine les problèmes de réseau WiFi et de pare-feu car il tourne sur le même PC que le backend.

---

## 📥 Installation

### Étape 1 : Installer Android Studio

1. Téléchargez Android Studio : https://developer.android.com/studio
2. Installez-le avec les options par défaut
3. Lancez Android Studio

### Étape 2 : Créer un appareil virtuel (AVD)

1. Dans Android Studio, allez dans **Tools** → **Device Manager**
2. Cliquez sur **Create Device**
3. Sélectionnez un modèle (ex: **Pixel 6**)
4. Sélectionnez une image système (ex: **API 33 - Android 13**)
5. Cliquez sur **Finish**

---

## 🚀 Utiliser l'émulateur avec Expo

### Étape 1 : Lancer l'émulateur

Dans Android Studio :

- Ouvrez **Device Manager**
- Cliquez sur le bouton ▶️ à côté de votre AVD

Attendez que l'émulateur démarre complètement (environ 1 minute).

### Étape 2 : Changer l'URL de l'API

**IMPORTANT** : Avec un émulateur, l'adresse IP est différente !

Ouvrez `MedVerifyExpo/src/config/constants.ts` et changez :

```typescript
export const API_BASE_URL = "http://10.0.2.2:8080/api/v1"; // ✅ Pour émulateur
```

Au lieu de :

```typescript
export const API_BASE_URL = "http://192.168.1.16:8080/api/v1"; // ❌ Pour appareil physique
```

### Étape 3 : Lancer l'application sur l'émulateur

Dans le terminal (dossier MedVerify) :

```bash
cd MedVerifyExpo
npx expo start --clear
```

Puis appuyez sur **`a`** pour lancer sur Android.

L'application devrait s'installer et se lancer automatiquement sur l'émulateur ! 🎉

---

## ✅ Avantages de l'émulateur

- ✅ Pas de problème de pare-feu
- ✅ Pas de problème de WiFi
- ✅ Debugging plus facile
- ✅ Performance similaire à un vrai appareil
- ✅ Possibilité de tester sur différentes versions d'Android

## ❌ Inconvénients

- ❌ Nécessite Android Studio (lourd)
- ❌ Consomme des ressources PC
- ❌ Pas de test en conditions réelles

---

## 🔄 Revenir au téléphone physique

Pour revenir au téléphone physique :

1. Changez l'URL dans `constants.ts` :

   ```typescript
   export const API_BASE_URL = "http://192.168.1.16:8080/api/v1";
   ```

2. Rechargez Expo :

   ```bash
   npx expo start --clear
   ```

3. Scannez le QR code avec Expo Go

---

## 🆘 Besoin d'aide ?

Si vous avez des questions sur l'émulateur, dites-le-moi !



