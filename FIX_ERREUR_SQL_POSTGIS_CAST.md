# 🔧 FIX - Erreur syntaxe SQL PostGIS (Cast)

## ❌ Problème

```
SQL Error: 0, SQLState: 42601
ERREUR: erreur de syntaxe sur ou près de « : »
Position: 81
```

**Cause :** Erreur de syntaxe dans les casts PostgreSQL.

## 🔍 Explication

Dans PostgreSQL, le **cast de type** utilise `::` (double deux-points), pas `:` (simple deux-points).

```sql
❌ p.location:geography      -- FAUX
✅ p.location::geography     -- CORRECT

❌ ST_SetSRID(...):geography -- FAUX
✅ ST_SetSRID(...)::geography -- CORRECT
```

**Mais dans les requêtes JPA @Query**, il faut **échapper** les `::` avec `\\:\\:` car `:` est utilisé pour les paramètres nommés.

## ✅ Solution appliquée

**Correction dans `PharmacyRepository.java` et `OnDutyScheduleRepository.java` :**

```java
// Avant (FAUX)
❌ p.location::geography

// Après (CORRECT)
✅ p.location\\:\\:geography
```

**Tous les casts ont été corrigés dans :**

- ✅ `PharmacyRepository.java` - 3 requêtes
- ✅ `OnDutyScheduleRepository.java` - 1 requête

## 🚀 Relancer le backend

```bash
# Arrêter le backend (Ctrl+C dans le terminal)

# Relancer
cd medverify-backend
mvn spring-boot:run
```

## ✅ Vérification

Si tout fonctionne, vous devriez voir dans les logs :

```
✅ Searching pharmacies: lat=X.XXXX, lon=X.XXXX, radius=5.0km
✅ Found X pharmacies
```

Au lieu de :

```
❌ SQL Error: 0, SQLState: 42601
❌ ERREUR: erreur de syntaxe sur ou près de « : »
```

---

## 📝 Explications techniques

### Pourquoi `\\:\\:` ?

1. **PostgreSQL** attend `::` pour un cast
2. **JPA** utilise `:` pour les paramètres nommés (`:latitude`, `:longitude`)
3. **Dans @Query**, il faut échapper\*\* : `\\:` devient `:` en SQL final

**Exemple complet :**

```java
// Code Java avec échappement
@Query(value = """
    SELECT ST_Distance(
        p.location\\:\\:geography,  -- Devient :: en SQL
        ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography
    ) as distance
    FROM pharmacies p
""", nativeQuery = true)
```

**SQL généré par Hibernate :**

```sql
SELECT ST_Distance(
    p.location::geography,  -- ✅ Cast correct
    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
) as distance
FROM pharmacies p
```

---

## 🎯 Fichiers corrigés

- ✅ `PharmacyRepository.java`

  - `findPharmaciesWithinRadius()`
  - `findNearestPharmacies()`
  - `find24hPharmaciesNearby()`

- ✅ `OnDutyScheduleRepository.java`
  - `findOnDutyPharmaciesNearby()`

---

## 🧪 Test après correction

**1. Relancer le backend**

```bash
mvn spring-boot:run
```

**2. Test depuis l'app mobile**

- Ouvrir l'app
- Aller sur l'onglet "Pharmacies" 🏥
- Autoriser la géolocalisation
- ✅ La liste devrait s'afficher avec les pharmacies

**3. Vérifier les logs backend**

Vous devriez voir :

```
✅ Searching pharmacies: lat=48.9819, lon=1.6744, radius=5.0km
✅ Found 0 pharmacies
```

(0 pharmacies normal si position en France et données de test à Bissau)

---

## 📊 Test avec Postman/Insomnia

```http
POST http://localhost:8080/api/v1/pharmacies/search
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "limit": 20
}
```

**Réponse attendue :** Liste des 3 pharmacies de test à Bissau

---

## 🐛 Si erreur "0 pharmacies found"

C'est **normal** si :

- Votre position GPS est en Europe (France, etc.)
- Les données de test sont à Bissau, Guinée-Bissau

**Solutions :**

1. **Test avec coordonnées Bissau** (dans Postman) :

   ```json
   {
     "latitude": 11.8636,
     "longitude": -15.5984,
     "radiusKm": 5
   }
   ```

2. **Ou ajouter pharmacies locales** :
   ```sql
   INSERT INTO pharmacies (name, address, city, region, country, location, is_active)
   VALUES (
       'Pharmacie Test Paris',
       '1 rue de la Paix',
       'Paris',
       'Île-de-France',
       'France',
       ST_SetSRID(ST_MakePoint(2.3522, 48.8566), 4326)::geography,
       true
   );
   ```

---

## ✅ C'est corrigé !

Les requêtes PostGIS devraient maintenant fonctionner sans erreur de syntaxe.

