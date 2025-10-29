# 🔧 FIX - Erreur URL dupliquée Pharmacies

## ❌ Problème

Quand on clique sur l'onglet "Pharmacies", erreur :

```
No static resource api/v1/api/v1/pharmacies/search.
```

**URL incorrecte :** `/api/v1/api/v1/pharmacies/search` (double `/api/v1`)  
**URL correcte :** `/api/v1/pharmacies/search`

## 🔍 Cause

La `BASE_URL` dans `constants.ts` contient déjà `/api/v1` :

```typescript
BASE_URL: "http://192.168.1.16:8080/api/v1";
```

Et dans `PharmacyService.ts`, on rajoutait encore `/api/v1` :

```typescript
❌ apiClient.post('/api/v1/pharmacies/search', ...)
// Résultat : http://192.168.1.16:8080/api/v1/api/v1/pharmacies/search
```

## ✅ Solution appliquée

**Correction dans `PharmacyService.ts` :**

```typescript
// Avant
❌ '/api/v1/pharmacies/search'
❌ '/api/v1/pharmacies/search-by-city'
❌ '/api/v1/pharmacies/${id}'

// Après
✅ '/pharmacies/search'
✅ '/pharmacies/search-by-city'
✅ '/pharmacies/${id}'
```

## 🚀 Relancer l'application

```bash
cd MedVerifyApp/MedVerifyExpo

# Reload l'app (dans Metro Bundler, appuyer sur 'r')
# Ou relancer complètement :
npx expo start --clear
```

## ✅ Vérification

L'onglet "Pharmacies" devrait maintenant fonctionner et :

- ✅ Demander la permission de localisation
- ✅ Afficher les pharmacies proches
- ✅ Ne plus avoir d'erreur 404

---

## 📝 Règle à retenir

**Si `BASE_URL` contient déjà `/api/v1`, NE PAS le répéter dans les appels :**

```typescript
// constants.ts
BASE_URL: 'http://192.168.1.16:8080/api/v1'

// services/*.ts
✅ apiClient.get('/medications/search')    // → /api/v1/medications/search
✅ apiClient.post('/pharmacies/search')    // → /api/v1/pharmacies/search
❌ apiClient.post('/api/v1/pharmacies/...')  // → /api/v1/api/v1/...
```

---

## 🎯 Autres endpoints à vérifier

Si vous avez d'autres services, vérifiez qu'ils n'ont pas le même problème :

```bash
# Rechercher tous les appels avec /api/v1 en dur
grep -r "'/api/v1" MedVerifyApp/MedVerifyExpo/src/services/
```

Tous les endpoints devraient commencer par `/` sans `/api/v1`.

---

## ✅ C'est corrigé !

L'onglet Pharmacies devrait maintenant charger correctement les données.

