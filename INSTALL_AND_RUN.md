# 🚀 Installation et Lancement - MedVerify

## ⚡ Guide rapide (5 minutes)

---

## 1️⃣ Installer les dépendances

```bash
cd MedVerifyApp\MedVerifyExpo
npm install
```

**Cela va installer :**

- React Navigation (6 packages)
- expo-sqlite
- @react-native-community/netinfo
- Autres dépendances existantes

---

## 2️⃣ Lancer l'application

### Option A : Android

```bash
npm run android
```

### Option B : iOS

```bash
npm run ios
```

### Option C : Web (limité)

```bash
npm run web
```

---

## 3️⃣ Se connecter

**Compte de test :**

```
Email : admin@medverify.gw
Password : Admin@123456
```

---

## 🎯 Tester les fonctionnalités

### Scanner

1. Cliquer sur tab "Scanner"
2. Cliquer sur "Scanner avec caméra" OU saisir manuellement
3. **GTIN de test** : `03401234567890`
4. Voir le résultat

### Signalement

1. Cliquer sur tab "Signaler"
2. Remplir GTIN + Description
3. Créer le signalement
4. Voir la liste

### Profile

1. Cliquer sur tab "Profil"
2. Voir informations
3. Changer thème/langue
4. Se déconnecter

### Navigation

1. **Bouton retour Android** → Fonctionne maintenant ! ✅
2. **Transitions animées** → Entre les tabs ✅
3. **Register** → Login → Register ✅

---

## 📱 Nouveautés

### 🆕 Écrans ajoutés

- **HomeScreen** - Page d'accueil avec actions rapides
- **RegisterScreen** - Inscription
- **ProfileScreen** - Profil et paramètres

### ✨ Fonctionnalités ajoutées

- **React Navigation** - Navigation professionnelle
- **Notifications Toast** - Messages élégants
- **Mode Offline** - Cache SQLite

---

## ✅ Checklist de vérification

Après installation, vérifier que :

- [ ] L'app démarre sans erreur
- [ ] Login fonctionne
- [ ] Navigation entre tabs fonctionne
- [ ] Bouton retour Android fonctionne
- [ ] Scanner fonctionne
- [ ] Signalements fonctionnent
- [ ] Dashboard fonctionne (si ADMIN)
- [ ] Profil et déconnexion fonctionnent

---

## 🐛 En cas de problème

### Erreur : Cannot find module

```bash
rm -rf node_modules
rm package-lock.json
npm install
```

### Erreur : Metro bundler

```bash
npx expo start --clear
```

### Erreur : Types TypeScript

```bash
npm run android  # Relancer l'app
```

---

## 📖 Documentation

**Quick Starts** (2-3 min chacun) :

- `QUICK_START_TOAST.md`
- `QUICK_START_OFFLINE.md`
- `QUICK_START_REACT_NAVIGATION.md`

**Guides complets** (10-15 min chacun) :

- `REACT_NAVIGATION_IMPLEMENTATION_COMPLETE.md`
- `OFFLINE_MODE_IMPLEMENTATION_SUMMARY.md`
- `NOTIFICATIONS_TOAST_IMPLEMENTATION_SUMMARY.md`

**Récapitulatif global** :

- `IMPLEMENTATION_COMPLETE_SUMMARY.md` ⭐

---

## 🎉 Vous êtes prêt !

L'application MedVerify est **100% opérationnelle** !

**Lancez et profitez ! 🚀**
