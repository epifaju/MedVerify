# 🔧 Fix: Erreur "Text strings must be rendered within a <Text> component" - PharmaciesScreen

## 📋 Problème

Erreur React Native lors de l'affichage des pharmacies :

```
ERROR  Text strings must be rendered within a <Text> component.
```

## 🔍 Cause

Les données retournées par l'API peuvent contenir des propriétés `undefined` ou `null` :

- `address` : peut être null ou undefined
- `city` : peut être null ou undefined
- Les propriétés booléennes (`is24h`, `isNightPharmacy`, etc.) peuvent être null (Boolean wrapper Java)

En React Native, essayer de rendre directement `undefined` ou `null` dans un composant `<Text>` cause cette erreur.

## ✅ Solutions Appliquées

### 1. Ajout de vérifications conditionnelles dans `PharmaciesScreen.tsx`

**Avant :**

```typescript
<Text style={styles.pharmacyName}>{item.name}</Text>
<Text style={styles.pharmacyAddress}>{item.address}</Text>
<Text style={styles.pharmacyCity}>{item.city}</Text>
```

**Après :**

```typescript
<Text style={styles.pharmacyName}>{item.name}</Text>;
{
  item.address && <Text style={styles.pharmacyAddress}>{item.address}</Text>;
}
{
  item.city && <Text style={styles.pharmacyCity}>{item.city}</Text>;
}
```

### 2. Correction du popup HTML dans `LeafletMapView.tsx`

**Avant :**

```typescript
const popupContent = `
  <div class="pharmacy-popup">
    <h3>\${pharmacy.name}</h3>
    <p>\${pharmacy.address}</p>
    \${pharmacy.distanceKm ? \`<p class="distance">\${pharmacy.distanceKm.toFixed(1)} km</p>\` : ''}
  </div>
`;
```

**Après :**

```typescript
const popupContent = `
  <div class="pharmacy-popup">
    <h3>\${pharmacy.name}</h3>
    \${pharmacy.address ? \`<p>\${pharmacy.address}</p>\` : ''}
    \${pharmacy.distanceKm ? \`<p class="distance">\${pharmacy.distanceKm.toFixed(1)} km</p>\` : ''}
  </div>
`;
```

## 📊 Analyse Backend

Le backend retourne bien les bonnes propriétés :

- ✅ `latitude` et `longitude` (pas `lat`/`lng`)
- ⚠️ Les champs `address`, `city` peuvent être null
- ⚠️ Les booléens sont des `Boolean` wrapper (peuvent être null)

```java
// PharmacyDTO.java (lignes 38-40)
private Boolean is24h;
private Boolean isNightPharmacy;
private Boolean isOpenNow;
```

## 🎯 Résultat

Les pharmacies s'affichent maintenant correctement sans erreur, même si certaines propriétés sont manquantes.

Les composants conditionnels ne sont rendus que si les données existent :

- L'adresse s'affiche uniquement si présente
- La ville s'affiche uniquement si présente
- Le popup de la carte n'affiche que les informations disponibles

## 📝 Bonnes Pratiques

Pour éviter ce type d'erreur à l'avenir :

1. **Toujours vérifier les valeurs nulles/undefined avant de rendre dans JSX**

```typescript
{
  value && <Text>{value}</Text>;
}
```

2. **Utiliser des valeurs par défaut si nécessaire**

```typescript
<Text>{value || "Non renseigné"}</Text>
```

3. **Typer correctement avec des propriétés optionnelles**

```typescript
interface Pharmacy {
  name: string; // Obligatoire
  address?: string; // Optionnel
  city?: string; // Optionnel
}
```

## ✅ Fichiers Modifiés

- `MedVerifyApp/MedVerifyExpo/src/screens/Pharmacies/PharmaciesScreen.tsx`
- `MedVerifyApp/MedVerifyExpo/src/components/LeafletMapView.tsx`
