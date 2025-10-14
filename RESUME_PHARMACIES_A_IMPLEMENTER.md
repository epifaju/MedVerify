# 📋 Résumé - Ce qui manque à implémenter (Pharmacies)

## 🎯 État actuel : 60% complet

---

## ✅ CE QUI FONCTIONNE DÉJÀ

✅ Backend PostGIS complet  
✅ Recherche géolocalisée (rayon, ville)  
✅ Filtres (Toutes / Ouvertes / Garde / 24h)  
✅ Liste pharmacies avec distance  
✅ Détails pharmacie + horaires  
✅ Itinéraire Google Maps  
✅ Appel direct

---

## ❌ CE QUI MANQUE (3 fonctionnalités)

### 🔴 1. CARTE INTERACTIVE (PRIORITÉ HAUTE)

**Statut :** ❌ **NON IMPLÉMENTÉ**

**Problème :**

- `react-native-maps` pas installé
- Aucun composant `MapView` dans le code
- Pas de markers sur carte
- Pas de toggle Liste/Carte

**Solution rapide (2-3h) :**

```bash
# 1. Installer dépendances
cd MedVerifyApp/MedVerifyExpo
npx expo install react-native-maps
npm install geolib

# 2. Obtenir Google Maps API Key
# https://console.cloud.google.com/
# Activer Maps SDK for Android/iOS

# 3. Ajouter dans app.json
{
  "expo": {
    "android": {
      "config": {
        "googleMaps": {
          "apiKey": "VOTRE_CLE_API"
        }
      }
    }
  }
}

# 4. Modifier PharmaciesScreen.tsx
# Ajouter MapView avec markers pharmacies
# Ajouter toggle Liste/Carte
```

**Fichiers à modifier :**

- [ ] `package.json` - Ajouter dépendances
- [ ] `app.json` - Ajouter API key
- [ ] `PharmaciesScreen.tsx` - Ajouter MapView + toggle

---

### 🟡 2. NOTIFICATIONS PUSH GARDES (PRIORITÉ MOYENNE)

**Statut :** ❌ **NON IMPLÉMENTÉ**

**Problème :**

- `PharmacyDutyNotificationService.java` n'existe pas
- Pas de @Scheduled pour notifications auto
- Pas de notifs garde weekend

**Solution (3-4h) :**

```java
// Créer PharmacyDutyNotificationService.java
@Service
public class PharmacyDutyNotificationService {

    @Scheduled(cron = "0 0 18 * * FRI") // Vendredi 18h
    public void notifyWeekendDutyPharmacies() {
        // Envoyer notifs utilisateurs de la région
    }

    public void notifyDutyChange(OnDutySchedule newDuty, String region) {
        // Notification urgente changement garde
    }
}
```

**Fichiers à créer :**

- [ ] `PharmacyDutyNotificationService.java`
- [ ] Mettre à jour `NotificationType.java` (ajouter PHARMACY_DUTY_UPDATE)
- [ ] Mettre à jour `UserRepository.java` (findActiveUsersInRegion)
- [ ] `application.properties` - Activer scheduling

---

### 🟢 3. SYSTÈME NOTATION/AVIS (PRIORITÉ BASSE)

**Statut :** ❌ **NON IMPLÉMENTÉ** (optionnel)

**Problème :**

- Champ `rating` existe mais pas d'endpoints
- Pas de table `pharmacy_reviews`
- Pas d'affichage étoiles

**Solution (4-5h) :**

```sql
-- Migration V11
CREATE TABLE pharmacy_reviews (
    pharmacy_id UUID,
    user_id UUID,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT
);
```

```java
// Endpoints à créer
POST /api/v1/pharmacies/{id}/rate
POST /api/v1/pharmacies/{id}/reviews
GET /api/v1/pharmacies/{id}/reviews
```

**Fichiers à créer :**

- [ ] Migration `V11__pharmacy_reviews_system.sql`
- [ ] `PharmacyReview.java` entity
- [ ] Endpoints rate/reviews dans Controller
- [ ] Composant `StarRating.tsx` frontend
- [ ] Écran `PharmacyReviewsScreen.tsx`

---

## 📊 Tableau récapitulatif

| Fonctionnalité            | Demandée | Implémentée | Temps | Priorité     |
| ------------------------- | -------- | ----------- | ----- | ------------ |
| Recherche géolocalisée    | ✅       | ✅          | -     | -            |
| Filtres avancés           | ✅       | ✅          | -     | -            |
| Itinéraire/Appel          | ✅       | ✅          | -     | -            |
| **🗺️ Carte interactive**  | ✅       | ❌          | 2-3h  | 🔴 **HAUTE** |
| **📢 Notifications push** | ✅       | ❌          | 3-4h  | 🟡 MOYENNE   |
| **⭐ Notation/Avis**      | ⚠️       | ❌          | 4-5h  | 🟢 BASSE     |

---

## 🚀 Action recommandée

### PRIORITÉ 1 : Implémenter la carte interactive

**Pourquoi ?**

- ✅ Fonctionnalité clé mentionnée dans specs initiales
- ✅ Impact UX majeur
- ✅ Temps raisonnable (2-3h)
- ✅ Dépendances documentées dans guides

**Commandes rapides :**

```bash
# Terminal 1 - Backend (déjà fonctionnel)
cd medverify-backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd MedVerifyApp/MedVerifyExpo

# Installer react-native-maps
npx expo install react-native-maps
npm install geolib

# Configurer Google Maps API dans app.json
# (voir guide GUIDE_COMPLET_PHARMACIES.md section 4)

# Relancer
npx expo start --clear
```

**Puis modifier `PharmaciesScreen.tsx` :**

1. Importer `MapView`, `Marker` de `react-native-maps`
2. Ajouter state `viewMode: 'list' | 'map'`
3. Ajouter toggle Liste/Carte dans header
4. Ajouter composant MapView avec markers
5. Gérer onPress sur markers

---

## 📝 Guides disponibles

- 📘 **ANALYSE_PHARMACIES_FONCTIONNALITES_MANQUANTES.md** - Analyse détaillée (ce document complet)
- 📘 **GUIDE_COMPLET_PHARMACIES.md** - Guide exhaustif existant
- 📘 **QUICK_START_PHARMACIES.md** - Démarrage rapide
- 📘 **FEATURE_PHARMACIES_COMPLETE.md** - Récap technique

---

## ✅ Checklist finale (à compléter)

### Backend

- [x] PostGIS + requêtes géospatiales
- [x] Endpoints REST pharmacies
- [x] Filtres (ouvert, garde, 24h)
- [ ] Service notifications garde
- [ ] Endpoints notation/avis

### Frontend

- [x] Liste pharmacies avec filtres
- [x] Détails pharmacie + horaires
- [x] Itinéraire + Appel
- [ ] **Carte interactive MapView**
- [ ] **Toggle Liste/Carte**
- [ ] Composant notation étoiles
- [ ] Écran avis utilisateurs

---

**🎯 Objectif :** Passer de 60% à 100% en implémentant les 3 fonctionnalités manquantes

**⏱️ Temps total estimé :** 9-12h

- Carte : 2-3h
- Notifications : 3-4h
- Notation : 4-5h
