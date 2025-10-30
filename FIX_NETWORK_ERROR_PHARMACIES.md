# 🔧 Résolution : Network Error sur l'onglet Pharmacies

## Problème

Erreur `Network Error` quand on clique sur l'onglet Pharmacies dans l'app mobile.

## Causes possibles

1. ✅ **Backend non démarré** - L'application Spring Boot n'est pas en cours d'exécution
2. ✅ **URL API incorrecte** - L'URL dans le frontend ne correspond pas à l'environnement (émulateur vs appareil)
3. ✅ **CORS bloqué** - Les requêtes sont bloquées par la configuration CORS

## Solutions

### Étape 1 : Vérifier que le backend est démarré

Le backend doit être démarré sur le port 8080 :

```powershell
cd赚 medverify-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Vous devriez voir :

```
Started MedVerifyApplication in X.XXX seconds
```

### Étape 2 : Vérifier l'URL de l'API dans l'app

J'ai mis à jour `MedVerifyApp/MedVerifyExpo/src/config/constants.ts` pour utiliser automatiquement :

- **Android émulateur** : `http://10.0.2.2:8080/api/v1`
- **iOS Simulator** : `http://localhost:8080/api/v1`
- **Appareil physique** : Nécessite l'IP du PC (ex: `http://192.168.1.16:8080/api/v1`)

Vérifiez dans les logs de l'app Expo que l'URL correcte est utilisée :

```
🌐 API Base URL: http://10.0.2.2:8080/api/v1
📱 Platform: android
```

### Étape 3 : Si vous utilisez un appareil physique (pas émulateur)

1. **Trouver l'IP de votre PC :**

   ```powershell
   ipconfig
   ```

   Cherchez l'adresse IPv4 (ex: `192.168.1.16`)

2. **Modifier temporairement `constants.ts` :**

   ```typescript
   return "http://192.168.1.16:8080/api/v1"; // Remplacez par votre IP
   ```

3. **S'assurer que le PC et l'appareil sont sur le même réseau WiFi**

### Étape 4 : Tester la connexion

Vous pouvez tester si le backend répond en ouvrant dans un navigateur :

- `http://localhost:8080/actuator/health` (depuis le PC)
- `http://10.0.2.2:8080/actuator/health` (depuis l'émulateur Android)

### Étape 5 : Vérifier les logs du backend

Quand vous cliquez sur l'onglet Pharmacies, vous devriez voir dans les logs du backend :

```
POST /api/v1/pharmacies/search
```

Si vous ne voyez rien, c'est que la requête n'arrive pas au backend.

## Configuration CORS

J'ai mis à jour `SecurityConfig.java` pour mieux gérer les requêtes React Native.

## Redémarrer l'app Expo

Après avoir modifié `constants.ts`, vous devez redémarrer l'app Expo :

1. Arrêter l'app (Ctrl+C)
2. Relancer : `npm start` ou `expo start`
3. Recharger l'app sur l'émulateur/appare masyarakatil


