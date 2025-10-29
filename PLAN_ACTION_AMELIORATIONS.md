# 🎯 Plan d'Action - Améliorations MedVerify

**Date** : 15 Octobre 2025  
**Version** : 1.0

---

## 🔴 PHASE 1 : CORRECTIONS CRITIQUES (URGENT - 1-2 jours)

### ✅ Action 1.1 : Sécuriser les Credentials (URGENT)

**Problème** : Mot de passe DB et JWT secret en clair dans `application.yml`

**Solution** :

1. **Créer/Mettre à jour `application-local.yml`** (déjà gitignored)

   ```yaml
   spring:
     datasource:
       password: Malagueta7

   jwt:
     secret: ${JWT_SECRET}
   ```

2. **Nettoyer `application.yml`**

   ```yaml
   spring:
     datasource:
       password: ${DB_PASSWORD:}

   jwt:
     secret: ${JWT_SECRET:}
   ```

3. **Générer JWT secret fort**

   ```bash
   # Linux/Mac
   openssl rand -base64 32

   # Windows PowerShell
   -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
   ```

4. **Créer script `.env.local` ou `set-env-local.ps1`**
   ```powershell
   $env:DB_PASSWORD="Malagueta7"
   $env:JWT_SECRET="<GENERATED_SECRET>"
   ```

**Fichiers à modifier** :

- `medverify-backend/src/main/resources/application.yml`
- `medverify-backend/src/main/resources/application-local.yml` (créer si n'existe pas)

**Priorité** : ⚠️ **P0 - CRITIQUE**

---

### ✅ Action 1.2 : Corriger Bug PharmacyController

**Problème** : Variable `pharmacies` peut être non initialisée

**Fichier** : `medverify-backend/src/main/java/com/medverify/controller/PharmacyController.java`

**Correction** :

```java
List<PharmacyDTO> pharmacies = new ArrayList<>(); // Initialiser au début
```

**Priorité** : ⚠️ **P0 - CRITIQUE**

---

### ✅ Action 1.3 : Restreindre CORS

**Problème** : `@CrossOrigin(origins = "*")` accepte toutes origines

**Fichier** : `medverify-backend/src/main/java/com/medverify/controller/PharmacyController.java`

**Correction** :

```java
// Supprimer @CrossOrigin si SecurityConfig gère déjà CORS
// Ou utiliser :
@CrossOrigin(origins = "${cors.allowed-origins}")
```

**Vérifier** : `SecurityConfig.java` gère déjà CORS globalement

**Priorité** : ⚠️ **P0 - CRITIQUE**

---

### ✅ Action 1.4 : Ajuster Logging Production

**Problème** : Logging DEBUG en production

**Fichier** : `medverify-backend/src/main/resources/application.yml`

**Correction** :

```yaml
---
# Profile: Production
spring:
  config:
    activate:
      on-profile: prod
  jpa:
    show-sql: false
logging:
  level:
    root: INFO
    com.medverify: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN # Pas DEBUG en prod
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
```

**Priorité** : ⚠️ **P1 - IMPORTANT**

---

## 🟡 PHASE 2 : AMÉLIORATIONS QUALITÉ (1 semaine)

### ✅ Action 2.1 : Ajouter Tests Unitaires

**Objectif** : Atteindre 40% coverage minimum

**Tests à créer** :

1. **AuthServiceTest.java**

   - testRegister_Success
   - testRegister_DuplicateEmail
   - testLogin_Success
   - testLogin_InvalidCredentials
   - testLogin_LockedAccount
   - testRefreshToken_Success
   - testRefreshToken_Expired

2. **MedicationVerificationServiceTest.java**

   - testVerify_Authentic
   - testVerify_Suspicious_DuplicateSerial
   - testVerify_Suspicious_Expired
   - testVerify_Suspicious_RecalledBatch
   - testVerify_Unknown_GtinNotFound
   - testVerify_WithLocation

3. **EmailVerificationServiceTest.java**
   - testGenerateCode
   - testVerifyCode_Success
   - testVerifyCode_Expired
   - testVerifyCode_Invalid
   - testCleanExpiredCodes

**Emplacement** : `medverify-backend/src/test/java/com/medverify/service/`

**Priorité** : ⚠️ **P1 - IMPORTANT**

---

### ✅ Action 2.2 : Organiser Documentation

**Problème** : 250+ fichiers MD dans racine, désorganisés

**Solution** :

1. **Créer structure** :

   ```
   docs/
   ├── architecture/
   │   ├── ARCHITECTURE.md
   │   └── DATABASE_SCHEMA.md
   ├── guides/
   │   ├── INSTALLATION.md
   │   ├── CONFIGURATION.md
   │   └── DEPLOYMENT.md
   ├── api/
   │   └── API_REFERENCE.md
   └── changelog/
       └── CHANGELOG.md
   ```

2. **Déplacer fichiers existants**

   - Guides d'installation → `docs/guides/`
   - Analyses → `docs/analysis/`
   - Fix documents → `docs/fixes/` (ou supprimer si obsolètes)

3. **Créer `README.md` principal** avec index vers docs

4. **Supprimer doublons** et fichiers obsolètes

**Priorité** : ⚠️ **P2 - MOYENNE**

---

### ✅ Action 2.3 : Configurer CI/CD Basique

**Objectif** : Pipeline GitHub Actions pour build + tests

**Fichier à créer** : `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgis/postgis:14-3.2
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: medverify_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Run tests
        run: mvn clean test
        env:
          DB_PASSWORD: postgres
          JWT_SECRET: test-secret-key-for-ci-only

      - name: Generate coverage report
        run: mvn jacoco:report

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./medverify-backend/target/site/jacoco/jacoco.xml
```

**Priorité** : ⚠️ **P1 - IMPORTANT**

---

## 🟢 PHASE 3 : AMÉLIORATIONS AVANCÉES (2-3 semaines)

### ✅ Action 3.1 : Configuration Environnement Mobile

**Fichier** : `MedVerifyApp/MedVerifyExpo/app.config.js`

```javascript
export default {
  expo: {
    extra: {
      apiUrl: process.env.EXPO_PUBLIC_API_URL || "http://localhost:8080/api/v1",
      env: process.env.EXPO_PUBLIC_ENV || "development",
    },
  },
};
```

**Mettre à jour** : `src/config/constants.ts`

```typescript
export const API_CONFIG = {
  BASE_URL: process.env.EXPO_PUBLIC_API_URL || "http://localhost:8080/api/v1",
  TIMEOUT: 30000,
};
```

---

### ✅ Action 3.2 : Ajouter Error Boundary

**Fichier** : `MedVerifyApp/MedVerifyExpo/src/components/ErrorBoundary.tsx`

```typescript
import React from "react";
import { View, Text, Button } from "react-native";

class ErrorBoundary extends React.Component {
  state = { hasError: false };

  static getDerivedStateFromError(error: Error) {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error("Error caught by boundary:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <View>
          <Text>Une erreur est survenue</Text>
          <Button
            title="Réessayer"
            onPress={() => this.setState({ hasError: false })}
          />
        </View>
      );
    }
    return this.props.children;
  }
}
```

---

### ✅ Action 3.3 : Ajouter Tests Frontend

**Configuration Jest** : `MedVerifyApp/MedVerifyExpo/jest.config.js`

**Tests à créer** :

- `AuthService.test.ts`
- `gs1Parser.test.ts`
- `ScanService.test.ts`

---

### ✅ Action 3.4 : Optimisations Performance

1. **Memoization composants**

   ```typescript
   export default React.memo(PharmaciesScreen);
   ```

2. **Lazy loading images**

   ```typescript
   import { Image } from "expo-image";
   ```

3. **useMemo pour calculs coûteux**
   ```typescript
   const filteredPharmacies = useMemo(() => {
     return pharmacies.filter(/* ... */);
   }, [pharmacies, filters]);
   ```

---

## 📋 CHECKLIST GÉNÉRALE

### Phase 1 (Critique) - 1-2 jours

- [ ] 1.1 Sécuriser credentials
- [ ] 1.2 Corriger bug PharmacyController
- [ ] 1.3 Restreindre CORS
- [ ] 1.4 Ajuster logging production
- [ ] Vérifier application démarre correctement
- [ ] Tests manuels authentification
- [ ] Tests manuels vérification médicaments

### Phase 2 (Qualité) - 1 semaine

- [ ] 2.1 Tests unitaires AuthService (7 tests)
- [ ] 2.2 Tests unitaires MedicationVerificationService (10 tests)
- [ ] 2.3 Tests unitaires EmailVerificationService (5 tests)
- [ ] 2.4 Organiser documentation
- [ ] 2.5 CI/CD pipeline
- [ ] Vérifier coverage > 40%

### Phase 3 (Avancé) - 2-3 semaines

- [ ] 3.1 Configuration environnement mobile
- [ ] 3.2 Error boundaries
- [ ] 3.3 Tests frontend
- [ ] 3.4 Optimisations performance
- [ ] 3.5 Cache Redis
- [ ] 3.6 Pagination endpoints

---

## 🎯 CRITÈRES DE RÉUSSITE

### Phase 1

✅ Aucun credential en clair  
✅ Build sans erreurs  
✅ Tests manuels passent

### Phase 2

✅ Coverage tests > 40%  
✅ CI/CD pipeline fonctionnel  
✅ Documentation organisée

### Phase 3

✅ Performance optimisée  
✅ Tests E2E passent  
✅ Prêt pour production

---

**Prochaine révision** : Après Phase 1 complétée
