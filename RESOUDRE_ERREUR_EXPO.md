# 🔧 Résoudre l'Erreur Expo - "Failed to download remote update"

## 🚨 Erreur Rencontrée

```
uncaught error: java.io.IOException: Failed to download remote update
```

Cette erreur signifie que votre téléphone **ne peut pas se connecter** au serveur Metro Bundler sur votre PC.

---

## ✅ Solution 1 : Mode Tunnel (EN COURS)

J'ai lancé Expo en **mode tunnel** qui contourne les problèmes réseau.

### Dans le Terminal Actuel

Vous devriez voir (après quelques secondes) :

```
› Metro waiting on exp://XXX.XXX.XXX.XXX:8081
› Tunnel ready.
```

**Instructions** :

1. Ouvrez **Expo Go** sur votre téléphone
2. Scannez le **nouveau QR code** du terminal
3. L'app devrait se charger ! 🎉

⚠️ **Note** : Le tunnel peut prendre 20-30 secondes à s'établir.

---

## ✅ Solution 2 : Vérifier le WiFi

### Problème

PC et téléphone doivent être sur le **même réseau WiFi**.

### Vérification

1. **Sur votre PC** :

   ```powershell
   ipconfig
   ```

   Notez le nom du réseau WiFi

2. **Sur votre téléphone** :
   - Paramètres → WiFi
   - Vérifiez que c'est le **même réseau**

---

## ✅ Solution 3 : Désactiver le Firewall (Temporaire)

Le Firewall Windows peut bloquer le port 8081.

### Option A : Autoriser Node.js

1. Recherche Windows : **"Pare-feu Windows Defender"**
2. **"Autoriser une application"**
3. Cherchez **"Node.js"**
4. Cochez **"Privé"** et **"Public"**
5. OK

### Option B : Désactiver Temporairement

1. Recherche Windows : **"Pare-feu Windows Defender"**
2. **"Activer ou désactiver le Pare-feu"**
3. Désactivez pour **"Réseau privé"** UNIQUEMENT
4. ⚠️ Réactivez après le test !

---

## ✅ Solution 4 : Redémarrer Expo en Mode LAN

Si le tunnel ne fonctionne pas, essayons le mode LAN avec quelques ajustements :

### Étape 1 : Arrêter Expo

Dans le terminal Expo, appuyez sur **Ctrl+C**

### Étape 2 : Nettoyer et Relancer

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npx expo start --clear --lan
```

### Étape 3 : Scanner le QR Code

---

## ✅ Solution 5 : Utiliser l'URL Manuelle

Si le scan QR ne fonctionne pas :

1. Dans le terminal, notez l'URL :

   ```
   exp://192.168.1.16:8081
   ```

2. Dans **Expo Go** :
   - Onglet **"Projects"**
   - **"Enter URL manually"**
   - Tapez l'URL complète

---

## 🔍 Diagnostics Supplémentaires

### Test 1 : Ping depuis le téléphone

1. Installez **"Ping & Net"** depuis Play Store (optionnel)
2. Essayez de ping : `192.168.1.16`
3. Si ça ne répond pas → Problème réseau

### Test 2 : Port 8081 Ouvert ?

Sur votre PC, dans PowerShell :

```powershell
netstat -an | findstr "8081"
```

Vous devriez voir :

```
TCP    0.0.0.0:8081           0.0.0.0:0              LISTENING
```

Si ce n'est pas le cas, Metro n'écoute pas.

---

## ⚠️ IMPORTANT : Problème Backend

Même si Expo fonctionne, l'app ne pourra **pas se connecter au backend** si :

1. **Le téléphone n'est pas sur le même WiFi** que le PC
2. **Le backend utilise `localhost`** (il doit utiliser votre IP : `192.168.1.16`)

### Vérification Backend

Dans votre navigateur PC :

```
http://192.168.1.16:8080/actuator/health
```

**Si ça ne fonctionne PAS** → Le backend n'écoute que sur localhost !

**Solution** : Modifier `application.yml` :

```yaml
server:
  address: 0.0.0.0 # Écouter sur toutes les interfaces
  port: 8080
```

Puis redémarrer le backend.

---

## 🎯 Actions Immédiates

### Maintenant

1. **Attendez 30 secondes** que le tunnel se crée
2. **Scannez le nouveau QR code** dans Expo Go
3. Si ça marche → 🎉 **Testez l'app !**

### Si ça ne marche toujours pas

**Dites-moi** :

1. Quel message vous voyez dans le terminal Expo ?
2. Le téléphone et PC sont-ils sur le même WiFi ?
3. Avez-vous un VPN actif ?

Je vous guiderai étape par étape ! 😊

---

## 📱 Alternative : Tester sur Émulateur

Si le téléphone physique pose trop de problèmes, on peut tester sur un **émulateur Android** :

```powershell
# Dans le terminal Expo, appuyez sur 'a'
# Cela lancera l'émulateur Android Studio si installé
```

---

**Attendez encore 15 secondes et dites-moi ce que vous voyez dans le terminal !** 🚀
