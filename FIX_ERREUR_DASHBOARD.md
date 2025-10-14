# 🔧 Fix Erreur Dashboard

## 🐛 Erreur Rencontrée

```
ERROR  Load dashboard error: [Error: Erreur lors de la récupération des statistiques]
```

Cette erreur se produit quand vous cliquez sur l'onglet Dashboard.

---

## 🔍 Causes Possibles

### 1. ❌ Backend non démarré ou inaccessible

Le dashboard fait appel à l'API backend :

```
GET http://192.168.1.16:8080/api/v1/admin/dashboard/stats?period=30d
```

**Solution** : Vérifier que le backend est bien démarré.

### 2. ❌ Permissions insuffisantes

L'endpoint `/admin/dashboard/stats` nécessite un rôle **AUTHORITY** ou **ADMIN**.

**Solution** : Vérifier le rôle de l'utilisateur connecté.

### 3. ❌ Token JWT expiré

Le token d'authentification peut être expiré.

**Solution** : Se reconnecter.

### 4. ❌ URL de l'API incorrecte

L'URL configurée ne correspond pas à votre serveur backend.

**Solution** : Vérifier l'URL dans `constants.ts`.

---

## ✅ Solutions

### Solution 1 : Vérifier le Backend

**Étape 1 - Démarrer le backend** :

```bash
cd medverify-backend
mvn spring-boot:run
```

**Étape 2 - Vérifier que le backend répond** :

```bash
curl http://localhost:8080/api/v1/public/health
```

Si ça ne fonctionne pas, le backend n'est pas démarré.

---

### Solution 2 : Vérifier l'URL de l'API

**Fichier** : `MedVerifyApp/MedVerifyExpo/src/config/constants.ts`

```typescript
export const API_CONFIG = {
  BASE_URL: "http://192.168.1.16:8080/api/v1", // ← Vérifier cette URL
  TIMEOUT: 30000,
};
```

**Actions** :

1. Vérifier que `192.168.1.16` est bien l'IP de votre ordinateur
2. Sur Windows, trouver votre IP locale :

   ```powershell
   ipconfig
   ```

   Chercher "Adresse IPv4" dans la section Wi-Fi ou Ethernet

3. Si l'IP a changé, mettre à jour `BASE_URL`

**Exemple** :

```typescript
// Si votre nouvelle IP est 192.168.1.25
BASE_URL: "http://192.168.1.25:8080/api/v1";
```

---

### Solution 3 : Vérifier le Rôle de l'Utilisateur

Le dashboard est **réservé aux AUTHORITY et ADMIN**.

**Vérifier votre rôle** :

1. Aller dans l'onglet **Profil**
2. Regarder le rôle affiché

**Si vous êtes "Patient" ou "Pharmacien"** :

- Vous n'avez pas accès au dashboard
- Créer un compte ADMIN ou AUTHORITY

**Créer un compte ADMIN via SQL** :

```sql
-- Se connecter à PostgreSQL
psql -U postgres -d medverify

-- Vérifier les utilisateurs
SELECT email, role FROM users;

-- Changer le rôle d'un utilisateur existant
UPDATE users
SET role = 'ADMIN', is_verified = true, is_active = true
WHERE email = 'votre.email@test.com';
```

---

### Solution 4 : Améliorer le Diagnostic

**J'ai ajouté des logs détaillés** dans le DashboardScreen.

Maintenant, quand vous cliquez sur Dashboard, regardez les logs dans la console pour voir :

```
Load dashboard error: ...
Error details: ...
Error response: ...
```

Cela vous donnera plus d'informations sur la cause exacte.

---

## 🧪 Test de l'API

### Test avec curl

Pour vérifier que l'endpoint fonctionne :

```bash
# D'abord se connecter et récupérer le token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@medverify.com","password":"Admin123!"}'

# Copier le accessToken reçu

# Tester l'endpoint dashboard
curl -X GET "http://localhost:8080/api/v1/admin/dashboard/stats?period=30d" \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

**Résultat attendu** : Un JSON avec les statistiques
**Si erreur 401** : Token invalide ou rôle insuffisant
**Si erreur 403** : Permissions insuffisantes
**Si erreur 404** : Endpoint non trouvé

---

## 🎯 Corrections Appliquées

### ✅ 1. Traductions Dashboard

J'ai ajouté les traductions pour le Dashboard dans les 3 langues :

```typescript
// Français
dashboard_authority_title: 'Dashboard Autorités',
dashboard_load_stats: 'Charger les Statistiques',
dashboard_total_scans: 'Total Scans',
dashboard_authenticity: 'Authenticité',
dashboard_reports: 'Signalements',
dashboard_user_management: 'Gestion des utilisateurs',
dashboard_refresh: 'Actualiser',
dashboard_error_load: 'Impossible de charger les statistiques',
```

### ✅ 2. Amélioration Gestion d'Erreur

Le Dashboard affiche maintenant une **alerte détaillée** en cas d'erreur, avec le message exact du serveur.

### ✅ 3. Logs Détaillés

Ajout de logs pour diagnostiquer :

```typescript
console.error("Load dashboard error:", error);
console.error("Error details:", error.message);
console.error("Error response:", error.response?.data);
```

---

## 🚀 Prochaines Étapes

1. **Vérifier le backend** :

   ```bash
   cd medverify-backend
   mvn spring-boot:run
   ```

2. **Vérifier l'URL dans constants.ts** (adapter à votre IP locale)

3. **Vérifier votre rôle** (doit être ADMIN ou AUTHORITY)

4. **Relancer l'app mobile** :

   ```bash
   cd MedVerifyApp/MedVerifyExpo
   npm start
   ```

5. **Regarder les logs** dans la console pour plus de détails

---

## 📝 Informations Supplémentaires

### Endpoint Backend

L'endpoint utilisé :

```
GET /api/v1/admin/dashboard/stats?period=30d
```

**Permissions requises** :

- Rôle : `ADMIN` ou `AUTHORITY`
- Token JWT valide

**Réponse attendue** :

```json
{
  "kpis": {
    "totalScans": 150,
    "authenticityRate": 92.5,
    "totalReports": 12,
    "suspiciousMedications": 8,
    "activeUsers": 45,
    "newUsers": 5
  },
  "trends": {
    "scansGrowth": 15.2,
    "reportsGrowth": 3.5
  },
  "recentAlerts": []
}
```

---

## 💡 Conseil

Si le problème persiste, partagez les **logs complets** de la console pour que je puisse vous aider à diagnostiquer le problème exact.

Les logs devraient maintenant afficher :

- Le message d'erreur
- Les détails de l'erreur
- La réponse du serveur (si disponible)


