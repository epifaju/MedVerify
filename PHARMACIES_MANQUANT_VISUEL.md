# 🏥 Pharmacies - Ce qui manque (Vue d'ensemble)

```
┌─────────────────────────────────────────────────────────────┐
│                 FONCTIONNALITÉ PHARMACIES                   │
│                                                             │
│  État actuel : ████████████░░░░░░░░ 60% complet            │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ IMPLÉMENTÉ (60%)

```
┌──────────────────────────────────────────────────┐
│ Backend PostGIS                            ✅    │
│ Recherche géolocalisée (rayon)             ✅    │
│ Recherche par ville                        ✅    │
│ Filtres (Toutes/Ouvertes/Garde/24h)        ✅    │
│ Liste pharmacies                           ✅    │
│ Détails + horaires                         ✅    │
│ Itinéraire Google Maps                     ✅    │
│ Appel direct                               ✅    │
└──────────────────────────────────────────────────┘
```

---

## ❌ MANQUANT (40%)

### 🔴 1. CARTE INTERACTIVE

```
┌──────────────────────────────────────────────────┐
│  Priorité : ████████████████████░ HAUTE          │
│  Temps   : 2-3h                                  │
│  Impact  : ÉLEVÉ (UX majeur)                     │
└──────────────────────────────────────────────────┘

❌ Problèmes :
   • react-native-maps NON installé
   • Aucun MapView dans le code
   • Pas de markers pharmacies
   • Pas de toggle Liste/Carte
   • Pas de Google Maps API Key

✅ Solution :
   1. npx expo install react-native-maps
   2. npm install geolib
   3. Obtenir Google Maps API Key
   4. Ajouter MapView dans PharmaciesScreen
   5. Ajouter toggle Liste/Carte
```

**Aperçu visuel de ce qui manque :**

```
┌─────────────────────────────────────────┐
│  📋 Liste            🗺️ Carte           │  ← Toggle à ajouter
├─────────────────────────────────────────┤
│                                         │
│      🗺️ [Google Maps vide]             │  ← MapView à créer
│                                         │
│      📍 Pharmacie A                     │  ← Markers à ajouter
│      📍 Pharmacie B                     │
│      📍 Pharmacie C                     │
│                                         │
└─────────────────────────────────────────┘
```

---

### 🟡 2. NOTIFICATIONS PUSH

```
┌──────────────────────────────────────────────────┐
│  Priorité : ████████████░░░░░░ MOYENNE           │
│  Temps   : 3-4h                                  │
│  Impact  : MOYEN (utile mais non bloquant)       │
└──────────────────────────────────────────────────┘

❌ Problèmes :
   • PharmacyDutyNotificationService.java N'EXISTE PAS
   • Pas de @Scheduled vendredi 18h
   • Pas de notifications garde weekend
   • Pas de notifications urgentes

✅ Solution :
   1. Créer PharmacyDutyNotificationService.java
   2. Ajouter @Scheduled(cron = "0 0 18 * * FRI")
   3. Intégrer avec NotificationService existant
   4. Mettre à jour NotificationType enum
   5. Activer scheduling dans application.properties
```

**Flux de notification manquant :**

```
Vendredi 18h
     │
     ├─→ Rechercher gardes weekend
     │
     ├─→ Grouper par région
     │
     ├─→ Trouver utilisateurs région
     │
     └─→ 📢 Envoyer notification
          "Pharmacie X sera de garde ce weekend"
```

---

### 🟢 3. NOTATION / AVIS

```
┌──────────────────────────────────────────────────┐
│  Priorité : ████░░░░░░░░░░░░ BASSE              │
│  Temps   : 4-5h                                  │
│  Impact  : FAIBLE (nice-to-have)                 │
└──────────────────────────────────────────────────┘

❌ Problèmes :
   • Table pharmacy_reviews N'EXISTE PAS
   • Champ rating existe mais pas d'endpoints
   • Pas de POST /pharmacies/{id}/rate
   • Pas de composant StarRating
   • Pas d'affichage moyenne étoiles

✅ Solution :
   1. Créer migration V11__pharmacy_reviews_system.sql
   2. Créer PharmacyReview.java entity
   3. Ajouter endpoints rate/reviews
   4. Créer composant StarRating.tsx
   5. Afficher moyenne sur cartes pharmacies
```

**Affichage manquant :**

```
┌─────────────────────────────────────────┐
│  Pharmacie Centrale                     │
│  ⭐⭐⭐⭐☆ 4.2/5 (23 avis) ← MANQUANT    │
│                                         │
│  📍 Avenue Amilcar Cabral               │
│  🟢 Ouvert • 0.5 km                     │
│                                         │
│  ───────────────────────────────────    │
│                                         │
│  💬 Avis utilisateurs                   │  ← MANQUANT
│  • Jean D. ⭐⭐⭐⭐⭐                     │
│    "Excellent service"                  │
│                                         │
│  [ Laisser un avis ]                    │  ← MANQUANT
└─────────────────────────────────────────┘
```

---

## 📊 Comparaison Demandé vs Implémenté

```
Fonctionnalité              Demandée    Implémentée    Manquante
──────────────────────────────────────────────────────────────
Géolocalisation                ✅            ✅             -
Pharmacies ouvertes            ✅            ✅             -
Pharmacies garde               ✅            ✅             -
Pharmacies 24h                 ✅            ✅             -
Recherche ville                ✅            ✅             -
Itinéraire                     ✅            ✅             -
Filtres avancés                ✅            ✅             -

🗺️ Carte interactive          ✅            ❌            100%
📢 Notifications push          ✅            ❌            100%
⭐ Notation/Avis              ⚠️             ❌            100%
```

---

## 🚀 PLAN D'ACTION

### 🔴 PHASE 1 : Carte interactive (2-3h)

```bash
cd MedVerifyApp/MedVerifyExpo
npx expo install react-native-maps
npm install geolib

# Configurer Google Maps API Key dans app.json
# Modifier PharmaciesScreen.tsx (ajouter MapView)
```

**Impact : ÉLEVÉ** - Transformation majeure de l'UX

### 🟡 PHASE 2 : Notifications (3-4h)

```bash
cd medverify-backend
# Créer PharmacyDutyNotificationService.java
# Mettre à jour NotificationType.java
# Activer scheduling
mvn spring-boot:run
```

**Impact : MOYEN** - Améliore engagement utilisateurs

### 🟢 PHASE 3 : Notation (4-5h)

```bash
# Créer migration V11
# Créer PharmacyReview entity
# Créer endpoints rate/reviews
# Créer composants frontend
```

**Impact : FAIBLE** - Feature nice-to-have

---

## ⏱️ TEMPS TOTAL : 9-12h

```
┌────────────────────────────────────────┐
│  Carte         : ██████░░░░ 2-3h       │
│  Notifications : ████████░░ 3-4h       │
│  Notation      : ██████████ 4-5h       │
│                                        │
│  TOTAL         : 9-12h                 │
└────────────────────────────────────────┘
```

---

## 📁 FICHIERS À CRÉER/MODIFIER

### Backend (Notifications)

```
medverify-backend/src/main/java/com/medverify/
├── service/notification/
│   └── PharmacyDutyNotificationService.java    ← À CRÉER
├── entity/
│   └── NotificationType.java                   ← À MODIFIER
└── repository/
    └── UserRepository.java                     ← À MODIFIER
```

### Backend (Notation)

```
medverify-backend/
├── src/main/resources/db/migration/
│   └── V11__pharmacy_reviews_system.sql        ← À CRÉER
└── src/main/java/com/medverify/
    ├── entity/PharmacyReview.java              ← À CRÉER
    ├── repository/PharmacyReviewRepository.java ← À CRÉER
    └── controller/PharmacyController.java       ← À MODIFIER
```

### Frontend (Carte)

```
MedVerifyApp/MedVerifyExpo/
├── package.json                                 ← À MODIFIER
├── app.json                                     ← À MODIFIER
└── src/screens/Pharmacies/
    └── PharmaciesScreen.tsx                     ← À MODIFIER
```

### Frontend (Notation)

```
MedVerifyApp/MedVerifyExpo/src/
├── components/
│   └── StarRating.tsx                           ← À CRÉER
└── screens/Pharmacies/
    └── PharmacyReviewsScreen.tsx                ← À CRÉER
```

---

## ✅ CHECKLIST RAPIDE

### Carte interactive

- [ ] Installer react-native-maps
- [ ] Installer geolib
- [ ] Obtenir Google Maps API Key
- [ ] Configurer app.json
- [ ] Ajouter MapView dans PharmaciesScreen
- [ ] Ajouter markers pharmacies
- [ ] Créer toggle Liste/Carte
- [ ] Tester sur émulateur

### Notifications push

- [ ] Créer PharmacyDutyNotificationService.java
- [ ] Ajouter PHARMACY_DUTY_UPDATE dans NotificationType
- [ ] Ajouter findActiveUsersInRegion dans UserRepository
- [ ] Activer scheduling dans application.properties
- [ ] Tester notification manuelle
- [ ] Tester @Scheduled

### Notation/Avis

- [ ] Créer migration V11
- [ ] Créer PharmacyReview entity
- [ ] Créer PharmacyReviewRepository
- [ ] Ajouter endpoints rate/reviews
- [ ] Créer StarRating component
- [ ] Créer PharmacyReviewsScreen
- [ ] Afficher moyenne sur cartes
- [ ] Tester notation complète

---

## 🎯 RECOMMANDATION FINALE

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│  🚀 COMMENCER PAR LA CARTE INTERACTIVE              │
│                                                     │
│  Pourquoi ?                                         │
│  • Fonctionnalité clé des specs                    │
│  • Impact UX le plus élevé                         │
│  • Temps raisonnable (2-3h)                        │
│  • Les utilisateurs s'attendent à voir une carte   │
│                                                     │
│  Commande rapide :                                  │
│  $ cd MedVerifyApp/MedVerifyExpo                   │
│  $ npx expo install react-native-maps              │
│  $ npm install geolib                              │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 📚 DOCUMENTATION DISPONIBLE

- 📄 **ANALYSE_PHARMACIES_FONCTIONNALITES_MANQUANTES.md** - Analyse détaillée complète
- 📄 **RESUME_PHARMACIES_A_IMPLEMENTER.md** - Résumé des actions
- 📄 **PHARMACIES_MANQUANT_VISUEL.md** - Vue d'ensemble (ce document)
- 📄 **GUIDE_COMPLET_PHARMACIES.md** - Guide technique exhaustif
- 📄 **QUICK_START_PHARMACIES.md** - Démarrage rapide
- 📄 **FEATURE_PHARMACIES_COMPLETE.md** - Récapitulatif technique

---

**🎉 Avec ces 3 fonctionnalités, la feature Pharmacies sera 100% complète !**

