# 🔧 Solutions - Erreur Expo "failed to download remote update"

## ❌ Erreur actuelle

```
Uncaught Error: java.io.IOException: failed to download remote update
```

---

## ✅ Solution appliquée MAINTENANT

```bash
cd MedVerifyApp\MedVerifyExpo
npx expo start --clear --tunnel
```

**Ce que cela fait :**

- `--clear` : Nettoie le cache Metro
- `--tunnel` : Utilise un tunnel ngrok (contourne les problèmes réseau)

---

## 📱 Que faire maintenant

### 1. Attendre que le serveur démarre (30-60 secondes)

Vous devriez voir :

```
› Metro waiting on exp://...
› Scan the QR code above with Expo Go (Android) or the Camera app (iOS)
```

### 2. Ouvrir Expo Go sur votre téléphone

### 3. Scanner le QR code affiché dans le terminal

### 4. Attendre le chargement (peut prendre 1-2 minutes avec tunnel)

---

## 🐛 Si l'erreur persiste

### Option A : Réinstaller complètement

```bash
cd MedVerifyApp\MedVerifyExpo

# 1. Supprimer node_modules
rmdir /s /q node_modules
del package-lock.json

# 2. Réinstaller
npm install

# 3. Nettoyer le cache
npm cache clean --force

# 4. Relancer avec tunnel
npx expo start --clear --tunnel
```

---

### Option B : Vérifier Expo Go

Sur votre téléphone Android :

1. Ouvrir **Google Play Store**
2. Chercher "**Expo Go**"
3. Vérifier que c'est la **dernière version**
4. Si pas à jour → Mettre à jour
5. **Vider le cache d'Expo Go** :
   - Paramètres Android → Apps → Expo Go
   - Stockage → Vider le cache
   - Vider les données (optionnel)

---

### Option C : Désactiver le pare-feu temporairement

```bash
# Windows PowerShell (Admin)
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled False

# ATTENTION : Réactiver après le test :
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled True
```

**OU créer une règle pour le port 8081 :**

1. Ouvrir "Pare-feu Windows Defender"
2. Règles de trafic entrant → Nouvelle règle
3. Port → TCP → 8081
4. Autoriser la connexion

---

### Option D : Utiliser un émulateur Android

Si le téléphone physique pose trop de problèmes :

#### Avec Android Studio :

1. Installer Android Studio
2. Tools → AVD Manager
3. Create Virtual Device
4. Choisir un device (ex: Pixel 5)
5. Download image système (API 33)
6. Lancer l'émulateur
7. Dans le terminal Expo, appuyer sur 'a'

**Avantage** : Pas de problème de réseau, plus stable.

---

## ⚡ Solutions rapides par type d'erreur

### Si "Network request failed"

```bash
# Utiliser tunnel
npx expo start --tunnel
```

### Si "Unable to resolve module"

```bash
# Réinstaller dépendances
rm -rf node_modules
npm install
npx expo start --clear
```

### Si "Expo Go crashes immediately"

```bash
# Problème de compatibilité → Mettre à jour Expo Go
# Ou downgrader expo dans package.json
```

---

## 🔍 Diagnostic avancé

### Voir les logs détaillés

#### Sur le PC :

```bash
npx expo start --clear --tunnel --verbose
```

#### Sur le téléphone (via ADB) :

```bash
# Si ADB installé
adb logcat | findstr "Expo"
```

---

## 📊 Checklist de vérification

Avant de relancer, vérifier :

- [ ] PC et téléphone sur le même WiFi (ou mode tunnel)
- [ ] Expo Go à jour sur le téléphone
- [ ] Cache nettoyé (`--clear`)
- [ ] Node modules réinstallés
- [ ] Pare-feu ne bloque pas
- [ ] Port 8081 libre
- [ ] Aucune erreur dans le terminal

---

## 🎯 Ma recommandation

### Essai 1 : Mode tunnel (en cours)

```bash
npx expo start --clear --tunnel
```

**Attendez 1-2 minutes** que le tunnel se connecte, puis scannez le QR code.

### Essai 2 : Si erreur persiste

```bash
# Nettoyer complètement
rmdir /s /q node_modules
del package-lock.json
npm cache clean --force
npm install

# Relancer
npx expo start --clear --tunnel
```

### Essai 3 : Si toujours erreur

**Utiliser un émulateur Android** (plus stable).

---

## 🆘 Commandes à essayer MAINTENANT

Le serveur est en train de démarrer avec `--tunnel`.

**Attendez 30-60 secondes** que vous voyiez :

```
› Metro waiting on exp://...
› Scan the QR code above
```

**Ensuite :**

1. Ouvrir Expo Go sur le téléphone
2. Scanner le QR code
3. Attendre le chargement (1-2 min avec tunnel)

---

## 📞 Si ça ne fonctionne toujours pas

**Dites-moi :**

1. Quelle erreur exacte vous voyez maintenant
2. Quel message dans le terminal
3. Si le QR code s'affiche

Je pourrai alors diagnostiquer plus précisément ! 🔍

---

**Le serveur est en train de démarrer... Attendez le QR code ! 🚀**
