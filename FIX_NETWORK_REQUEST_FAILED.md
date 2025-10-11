# 🔧 Fix "Network request failed" lors de la connexion

## ❌ Erreur rencontrée

```
Network request failed
```

Lors de la tentative de connexion avec email et mot de passe.

---

## 🎯 Diagnostic

✅ Backend est en cours d'exécution (port 8080)
✅ IP correcte dans la config : `192.168.1.16`
❌ Problème de connexion entre l'app et le backend

---

## 🔧 Solutions

### Solution 1 : Arrêter le tunnel et utiliser le mode LAN (⭐ Recommandé)

Le mode tunnel peut bloquer la connexion au backend local.

#### Étape 1 : Arrêter le serveur actuel

Appuyez sur `Ctrl+C` dans le terminal où Expo tourne.

#### Étape 2 : Relancer en mode LAN normal

```bash
cd MedVerifyApp\MedVerifyExpo
npx expo start --clear
```

#### Étape 3 : Vérifications importantes

**Sur votre téléphone :**

1. Assurez-vous d'être sur le **même WiFi** que votre PC
2. Scanner le QR code dans Expo Go
3. Essayer de vous connecter

---

### Solution 2 : Mettre à jour l'URL de l'API en mode tunnel

Si vous devez utiliser le tunnel, changez l'URL pour utiliser votre IP publique ou localhost.

Ouvrez `MedVerifyApp/MedVerifyExpo/src/config/constants.ts` et changez :

```typescript
export const API_CONFIG = {
  BASE_URL: "http://192.168.1.16:8080/api/v1", // IP locale
  TIMEOUT: 30000,
};
```

Par :

```typescript
export const API_CONFIG = {
  BASE_URL: "http://10.0.2.2:8080/api/v1", // Pour émulateur Android
  // OU
  // BASE_URL: 'http://[VOTRE_IP_PUBLIQUE]:8080/api/v1', // Si vous avez une IP publique
  TIMEOUT: 30000,
};
```

---

### Solution 3 : Vérifier que le backend accepte les connexions externes

Le backend Spring Boot doit être configuré pour accepter les connexions de toutes les interfaces.

Vérifiez dans `medverify-backend/src/main/resources/application.yml` :

```yaml
server:
  port: 8080
  address: 0.0.0.0 # Important : écoute sur toutes les interfaces
```

Si ce n'est pas le cas, ajoutez `address: 0.0.0.0` et redémarrez le backend.

---

### Solution 4 : Désactiver temporairement le pare-feu Windows

Le pare-feu peut bloquer les connexions entrantes sur le port 8080.

1. Ouvrir "Pare-feu Windows avec sécurité avancée"
2. Cliquer sur "Règles de trafic entrant"
3. Créer une nouvelle règle pour autoriser le port 8080

**OU** désactiver temporairement le pare-feu :

```powershell
# Exécuter en tant qu'administrateur
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled False
```

**⚠️ N'oubliez pas de réactiver après les tests !**

```powershell
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled True
```

---

### Solution 5 : Utiliser un émulateur Android

Si le téléphone physique pose toujours problème :

1. Installer Android Studio
2. Créer un AVD (Android Virtual Device)
3. Lancer l'émulateur
4. Dans le terminal Expo, appuyer sur `a`

**Avec un émulateur, utilisez cette URL :**

```typescript
export const API_CONFIG = {
  BASE_URL: "http://10.0.2.2:8080/api/v1", // IP spéciale pour émulateur
  TIMEOUT: 30000,
};
```

---

## 🚀 Commande recommandée MAINTENANT

### Arrêter le serveur tunnel actuel

1. Trouver le processus Expo en cours
2. L'arrêter avec `Ctrl+C`

### Relancer en mode LAN

```bash
cd MedVerifyApp\MedVerifyExpo
npx expo start --clear
```

### Tester la connexion backend

Dans un navigateur sur votre téléphone, aller à :

```
http://192.168.1.16:8080/api/v1/auth/test
```

Si vous voyez une réponse JSON, le backend est accessible.

---

## 🧪 Tester le backend depuis le PC

Pour vérifier que le backend fonctionne :

```bash
curl http://192.168.1.16:8080/api/v1/auth/test
```

Ou ouvrez dans votre navigateur.

---

## ✅ Checklist de diagnostic

- [ ] Backend en cours d'exécution (port 8080 ouvert)
- [ ] PC et téléphone sur le même WiFi
- [ ] IP correcte dans `constants.ts` (192.168.1.16)
- [ ] Mode LAN (pas tunnel) dans Expo
- [ ] Pare-feu Windows autorise le port 8080
- [ ] Backend configuré avec `address: 0.0.0.0`

---

## 📱 Test rapide

### Depuis votre téléphone

1. Ouvrir un navigateur web sur votre téléphone
2. Aller à : `http://192.168.1.16:8080`
3. Si ça charge, le problème vient de l'app
4. Si ça ne charge pas, le problème vient du réseau/pare-feu

---

## 🔍 Logs du backend

Pour voir les logs du backend en temps réel :

```bash
cd medverify-backend
tail -f logs/medverify.log
```

Cela vous permettra de voir si les requêtes arrivent au backend.

---

**Essayez la Solution 1 en premier et dites-moi si ça fonctionne ! 🚀**
