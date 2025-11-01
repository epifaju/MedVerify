# 🧪 Guide de Test - Phase 1 Améliorations

**Date** : 2025-11-01

---

## ✅ Objectif

Vérifier que les améliorations de la Phase 1 fonctionnent correctement :
1. Logs DEBUG désactivés en production (Backend)
2. Logs console désactivés en production (Frontend)
3. CORS configuré différemment selon l'environnement

---

## 🔍 Tests Backend

### Test 1 : Logs en Production

**Commande** :
```bash
cd medverify-backend
mvn spring-boot:run -Dspring.profiles.active=prod
```

**Vérifications** :
- ✅ Les logs ne doivent **PAS** contenir :
  - `DEBUG` pour `com.medverify`
  - `DEBUG` pour `org.springframework.security`
  - `DEBUG` pour `org.hibernate.SQL`
  - `TRACE` pour `org.hibernate.type.descriptor.sql.BasicBinder`
- ✅ Les logs doivent contenir :
  - `INFO` pour `com.medverify`
  - `WARN` pour Hibernate/Spring Security

**Résultat attendu** :
```
2025-11-01 12:09:32 - JWT configuration validated successfully. Secret length: 64 characters
2025-11-01 12:09:32 - HikariPool-1 - Starting...
...
Started MedVerifyApplication in X.XXX seconds
```

**Pas de** :
```
DEBUG - ...
TRACE - ...
```

---

### Test 2 : Logs en Développement

**Commande** :
```bash
cd medverify-backend
mvn spring-boot:run -Dspring.profiles.active=dev
```

**Vérifications** :
- ✅ Les logs **DOIVENT** contenir :
  - `DEBUG` pour `com.medverify`
  - `DEBUG` pour `org.springframework.security`
  - `DEBUG` pour `org.hibernate.SQL`
  - `TRACE` pour `org.hibernate.type.descriptor.sql.BasicBinder`

**Résultat attendu** :
```
DEBUG - User test@example.com authenticated successfully
DEBUG - Searching by CIP13: ...
TRACE - binding parameter ...
```

---

### Test 3 : CORS en Production

**Test avec curl** :
```bash
# Depuis une origine non autorisée (devrait échouer)
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://localhost:8080/api/v1/pharmacies
```

**Avec profil prod** :
- ✅ Si origine = `https://medverify.gw` → doit réussir
- ❌ Si origine = `http://localhost:3000` → doit échouer (CORS)

**Vérification dans les logs** :
```
Access-Control-Allow-Origin: https://medverify.gw
# PAS de localhost
```

---

### Test 4 : CORS en Développement

**Avec profil dev** :
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

**Test avec curl** :
```bash
# Depuis localhost (devrait réussir)
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     http://localhost:8080/api/v1/pharmacies
```

**Vérifications** :
- ✅ `http://localhost:3000` doit être autorisé
- ✅ `http://localhost:19006` doit être autorisé
- ❌ IPs comme `http://192.168.1.16:8080` ne doivent **PAS** être autorisées (profil dev)

---

### Test 5 : CORS en Local (avec téléphone)

**Avec profil local** :
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

**Vérifications** :
- ✅ `*` doit être autorisé (toutes les origines)
- ✅ IPs locales doivent être autorisées
- ✅ Émulaturs (`http://10.0.2.2:8080`) doivent être autorisés

---

## 📱 Tests Frontend

### Test 6 : Logs Console en Développement

**Mode développement** (`__DEV__ = true`) :

1. Lancer l'app en mode développement :
```bash
cd MedVerifyApp/MedVerifyExpo
npm start
```

2. Ouvrir la console React Native Debugger ou Chrome DevTools

3. **Vérifications** :
- ✅ Les logs doivent apparaître :
  - `🌐 ApiClient initialized with BASE_URL: ...`
  - `📤 API Request: ...`
  - `✅ API Response: ...`
  - `🏥 PharmaciesScreen monté - Initialisation...`
  - Etc.

**Résultat attendu** : Logs visibles dans la console

---

### Test 7 : Logs Console en Production

**Mode production** (`__DEV__ = false`) :

1. Build production :
```bash
# Android
npx expo build:android --type apk

# iOS
npx expo build:ios
```

2. Installer l'APK/IPA sur un appareil

3. **Vérifications** :
- ❌ Les logs suivants ne doivent **PAS** apparaître :
  - `log()` → aucun log
  - `logWarn()` → aucun log
  - `logDebug()` → aucun log
- ✅ Seuls les `logError()` doivent apparaître (en cas d'erreur réelle)

**Test manuel** :
- Naviguer dans l'app
- Faire des requêtes API
- Scanner des codes
- **Résultat** : Console vide (sauf erreurs critiques)

---

### Test 8 : Fonctionnalité Logger

**Test du logger utilitaire** :

1. Créer un test rapide :
```typescript
// Dans un composant de test
import { log, logWarn, logError, logDebug } from '../utils/logger';

log('Test log normal');        // Devrait apparaître en dev, pas en prod
logWarn('Test warning');       // Devrait apparaître en dev, pas en prod
logError('Test error');        // Devrait TOUJOURS apparaître
logDebug('Test debug');        // Devrait apparaître en dev, pas en prod
```

2. **Vérifications** :
- En dev (`__DEV__ = true`) : Tous les logs apparaissent
- En prod (`__DEV__ = false`) : Seul `logError()` apparaît

---

## 🧪 Tests d'Intégration

### Test 9 : Backend + Frontend Ensemble

**Scenario 1 : Développement**
1. Backend : `mvn spring-boot:run -Dspring.profiles.active=dev`
2. Frontend : `npm start` (mode dev)
3. **Vérifier** :
   - ✅ Connexion réussie
   - ✅ Requêtes API fonctionnent
   - ✅ Logs visibles dans console
   - ✅ CORS accepte localhost

**Scenario 2 : Production**
1. Backend : `mvn spring-boot:run -Dspring.profiles.active=prod`
2. Frontend : Build production (`__DEV__ = false`)
3. **Vérifier** :
   - ✅ Connexion réussie
   - ✅ Requêtes API fonctionnent
   - ❌ Aucun log DEBUG visible
   - ❌ Aucun log console visible
   - ✅ CORS n'accepte que domaines réels

---

## ✅ Checklist de Vérification

### Backend
- [ ] Profil prod : Aucun log DEBUG
- [ ] Profil dev : Logs DEBUG visibles
- [ ] Profil prod : CORS restreint
- [ ] Profil dev : CORS localhost autorisé
- [ ] Profil local : CORS IPs locales autorisées

### Frontend
- [ ] Mode dev : Logs console visibles
- [ ] Mode prod : Logs console absents (sauf erreurs)
- [ ] Logger utilitaire fonctionne
- [ ] Application fonctionne normalement

### Intégration
- [ ] Dev + Dev : Fonctionne
- [ ] Prod + Prod : Fonctionne
- [ ] Pas de régression fonctionnelle

---

## 🐛 Problèmes Potentiels

### Problème 1 : Backend ne démarre pas en prod

**Symptôme** : Erreur au démarrage

**Solution** :
- Vérifier que `JWT_SECRET` est configuré
- Vérifier que `DB_PASSWORD` est configuré
- Vérifier les logs pour identifier le problème

---

### Problème 2 : Frontend ne peut pas se connecter

**Symptôme** : Erreur CORS ou Network Error

**Solution** :
- Vérifier le profil Spring actif
- Vérifier que l'origine frontend est autorisée
- Utiliser profil `local` si test avec téléphone

---

### Problème 3 : Logs toujours visibles en production

**Symptôme** : Logs DEBUG apparaissent même avec profil prod

**Solution** :
- Vérifier que le profil est bien actif : `spring.profiles.active=prod`
- Vérifier dans les logs : `The following 1 profile is active: "prod"`
- Redémarrer l'application

---

## 📊 Résultats Attendus

Après tous les tests :

✅ **Backend Production** :
- Logs : INFO/WARN uniquement
- CORS : Domaines réels uniquement
- Performance : Pas de logs verbeux

✅ **Frontend Production** :
- Console : Vide (sauf erreurs critiques)
- Performance : Pas de logs verbeux
- Fonctionnalité : Normale

✅ **Sécurité** :
- Pas d'informations sensibles dans les logs
- CORS restreint en production
- IPs locales uniquement pour dev

---

**Temps estimé pour tous les tests** : 30-45 minutes

