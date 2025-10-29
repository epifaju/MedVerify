# 🔧 Fix : Erreur Network Error lors de la connexion

## 🐛 Problème

Lors de la tentative de connexion, vous recevez l'erreur :

```
Erreur de connexion: [AxiosError: Network Error]
```

## ✅ Corrections Appliquées

### 1. Amélioration de la gestion des erreurs réseau

**Fichier modifié** : `MedVerifyApp/src/store/slices/authSlice.ts`

- Détection spécifique des erreurs réseau (`ERR_NETWORK`)
- Message d'erreur plus informatif avec étapes de diagnostic
- Meilleure gestion des différents types d'erreurs Axios

### 2. Configuration vérifiée

- ✅ AndroidManifest.xml : `usesCleartextTraffic="true"` activé
- ✅ Permissions INTERNET : Ajoutée
- ✅ Backend configuré pour écouter sur `0.0.0.0:8080` (toutes interfaces)
- ✅ CORS configuré pour autoriser `http://192.168.1.16:8080`

---

## 🔍 Diagnostic - Étapes de Vérification

### Étape 1 : Vérifier que le backend tourne

```powershell
# Vérifier que le port 8080 est en écoute
netstat -ano | findstr :8080

# Tester depuis votre PC
Invoke-WebRequest -Uri "http://192.168.1.16:8080/actuator/health" -Method GET
```

**Résultat attendu** : StatusCode 200

---

### Étape 2 : Vérifier l'IP de votre PC

```powershell
ipconfig | findstr IPv4
```

**Confirmer** : L'IP doit correspondre à celle dans `MedVerifyApp/src/config/constants.ts`

```typescript
export const API_BASE_URL =
  process.env.API_BASE_URL || "http://192.168.1.16:8080/api/v1";
```

---

### Étape 3 : Vérifier le Firewall Windows

**Option A : Vérifier les règles existantes**

```powershell
netsh advfirewall firewall show rule name=all | findstr 8080
```

**Option B : Ajouter une règle de pare-feu (si nécessaire)**

Exécutez le script `configure-firewall.ps1` en tant qu'administrateur :

```powershell
# Clic droit sur configure-firewall.ps1 → "Exécuter en tant qu'administrateur"
```

Ou manuellement :

```powershell
netsh advfirewall firewall add rule name="MedVerify Backend Port 8080" dir=in action=allow protocol=TCP localport=8080
```

---

### Étape 4 : Vérifier la connexion réseau

**Important** : Votre téléphone Android et votre PC doivent être sur le **même réseau WiFi**.

1. **Sur votre téléphone** :
   - Allez dans Paramètres → WiFi
   - Notez le nom du réseau WiFi
2. **Sur votre PC** :

   - Vérifiez que vous êtes connecté au même réseau WiFi

3. **Test de ping** (depuis un terminal sur le téléphone ou via ADB) :
   ```powershell
   adb shell ping -c 4 192.168.1.16
   ```

---

### Étape 5 : Vérifier depuis le navigateur du téléphone

Sur votre téléphone Android, ouvrez Chrome et testez :

```
http://192.168.1.16:8080/actuator/health
```

**Résultat attendu** : Page JSON avec `{"status":"UP"}`

**Si ça ne fonctionne pas** : Le problème est réseau/firewall, pas dans l'application.

---

### Étape 6 : Vérifier l'URL dans l'application

**Fichier** : `MedVerifyApp/src/config/constants.ts`

Vérifiez que l'URL correspond bien :

- **Appareil physique** : `http://192.168.1.16:8080/api/v1` (IP de votre PC)
- **Émulateur Android** : `http://10.0.2.2:8080/api/v1`

---

## 🚀 Solutions Spécifiques

### Solution 1 : Firewall Windows bloque

Si le firewall bloque les connexions :

1. Ouvrez le **Pare-feu Windows Defender** :

   - Panneau de configuration → Système et sécurité → Pare-feu Windows Defender → Paramètres avancés

2. **Créer une règle entrante** :
   - Clic droit "Règles de trafic entrant" → Nouvelle règle
   - Type : Port
   - Protocole : TCP
   - Ports : 8080
   - Action : Autoriser la connexion
   - Profils : Privé (et Domaine si nécessaire)
   - Nom : "MedVerify Backend"

---

### Solution 2 : Le téléphone n'est pas sur le même réseau

**Problème** : Le téléphone est sur un réseau mobile (4G/5G) ou un WiFi différent.

**Solution** :

1. Connectez votre téléphone au même WiFi que votre PC
2. Ou utilisez un point d'accès WiFi de votre PC (si disponible)

---

### Solution 3 : L'IP du PC a changé

**Problème** : L'IP dynamique a changé.

**Solution** :

1. Vérifiez la nouvelle IP : `ipconfig | findstr IPv4`
2. Mettez à jour `MedVerifyApp/src/config/constants.ts` avec la nouvelle IP
3. Redémarrez l'application mobile

---

### Solution 4 : Utiliser localhost si via USB Debugging (Avancé)

Si vous utilisez le débogage USB et que votre téléphone est connecté par USB, vous pouvez utiliser `adb reverse` :

```powershell
adb reverse tcp:8080 tcp:8080
```

Puis changez l'URL dans `constants.ts` :

```typescript
export const API_BASE_URL = "http://localhost:8080/api/v1";
```

**Note** : Cette solution ne fonctionne que pour le développement avec USB.

---

## 📱 Test Final

Après avoir appliqué les corrections :

1. **Relancer l'application mobile**
2. **Essayer de se connecter** avec :

   - Email : `admin@medverify.gw`
   - Mot de passe : `Admin@123456`

3. **Si l'erreur persiste**, le nouveau message d'erreur devrait indiquer exactement quoi vérifier.

---

## 🔍 Logs de Debug

Pour voir les détails de l'erreur dans l'application :

1. **Via React Native Debugger** ou **Flipper**
2. **Via les logs** :

   ```powershell
   npx react-native log-android
   # ou
   npx react-native log-ios
   ```

3. **Cherchez** les lignes avec `AxiosError` ou `Network Error`

---

## ✅ Checklist de Résolution

- [ ] Backend démarré et accessible sur `http://192.168.1.16:8080`
- [ ] Téléphone et PC sur le même réseau WiFi
- [ ] Firewall Windows autorise le port 8080
- [ ] IP dans `constants.ts` correspond à l'IP réelle du PC
- [ ] Test de l'URL depuis le navigateur du téléphone fonctionne
- [ ] Application mobile relancée après modifications

---

## 📞 Prochaines Étapes

Si le problème persiste après toutes ces vérifications :

1. **Capturez l'écran** du message d'erreur amélioré
2. **Vérifiez les logs** du backend : `medverify-backend/logs/medverify.log`
3. **Testez avec curl** depuis le téléphone (si possible) ou via ADB
4. **Vérifiez les logs Android** avec `adb logcat | findstr -i "network\|axios\|medverify"`

---

## 📝 Notes

- L'amélioration de la gestion d'erreurs vous donnera maintenant des messages plus clairs
- Si vous voyez le nouveau message d'erreur détaillé, suivez les étapes indiquées
- Le problème est généralement lié au réseau/firewall, pas au code de l'application
