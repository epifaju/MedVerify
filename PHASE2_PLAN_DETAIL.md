# 📋 Phase 2 : Qualité Code - Plan Détaillé

**Priorité** : 🟠 Moyenne  
**Temps estimé** : 3-4 heures

---

## 🎯 Objectif

Améliorer la qualité du code backend et frontend en ajoutant des tests, résolvant les TODOs, et optimisant la base de données.

---

## ✅ Améliorations Prévues

### 1. Tests Backend Additionnels

**Temps estimé** : 2-3 heures

#### Tests pour `MedicationVerificationService`

**Fichier à créer** : `medverify-backend/src/test/java/com/medverify/service/MedicationVerificationServiceTest.java`

**Tests à implémenter** (~10-15 tests) :

1. **Tests de vérification** :
   - `verify_ValidGtinInCache_ShouldReturnAuthentic` - GTIN trouvé en cache
   - `verify_ValidGtinFromApi_ShouldReturnAuthenticAndCache` - GTIN trouvé via API
   - `verify_InvalidGtin_ShouldReturnUnknown` - GTIN non trouvé
   - `verify_ExpiredCache_ShouldFetchFromApi` - Cache expiré

2. **Tests d'alertes** :
   - `verify_DuplicateSerialNumber_ShouldReturnSuspicious` - Serial dupliqué
   - `verify_ExpiredMedication_ShouldReturnAlert` - Médicament périmé
   - `verify_RecalledBatch_ShouldReturnCriticalAlert` - Lot rappelé
   - `verify_InactiveGtin_ShouldReturnHighSeverityAlert` - GTIN inactif

3. **Tests de fallback** :
   - `verify_ApiUnavailable_ShouldFallbackToLocalDb` - API indisponible
   - `verify_ApiError_ShouldFallbackGracefully` - Erreur API

**Technologies** :
- JUnit 5
- Mockito (pour mocker repositories et API client)
- AssertJ (assertions)

---

#### Tests d'Intégration pour Controllers

**Fichiers à créer** :
- `medverify-backend/src/test/java/com/medverify/controller/AuthControllerTest.java`
- `medverify-backend/src/test/java/com/medverify/controller/MedicationControllerTest.java`

**Tests à implémenter** (~8-10 tests) :

**AuthController** :
- `register_ValidRequest_ShouldReturn201`
- `register_DuplicateEmail_ShouldReturn409`
- `login_ValidCredentials_ShouldReturn200WithTokens`
- `login_InvalidCredentials_ShouldReturn401`
- `refresh_ValidToken_ShouldReturnNewAccessToken`

**MedicationController** :
- `verify_AuthenticatedUser_ShouldReturnVerificationResponse`
- `verify_Unauthenticated_ShouldReturn401`
- `getById_ValidId_ShouldReturnMedication`
- `search_ValidName_ShouldReturnList`

**Technologies** :
- `@WebMvcTest` pour tests de controllers
- `@WithMockUser` pour simuler authentification
- MockMvc pour requêtes HTTP

---

### 2. Résolution des TODOs

**Temps estimé** : 30 minutes

#### Backend : `CloudStorageService.java`

**TODOs à résoudre** :
```java
// TODO: Implémenter avec AWS SDK S3
// TODO: Implémenter suppression selon provider
```

**Actions** :
- Option A : Implémenter les méthodes avec AWS S3
- Option B : Créer des issues GitHub et documenter les limites actuelles
- Option C : Retirer les méthodes si non utilisées

**Recommandation** : Option B (documenter + issues GitHub)

---

#### Frontend : `DashboardScreen.tsx`

**Code commenté** :
```typescript
// Temporairement commenté pour debug
// import UserManagementScreen from '../UserManagementScreen';
```

**Actions** :
- Si fonctionnalité non utilisée : Supprimer le code commenté
- Si fonctionnalité à venir : Créer une issue GitHub
- Si fonctionnalité désactivée temporairement : Documenter pourquoi

**Recommandation** : Créer issue GitHub si fonctionnalité prévue

---

### 3. Index Composite DB

**Temps estimé** : 15 minutes

**Fichier** : Créer nouvelle migration Flyway

**Fichier à créer** : `medverify-backend/src/main/resources/db/migration/V12__add_composite_indexes.sql`

**Index à ajouter** :
```sql
-- Index composite pour requêtes fréquentes sur scan_history
CREATE INDEX idx_scan_gtin_status_date 
ON scan_history(gtin, status, scanned_at DESC);

-- Index pour détection de doublons (serial + gtin)
CREATE INDEX idx_scan_serial_gtin 
ON scan_history(serial_number, gtin) 
WHERE serial_number IS NOT NULL;

-- Index composite pour recherches de lots rappelés
CREATE INDEX idx_batch_medication_recalled 
ON medication_batches(medication_id, is_recalled, expiry_date DESC) 
WHERE is_recalled = TRUE;
```

**Impact** : 
- ⚡ Performance améliorée pour requêtes de statistiques
- ⚡ Recherche de doublons plus rapide
- ⚡ Requêtes dashboard plus rapides

---

## 📊 Résumé Phase 2

### Temps Total Estimé
- **Tests MedicationVerificationService** : 2 heures
- **Tests Controllers** : 1 heure
- **Résolution TODOs** : 30 minutes
- **Index Composite DB** : 15 minutes
- **Total** : ~4 heures

### Fichiers à Créer
- `MedicationVerificationServiceTest.java`
- `AuthControllerTest.java`
- `MedicationControllerTest.java`
- `V12__add_composite_indexes.sql`

### Fichiers à Modifier
- `CloudStorageService.java` (documentation ou implémentation)
- `DashboardScreen.tsx` (nettoyage code commenté)

### Bénéfices
- ✅ Couverture de tests augmentée (~30-40% → ~60-70%)
- ✅ Performance DB améliorée (requêtes plus rapides)
- ✅ Code plus propre (TODOs résolus)
- ✅ Maintenabilité améliorée

---

## 🎯 Ordre d'Exécution Recommandé

1. **Index Composite DB** (15 min) - Impact immédiat sur performance
2. **Résolution TODOs** (30 min) - Nettoyage rapide
3. **Tests MedicationVerificationService** (2h) - Service critique
4. **Tests Controllers** (1h) - Validation endpoints API

---

## ⚠️ Points d'Attention

### Tests MedicationVerificationService

**Dépendances à mocker** :
- `MedicationRepository`
- `MedicationBatchRepository`
- `ScanHistoryRepository`
- `ApiMedicamentsClient`
- `MeterRegistry` (Prometheus)

**Cas complexes à tester** :
- Logique de cache (expiration, mise à jour)
- Fallback API → DB locale
- Calcul du score de confiance
- Détection de doublons

---

### Tests Controllers

**Configuration requise** :
- Mock `JwtAuthenticationFilter` ou utiliser `@WithMockUser`
- Simuler les requêtes HTTP avec MockMvc
- Tester les codes de réponse et formats JSON

**Endpoints à tester en priorité** :
- `/api/v1/auth/login` (sécurité critique)
- `/api/v1/medications/verify` (fonctionnalité principale)

---

## ✅ Validation

Après Phase 2, vérifier :

1. **Tests passent** :
```bash
cd medverify-backend
mvn test
# Tous les tests doivent passer
```

2. **Couverture de code** :
```bash
mvn test jacoco:report
# Ouvrir target/site/jacoco/index.html
# Vérifier coverage > 60% pour services critiques
```

3. **Migration DB appliquée** :
```bash
# Vérifier dans logs Flyway :
# Successfully applied 1 migration to schema "public"
```

4. **TODOs résolus** :
```bash
git grep -i "TODO\|FIXME" -- "*.java" "*.ts" "*.tsx"
# Devrait retourner 0 ou très peu de résultats
```

---

**Voulez-vous que je commence la Phase 2 ?**

Confirmez et je commencerai par :
1. Index Composite DB (rapide, impact immédiat)
2. Résolution TODOs (nettoyage)
3. Tests backend (qualité)

