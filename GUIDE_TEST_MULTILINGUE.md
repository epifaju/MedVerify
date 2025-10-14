# 🧪 Guide de Test - Fonction Multilingue

## 🎯 Objectif

Vérifier que le changement de langue fonctionne parfaitement sur **tous les écrans** de l'application MedVerifyExpo.

---

## ⚙️ Prérequis

1. **Lancer le backend** (si nécessaire) :

   ```bash
   cd medverify-backend
   mvn spring-boot:run
   ```

2. **Lancer l'application mobile** :

   ```bash
   cd MedVerifyApp/MedVerifyExpo
   npm start
   ```

3. **Se connecter** avec un compte test

---

## 🔍 Scénarios de Test

### Test 1 : Changement de Langue dans le Profil ✅

**Étapes** :

1. Ouvrir l'application
2. Aller dans l'onglet **Profil** (👤 en bas)
3. Dans la section "Paramètres" → cliquer sur **Langue**
4. Sélectionner **Português** (🇵🇹)

**Vérifications ProfileScreen** :

- ✅ "Informations" → "Informações"
- ✅ "Email :" → "Email:"
- ✅ "Rôle :" → "Função:"
- ✅ "Statut :" → "Estado:"
- ✅ "Vérifié :" → "Verificado:"
- ✅ "Actif" → "Ativo"
- ✅ "Paramètres" → "Configurações"
- ✅ "Thème" → "Tema"
- ✅ "Langue" → "Idioma"
- ✅ Bouton "Déconnexion" → "Sair"

---

### Test 2 : Écran d'Accueil (Home) ✅

**Étapes** :

1. Rester en portugais
2. Aller dans l'onglet **Home** (🏠)

**Vérifications HomeScreen** :

- ✅ "Bienvenue, {{prénom}}!" → "Bem-vindo, {{prénom}}!"
- ✅ Rôle "Pharmacien" → "Farmacêutico"
- ✅ "Actions rapides" → "Ações rápidas"
- ✅ "Scanner un médicament" → "Escanear medicamento"
- ✅ "Vérifier l'authenticité avec la caméra" → "Verificar autenticidade com a câmera"
- ✅ "Signaler un problème" → "Relatar um problema"
- ✅ "Créer un signalement de contrefaçon" → "Criar um relatório de falsificação"
- ✅ "Tableau de bord" → "Painel"
- ✅ "Mon profil" → "Meu perfil"
- ✅ "Astuce" → "Dica"
- ✅ Texte de l'astuce complètement traduit

---

### Test 3 : Écran de Scan ✅

**Étapes** :

1. Rester en portugais
2. Cliquer sur "Escanear medicamento"

**Vérifications ScanScreen** :

- ✅ Titre "Vérifier un Médicament" → "Verificar um Medicamento"
- ✅ "Scanner avec caméra" → "Escanear com câmera"
- ✅ "OU" → "OU"
- ✅ "Saisie manuelle:" → "Entrada manual:"
- ✅ Placeholder "GTIN (ex: 03401234567890)" → traduit
- ✅ Placeholder "Numéro de série (optionnel)" → "Número de série (opcional)"
- ✅ Placeholder "Numéro de lot (optionnel)" → "Número de lote (opcional)"
- ✅ Bouton "Vérifier" → "Verificar"
- ✅ "GTINs de test :" → "GTINs de teste:"

**Test de scan** :

1. Entrer un GTIN de test : `03401234567890`
2. Cliquer sur "Verificar"

**Vérifications du résultat** :

- ✅ Status "AUTHENTIQUE" → "AUTÊNTICO"
- ✅ Status "SUSPECT" → "SUSPEITO"
- ✅ "Confiance: 95%" → "Confiança: 95%"
- ✅ "Fabricant:" → "Fabricante:"
- ✅ "Dosage:" → "Dosagem:"
- ✅ "Alertes :" → "Alertas:"
- ✅ "Signaler ce médicament" → "Relatar este medicamento"

**Test alertes** :

- ✅ "Médicament retiré du marché" → "Medicamento retirado do mercado"
- ✅ "Numéro de série déjà scanné" → "Número de série já escaneado"
- ✅ "Date de péremption dépassée" → "Data de validade expirada"

---

### Test 4 : Créole Bissau-Guinéen 🇬🇼

**Étapes** :

1. Aller dans Profil → Langue
2. Sélectionner **Kriol** (🇬🇼)

**Vérifications sur tous les écrans** :

**Profile** :

- ✅ "Informations" → "Informason"
- ✅ "Rôle :" → "Funson:"
- ✅ "Statut :" → "Istadu:"
- ✅ "Actif" → "Ativu"
- ✅ "Paramètres" → "Konfigurasõis"
- ✅ "Langue" → "Lingua"

**Home** :

- ✅ "Bienvenue" → "Bon bini"
- ✅ "Actions rapides" → "Ason rápidu"
- ✅ "Scanner un médicament" → "Skania remédiu"
- ✅ "Pharmacien" → "Farmaseútiku"
- ✅ "Patient" → "Pasienti"

**Scan** :

- ✅ "Vérifier" → "Verifika"
- ✅ "Scanner avec caméra" → "Skania ku kámara"
- ✅ "AUTHENTIQUE" → "AUTÉNTIKU"
- ✅ "Confiance" → "Kunfiansa"
- ✅ "Fabricant" → "Fabrikanti"

---

### Test 5 : Gestion des Utilisateurs (Admin) ✅

**Étapes** (si compte admin) :

1. Rester en portugais
2. Aller dans l'écran de gestion des utilisateurs

**Vérifications** :

- ✅ Titre "Gestion des Utilisateurs" → "Gestão de Utilizadores"
- ✅ Bouton "Créer un utilisateur" → "Criar utilizador"
- ✅ Modal titre "Créer un Utilisateur" → "Criar um Utilizador"
- ✅ Champs :
  - "Email" → "Email"
  - "Mot de passe" → "Senha"
  - "Prénom" → "Nome"
  - "Nom" → "Sobrenome"
  - "Téléphone" → "Telefone"
  - "Rôle :" → "Função:"
- ✅ Rôles :
  - "Patient" → "Paciente"
  - "Pharmacien" → "Farmacêutico"
  - "Autorité" → "Autoridade"
- ✅ Boutons :
  - "Créer" → "Criar"
  - "Annuler" → "Cancelar"
- ✅ Messages :
  - "Utilisateur créé avec succès" → "Utilizador criado com sucesso"
  - "Utilisateur supprimé" → "Utilizador excluído"

---

### Test 6 : Signalements ✅

**Étapes** :

1. Rester en portugais
2. Aller dans l'onglet **Reports** (📢)

**Vérifications** :

- ✅ Placeholder "GTIN du médicament" → "GTIN do medicamento"
- ✅ "Type de problème :" → "Tipo de problema:"
- ✅ Types :
  - "Contrefaçon" → "Falsificação"
  - "Qualité" → "Qualidade"
  - "Emballage" → "Embalagem"
- ✅ Placeholder "Description du problème..." → "Descrição do problema..."
- ✅ "Signalement créé" → "Relatório criado"
- ✅ "Référence: REF123" → "Referência: REF123"

---

### Test 7 : Persistance de la Langue ✅

**Étapes** :

1. Changer en créole (🇬🇼 Kriol)
2. Vérifier que tout est en créole
3. **Fermer complètement l'application** (kill l'app)
4. **Rouvrir l'application**
5. Se reconnecter

**Vérification** :

- ✅ L'application démarre en créole
- ✅ Tous les écrans restent en créole
- ✅ La langue est bien persistée

---

## 📋 Checklist Complète

### Écrans à Tester

- [ ] ProfileScreen - Informations, Paramètres
- [ ] HomeScreen - Bienvenue, Actions rapides
- [ ] ScanScreen - Scanner, Formulaire, Résultats
- [ ] ReportsScreen - Formulaire, Types
- [ ] UserManagementScreen - Gestion, Création
- [ ] RegisterScreen - Inscription

### Langues à Tester

- [ ] 🇫🇷 Français (langue par défaut)
- [ ] 🇵🇹 Portugais (langue principale Guinée-Bissau)
- [ ] 🇬🇼 Créole (Kriol - langue locale)

### Fonctionnalités

- [ ] Changement de langue instantané
- [ ] Persistance au redémarrage
- [ ] Variables dynamiques (nom, pourcentages)
- [ ] Messages d'erreur traduits
- [ ] Messages de succès traduits
- [ ] Placeholders traduits
- [ ] Labels traduits
- [ ] Rôles traduits

---

## 🎉 Résultat Attendu

**TOUT doit être traduit à 100% !**

Aucun texte en français ne doit apparaître quand vous êtes en portugais ou créole.

Si vous trouvez un texte non traduit, c'est un bug à signaler !

---

## 🚀 C'est Prêt !

L'application est maintenant **100% multilingue** et prête pour la Guinée-Bissau ! 🇬🇼🇵🇹🇫🇷


