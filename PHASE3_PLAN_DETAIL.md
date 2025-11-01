# 📋 Phase 3 : Tests Frontend & Documentation - Plan Détaillé

**Priorité** : 🟠 Moyenne  
**Temps estimé** : 3-4 heures

---

## 🎯 Objectif

Mettre en place les tests frontend, améliorer la gestion des erreurs avec Error Boundary, et compléter la documentation API.

---

## ✅ Améliorations Prévues

### 1. Setup Tests Frontend (1-2 heures)

**Temps estimé** : 1-2 heures

#### Configuration Jest + React Native Testing Library

**Fichiers à créer/modifier** :

1. **`MedVerifyApp/MedVerifyExpo/package.json`**
   - Ajouter dépendances de test :
     - `jest` (~29.x)
     - `jest-expo` (compatible Expo)
     - `@testing-library/react-native`
     - `@testing-library/jest-native`
     - `@testing-library/user-event`
     - `react-test-renderer`
     - `@types/jest`

2. **`MedVerifyApp/MedVerifyExpo/jest.config.js`** (nouveau)
   - Configuration Jest pour Expo
   - Setup files
   - Transformers
   - Module mappers
   - Coverage configuration

3. **`MedVerifyApp/MedVerifyExpo/jest.setup.js`** (nouveau)
   - Configuration globale des tests
   - Mock AsyncStorage
   - Mock NetInfo
   - Mock Navigation

4. **`MedVerifyApp/MedVerifyExpo/package.json` - Scripts**
   - Ajouter scripts :
     - `"test": "jest"`
     - `"test:watch": "jest --watch"`
     - `"test:coverage": "jest --coverage"`

#### Tests à Créer (Exemples)

**Fichiers de tests** :

1. **`MedVerifyApp/MedVerifyExpo/src/utils/__tests__/logger.test.ts`**
   - Test du logger utilitaire
   - Vérifier que les logs sont désactivés en production (`__DEV__ = false`)
   - Vérifier que `logError()` reste actif en production

2. **`MedVerifyApp/MedVerifyExpo/src/services/__tests__/ApiClient.test.ts`**
   - Test des interceptors
   - Test du retry logic
   - Test du refresh token automatique
   - Test de la gestion d'erreurs réseau

3. **`MedVerifyApp/MedVerifyExpo/src/components/__tests__/OfflineIndicator.test.tsx`**
   - Test du composant OfflineIndicator
   - Vérifier l'affichage selon l'état réseau

4. **`MedVerifyApp/MedVerifyExpo/src/screens/__tests__/LoginScreen.test.tsx`** (si existe)
   - Test du formulaire de login
   - Test de validation
   - Test de soumission

**Technologies** :
- Jest (test runner)
- React Native Testing Library (rendu et interactions)
- React Test Renderer (rendu shallow)

---

### 2. Error Boundary React (1 heure)

**Temps estimé** : 1 heure

#### Créer ErrorBoundary Component

**Fichier à créer** : `MedVerifyApp/MedVerifyExpo/src/components/ErrorBoundary.tsx`

**Fonctionnalités** :
- Intercepter les erreurs React (componentDidCatch)
- Afficher une UI d'erreur user-friendly
- Bouton "Réessayer" pour recharger l'app
- Option pour envoyer un rapport d'erreur (optionnel)
- Logging des erreurs critiques

**Intégration** :
- Wrapper dans `App.tsx` autour de l'app entière
- Possibilité d'ErrorBoundary par écran (optionnel)

**UI d'erreur** :
- Message d'erreur clair
- Bouton "Réessayer"
- Bouton "Retourner à l'accueil" (si navigation possible)
- Design cohérent avec le thème de l'app

#### Centraliser Gestion d'Erreurs API

**Fichier à modifier** : `MedVerifyApp/MedVerifyExpo/src/services/ApiClient.ts`

**Améliorations** :
- Intercepter toutes les erreurs non gérées
- Logger les erreurs critiques
- Option pour déclencher ErrorBoundary sur erreurs critiques
- Améliorer les messages d'erreur utilisateur

---

### 3. Documentation API (1-2 heures)

**Temps estimé** : 1-2 heures

#### Vérifier/Créer Configuration OpenAPI/Swagger

**Fichier à vérifier/créer** : Configuration Swagger dans le backend

**Fichiers à vérifier** :
- `medverify-backend/src/main/java/com/medverify/config/SwaggerConfig.java` (si existe)
- `application.yml` - Configuration Swagger

**Améliorations à apporter** :

1. **Annotations OpenAPI complètes** :
   - Ajouter `@Operation` sur tous les endpoints
   - Ajouter `@ApiResponse` pour tous les codes de réponse
   - Ajouter `@Schema` sur les DTOs
   - Ajouter exemples dans les DTOs

2. **Documentation des endpoints critiques** :
   - `/api/v1/auth/*` - Authentification
   - `/api/v1/medications/*` - Vérification médicaments
   - `/api/v1/pharmacies/*` - Pharmacies
   - `/api/v1/dashboard/*` - Dashboard (si existe)

3. **Configuration Swagger UI** :
   - Activer Swagger UI en dev
   - Configurer sécurité Swagger (JWT)
   - Ajouter descriptions des tags

4. **Documentation des schémas** :
   - DTOs avec descriptions
   - Exemples de requêtes/réponses
   - Codes d'erreur documentés

#### Créer Documentation API Markdown

**Fichier à créer** : `API_DOCUMENTATION.md`

**Contenu** :
- Overview de l'API
- Authentification (JWT)
- Endpoints principaux avec exemples
- Codes d'erreur
- Rate limiting
- Best practices

---

## 📊 Résumé Phase 3

### Temps Total Estimé
- **Setup tests frontend** : 1-2 heures
- **Error Boundary** : 1 heure
- **Documentation API** : 1-2 heures
- **Total** : ~3-4 heures

### Fichiers à Créer
- `jest.config.js`
- `jest.setup.js`
- `src/components/ErrorBoundary.tsx`
- `src/utils/__tests__/logger.test.ts`
- `src/services/__tests__/ApiClient.test.ts`
- `src/components/__tests__/OfflineIndicator.test.tsx`
- `API_DOCUMENTATION.md`
- Tests additionnels selon besoins

### Fichiers à Modifier
- `package.json` (ajout dépendances et scripts)
- `App.tsx` (intégration ErrorBoundary)
- `ApiClient.ts` (amélioration gestion erreurs)
- Controllers backend (annotations OpenAPI)
- DTOs backend (annotations @Schema)

### Bénéfices
- ✅ Tests frontend fonctionnels
- ✅ Gestion d'erreurs robuste
- ✅ Documentation API complète
- ✅ Meilleure maintenabilité
- ✅ Meilleure UX en cas d'erreur

---

## 🎯 Ordre d'Exécution Recommandé

1. **Error Boundary** (1h) - Impact immédiat sur UX
2. **Setup Tests Frontend** (1-2h) - Infrastructure de tests
3. **Documentation API** (1-2h) - Documentation pour développeurs

---

## ⚠️ Points d'Attention

### Setup Tests Frontend

**Dépendances à ajouter** :
- Jest compatible avec React Native 0.81.4
- React Native Testing Library compatible Expo
- Mocks pour AsyncStorage, NetInfo, Navigation

**Configuration** :
- Transformer pour TypeScript
- Mapper modules (assets, styles)
- Setup pour AsyncStorage mock

**Limitations** :
- Certains modules natifs peuvent nécessiter des mocks
- Tests d'intégration complets nécessiteront un émulateur

---

### Error Boundary

**Erreurs à intercepter** :
- Erreurs de rendu React
- Erreurs dans les composants
- Erreurs dans les hooks

**Erreurs non interceptées** :
- Erreurs dans les event handlers (try/catch manuel)
- Erreurs asynchrones (promises - nécessite .catch)
- Erreurs dans les callbacks (setTimeout, etc.)

**Recommandation** :
- Combiner ErrorBoundary avec gestion d'erreurs dans ApiClient
- Logger toutes les erreurs pour debugging

---

### Documentation API

**Annotations à ajouter** :
- `@Operation` avec summary et description
- `@ApiResponse` pour tous les codes HTTP
- `@Schema` avec exemple et description
- `@Parameter` pour path/query params
- `@Tag` pour organiser par domaine

**Swagger UI** :
- Accessible en dev uniquement (`/swagger-ui.html`)
- Sécurité JWT configurée pour tester endpoints authentifiés
- Exemples de requêtes pré-remplies

---

## ✅ Validation

Après Phase 3, vérifier :

1. **Tests frontend passent** :
```bash
cd MedVerifyApp/MedVerifyExpo
npm test
```

2. **Error Boundary fonctionne** :
- Tester en injectant une erreur dans un composant
- Vérifier que l'UI d'erreur s'affiche
- Vérifier que le bouton "Réessayer" fonctionne

3. **Documentation API accessible** :
```bash
# Backend démarré
curl http://localhost:8080/swagger-ui.html
# Ou
curl http://localhost:8080/v3/api-docs
```

4. **Annotations OpenAPI présentes** :
- Vérifier que tous les endpoints ont `@Operation`
- Vérifier que tous les DTOs ont `@Schema`
- Vérifier que les exemples sont présents

---

## 📝 Notes Techniques

### Tests Frontend

**Structure recommandée** :
```
src/
  components/
    __tests__/
      Component.test.tsx
  services/
    __tests__/
      Service.test.ts
  utils/
    __tests__/
      util.test.ts
```

**Coverage cible** :
- Utilitaires : 80%+
- Services : 70%+
- Composants critiques : 60%+
- Composants UI : 40%+

---

### Error Boundary

**Exemple d'utilisation** :
```tsx
<ErrorBoundary>
  <AppNavigator />
</ErrorBoundary>
```

**Erreurs critiques à logger** :
- Erreurs de rendu React
- Erreurs dans les composants racine
- Erreurs de navigation critiques

---

### Documentation API

**Exemple d'annotation** :
```java
@Operation(
    summary = "Verify medication authenticity",
    description = "Verifies a medication using GTIN and returns authenticity status"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Verification successful"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "400", description = "Invalid request")
})
```

---

**Voulez-vous que je commence la Phase 3 ?**

Confirmez et je commencerai par :
1. Error Boundary (impact UX immédiat)
2. Setup Tests Frontend (infrastructure)
3. Documentation API (documentation)

**Ou préférez-vous modifier l'ordre ou ajouter/retirer des éléments ?**

