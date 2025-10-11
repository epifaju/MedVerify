# FIX: Erreur JSON Parse - "Unexpected end of input"

**Date:** 9 octobre 2025  
**Statut:** ✅ CORRIGÉ  
**Impact:** Critical (empêche chargement rapports/dashboard)

---

## 🐛 Problème Identifié

### Erreur:

```
ERROR Load reports error: [SyntaxError: JSON Parse error: Unexpected end of input]
```

### Cause Racine:

Le code appelait `await response.json()` **AVANT** de vérifier si la réponse HTTP était valide (`response.ok`).

**Scénario typique:**

1. Backend retourne erreur 401/404/500
2. Corps de réponse vide ou HTML d'erreur
3. `response.json()` essaie de parser du texte vide → **CRASH**

### Code Problématique (AVANT):

```typescript
const loadMyReports = async () => {
  try {
    const response = await fetch(`${API_URL}/reports/my-reports`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    const data = await response.json(); // ❌ CRASH si réponse vide/erreur
    if (response.ok) {
      setMyReports(data);
    }
  } catch (error) {
    console.error("Load reports error:", error);
  }
};
```

---

## ✅ Solution Implémentée

### Modifications:

1. **Vérifier `response.ok` AVANT de parser**
2. **Utiliser `response.text()` puis `JSON.parse()`** pour gérer réponses vides
3. **Afficher erreurs détaillées** (code HTTP + message)
4. **Gérer cas réponse vide** (aucun rapport = tableau vide)
5. **Ajouter messages utilisateur** via `Alert`

### Code Corrigé (APRÈS):

```typescript
const loadMyReports = async () => {
  try {
    setLoading(true);
    const response = await fetch(`${API_URL}/reports/my-reports`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    // ✅ 1. Vérifier d'abord le statut avant de parser le JSON
    if (!response.ok) {
      const errorText = await response.text();
      console.error("Reports API error:", response.status, errorText);
      Alert.alert(
        "Erreur",
        `Impossible de charger les signalements (${response.status})`
      );
      return;
    }

    // ✅ 2. Parser le JSON seulement si la réponse est OK
    const text = await response.text();
    if (!text) {
      console.log("No reports found - empty response");
      setMyReports([]);
      return;
    }

    // ✅ 3. Parser manuellement pour catch les erreurs
    const data = JSON.parse(text);
    setMyReports(data);
  } catch (error: any) {
    console.error("Load reports error:", error);
    Alert.alert(
      "Erreur",
      "Impossible de charger les signalements. Vérifiez votre connexion."
    );
  } finally {
    setLoading(false);
  }
};
```

---

## 🔍 Diagnostic

### Causes Possibles de l'Erreur:

| Cause                   | Symptôme                | Solution                                     |
| ----------------------- | ----------------------- | -------------------------------------------- |
| **Backend non démarré** | Timeout / Network error | Lancer `mvn spring-boot:run`                 |
| **URL incorrecte**      | 404 Not Found           | Vérifier `API_URL` dans App.tsx              |
| **Token expiré**        | 401 Unauthorized        | Re-login                                     |
| **CORS bloqué**         | Erreur CORS             | Vérifier `application.yml` CORS              |
| **Endpoint inexistant** | 404                     | Vérifier `/api/v1/reports/my-reports` existe |

### Comment Diagnostiquer:

1. **Vérifier backend démarré:**

   ```bash
   cd medverify-backend
   mvn spring-boot:run
   # Devrait afficher: Started MedVerifyApplication
   ```

2. **Tester endpoint manuellement:**

   ```bash
   # Obtenir un token d'abord
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@medverify.gw","password":"Admin@123456"}'

   # Utiliser le token
   curl -X GET http://localhost:8080/api/v1/reports/my-reports \
     -H "Authorization: Bearer VOTRE_TOKEN"
   ```

3. **Vérifier URL dans App.tsx:**

   ```typescript
   // ⚠️ localhost ne fonctionne PAS sur mobile physique!
   // Utilisez votre IP locale
   const API_URL = "http://192.168.1.16:8080/api/v1";
   ```

4. **Trouver votre IP locale:**

   ```bash
   # Windows
   ipconfig
   # Cherchez "IPv4 Address" dans votre WiFi

   # Mac/Linux
   ifconfig | grep "inet "
   ```

---

## 📝 Changements Appliqués

### Fichier: `MedVerifyApp/MedVerifyExpo/App.tsx`

#### Fonction 1: `loadMyReports()`

- ✅ Ajout vérification `response.ok`
- ✅ Utilisation `response.text()` + `JSON.parse()`
- ✅ Gestion réponse vide
- ✅ Alert utilisateur en cas d'erreur

#### Fonction 2: `loadDashboard()`

- ✅ Mêmes corrections que ci-dessus

#### Documentation URL:

- ✅ Ajout commentaires explicatifs sur API_URL

---

## 🧪 Comment Tester

### Test 1: Backend Démarré

1. **Démarrer backend:**

   ```bash
   cd medverify-backend
   mvn spring-boot:run
   ```

2. **Attendre message:**

   ```
   Started MedVerifyApplication in X.XXX seconds
   ```

3. **Relancer app Expo:**

   ```bash
   cd MedVerifyApp/MedVerifyExpo
   npx expo start
   ```

4. **Se connecter et aller dans "Signalements"**
   - ✅ Devrait afficher "Aucun signalement" ou liste
   - ❌ Plus d'erreur JSON Parse

### Test 2: Backend Arrêté

1. **Arrêter backend** (Ctrl+C)
2. **Essayer charger signalements**
3. **Résultat attendu:**
   - ⚠️ Alert: "Impossible de charger les signalements. Vérifiez votre connexion."
   - ⚠️ Console: "Load reports error: [TypeError: Network request failed]"

### Test 3: Token Expiré

1. **Attendre 15 min** (expiration access token)
2. **Essayer charger signalements**
3. **Résultat attendu:**
   - ⚠️ Alert: "Impossible de charger les signalements (401)"
   - ⚠️ Console: "Reports API error: 401 Unauthorized"

---

## 🎯 Messages d'Erreur Améliorés

### Avant (peu informatif):

```
ERROR Load reports error: [SyntaxError: JSON Parse error: Unexpected end of input]
```

### Après (détaillé):

```
// Erreur réseau
Load reports error: [TypeError: Network request failed]
Alert: "Impossible de charger les signalements. Vérifiez votre connexion."

// Erreur HTTP
Reports API error: 401 Unauthorized
Alert: "Impossible de charger les signalements (401)"

// Réponse vide (OK)
No reports found - empty response
// Affiche tableau vide sans erreur
```

---

## ✅ Checklist Vérification

- [x] ✅ Vérification `response.ok` avant parsing
- [x] ✅ Gestion réponse vide
- [x] ✅ Messages d'erreur détaillés
- [x] ✅ Alertes utilisateur
- [x] ✅ Content-Type header ajouté
- [x] ✅ Documentation URL améliorée
- [x] ✅ Même fix appliqué à `loadDashboard()`

---

## 🔄 Prochaines Étapes

### Recommandations Additionnelles:

1. **Créer un service API centralisé** (éviter duplication):

   ```typescript
   // src/services/ApiClient.ts
   export async function fetchAPI(endpoint: string, options = {}) {
     const response = await fetch(`${API_URL}${endpoint}`, {
       headers: {
         "Content-Type": "application/json",
         Authorization: `Bearer ${token}`,
         ...options.headers,
       },
       ...options,
     });

     if (!response.ok) {
       const errorText = await response.text();
       throw new Error(`API Error ${response.status}: ${errorText}`);
     }

     const text = await response.text();
     return text ? JSON.parse(text) : null;
   }
   ```

2. **Implémenter refresh token automatique** (401 → refresh)

3. **Ajouter retry logic** (erreurs réseau temporaires)

4. **Utiliser variables d'environnement** pour API_URL:
   ```bash
   # .env
   EXPO_PUBLIC_API_URL=http://192.168.1.16:8080/api/v1
   ```

---

## 📊 Impact

| Avant                           | Après                            |
| ------------------------------- | -------------------------------- |
| ❌ Crash app sur erreur backend | ✅ Message utilisateur clair     |
| ❌ Pas de logging détaillé      | ✅ Logs HTTP status + message    |
| ❌ Réponse vide = crash         | ✅ Réponse vide = tableau vide   |
| ❌ Erreur mystérieuse           | ✅ Indication exacte du problème |

**Résultat:** Expérience utilisateur améliorée + debugging facilité 🎉

---

**Développé par:** Assistant IA  
**Testé:** ✅ Backend démarré + arrêté  
**Documentation:** Mise à jour avec procédure debugging complète


