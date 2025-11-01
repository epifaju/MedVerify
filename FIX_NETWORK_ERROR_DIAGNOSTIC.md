# 🔧 Diagnostic Network Error - Solution Complète

## Problème

`ERR_NETWORK` avec `status: undefined` = La requête n'atteint pas le serveur

## Solutions par ordre de priorité

### Solution 1 : Port Forwarding avec ADB (Émulateur Android)

L'émulateur Android ne peut parfois pas accéder directement à `10.0.2.2`. Utilisez `adb reverse` :

```powershell
# Vérifier que l'émulateur est connecté
adb devices

# Rediriger le port 8080 de l'émulateur vers le PC
adb reverse tcp:8080 tcp:8080

# Vérifier que le port forwarding est actif
adb reverse --list
```

**Puis modifiez `constants.ts` pour utiliser `localhost` :**

```typescript
// Dans getApiBaseUrl(), pour Android émulateur avec adb reverse :
if (Platform.OS === CENTRO && __DEV__) {
  return "http://localhost:8080/api/v1"; // Utiliser localhost avec adb reverse
}
```

### Solution 2 : Vérifier que le backend est démarré

1. **Dans le terminal du backend, vous devriez voir :**

   ```
   Started MedVerifyApplication in X.XXX seconds
   ```

2. **Tester depuis le PC :**
   Ouvrez un navigateur et allez sur :

   ```
   http://localhost:8080/actuator/health
   ```

   Vous devriez voir : `{"status":"UP"}`

3. **Tester depuis l'émulateur Android :**
   Dans l'émulateur, ouvrez le navigateur et allez sur :
   ```
   http://10.0.2.2:8080/actuator/health
   ```
   Si ça ne fonctionne pas, utilisez la Solution 1 (adb reverse)

### Solution 3 : Utiliser l'IP du PC (Appareil physique)

Si vous utilisez un appareil physique (pas émulateur) :

1. **Trouver l'IP du PC :**

   ```powershell
   ipconfig
   ```

   Cherchez l'IPv4 (ex: `192.168.1.16`)

2. **Modifier `constants.ts` :**

   ```typescript
   // Pour appareil physique
   if (Platform.OS === "android" && __DEV__) {
     return "http://192.168.1.16:8080/api/v1"; // Remplacez par votre IP
   }
   ```

3. **Vérifier que le PC et le téléphone sont sur le même réseau WiFi**

### Solution 4 : Firewall Windows

Le firewall peut bloquer les connexions entrantes :

1. Ouvrez "Pare-feu Windows Defender"
2. Cliquez "Paramètres avancés"
3. Règles de trafic entrant → Nouvelle règle
4. Port → TCP → 8080 → Autoriser la connexion

OU temporairement désactiver le firewall pour tester.

### Solution 5 : Vérifier la configuration backend

Vérifiez dans `application.yml` que le serveur écoute bien sur toutes les interfaces :

```yaml
server:
  address: 0.0.0.0 # Important !
  port: 8080
```

## Ordre de test recommandé

1. ✅ **Vérifier backend démarré** → `http://localhost:8080/actuator/health`
2. ✅ **Essayer adb reverse** → `adb reverse tcp:8080 tcp:8080`
3. ✅ **Modifier constants.ts pour utiliser `localhost` avec adb reverse**
4. ✅ **Redémarrer l'app Expo**
5. ✅ **Tester à nouveau**

## Commande complète

```powershell
# 1. Vérifier émulateur connecté
adb devices

# 2. Configurer port forwarding
adb reverse tcp:8080 tcp:8080

# 3. Vérifier
adb reverse --list
```

Ensuite, dans `constants.ts`, utilisez `http://localhost:8080/api/v1` pour Android.




