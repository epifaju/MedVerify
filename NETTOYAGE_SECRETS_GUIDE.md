# 🔒 Guide de Nettoyage des Secrets dans les Fichiers MD

## ⚠️ IMPORTANT

Ce document liste les secrets trouvés dans les fichiers MD et les instructions pour les nettoyer.

## Secrets Trouvés et Remplacés

### 1. Mot de passe PostgreSQL (`Malagueta7`)

**Fichiers affectés :**
- `medverify-backend/LANCER_AVEC_PROFIL_LOCAL.md` ✅ **Nettoyé**
- `RAPPORT_ANALYSE_PROBLEMES.md` - À nettoyer
- `PLAN_ACTION_AMELIORATIONS.md` - À nettoyer
- Autres fichiers mentionnant `Malagueta7` ou `$env:PGPASSWORD`

**Remplacer par :**
- `VOTRE_MOT_DE_PASSE_POSTGRESQL` (en majuscules pour indiquer qu'il faut remplacer)
- Ou `postgres` pour les exemples génériques

### 2. Mot de passe Admin (`Admin@123456`, `Admin123!`, etc.)

**Fichiers affectés :**
- `VERIFICATION_PHARMACIE_CRUD_COMPLETE.md`
- `CREER_COMPTE_TEST.md`
- `SOLUTION_FINALE_COMPLETE.md`
- Plusieurs autres fichiers de test

**Remplacer par :**
- `VOTRE_MOT_DE_PASSE_ADMIN` ou
- `Password123!` (exemple générique de format valide)

### 3. Secrets JWT et SMTP

**Remplacer par :**
- `VOTRE_JWT_SECRET` pour les secrets JWT
- `VOTRE_APP_PASSWORD_GMAIL` pour les passwords SMTP

## Commandes pour Vérifier

```bash
# Chercher tous les mots de passe potentiellement exposés
git grep -i "password.*=" -- "*.md" | grep -v "VOTRE\|xxxx\|Password123"

# Chercher les secrets JWT
git grep -i "jwt.*secret.*=" -- "*.md" | grep -v "VOTRE\|xxxx"

# Chercher les secrets DB
git grep -i "db.*password\|pgpassword" -- "*.md" | grep -v "VOTRE\|postgres\|xxxx"
```

## Règles à Suivre

1. ✅ **JAMAIS** de mot de passe réel dans les fichiers MD
2. ✅ Utiliser `VOTRE_MOT_DE_PASSE_XXX` pour indiquer qu'il faut remplacer
3. ✅ Utiliser `xxxx-xxxx-xxxx-xxxx` pour les exemples de format
4. ✅ Utiliser des mots de passe d'exemple génériques (`Password123!`, `postgres`) uniquement si documentés comme exemples
5. ✅ Ajouter un avertissement `⚠️` si un exemple de mot de passe est nécessaire

## Fichiers à Nettoyer Manuellement

Les fichiers suivants contiennent encore des secrets potentiels et doivent être nettoyés :

- `RAPPORT_ANALYSE_PROBLEMES.md` (ligne 27)
- `PLAN_ACTION_AMELIORATIONS.md` (lignes 50-51)
- Fichiers contenant `$env:PGPASSWORD='Malagueta7'`
- Fichiers contenant `Admin@123456` ou `Admin123!`

## Validation Finale

Après nettoyage, exécuter :

```bash
# Vérifier qu'aucun secret réel n'est présent
git grep -iE "(Malagueta7|Admin@123456|Admin123!)" -- "*.md"

# Devrait retourner 0 résultat
```

---

**Note :** Ce nettoyage est une **priorité de sécurité**. Si ces fichiers sont commités sur GitHub, les secrets seront exposés publiquement.

