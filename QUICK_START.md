# 🚀 Guide de Démarrage Rapide MedVerify (Windows)

## Prérequis

- ✅ Java 17 installé
- ✅ PostgreSQL 14+ installé avec pgAdmin
- ✅ Node.js 16+ installé
- ✅ Android Studio OU Xcode (Mac uniquement)

## 📊 Configuration de la Base de Données (Windows)

### Méthode 1 : Via pgAdmin (Recommandé)

1. **Ouvrir pgAdmin**

   - Cherchez "pgAdmin" dans le menu Démarrer
   - Connectez-vous avec le mot de passe défini lors de l'installation

2. **Créer la base de données**

   - Dans le panneau de gauche, clic droit sur "Databases"
   - Sélectionnez "Create" → "Database"
   - Nom : `medverify`
   - Cliquez "Save"

3. **Activer l'extension PostGIS**
   - Clic droit sur la base `medverify`
   - Sélectionnez "Query Tool"
   - Exécutez cette commande :
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   ```
   - Cliquez sur le bouton "Execute" (▶️)

### Méthode 2 : Via Command Line (PowerShell)

Si vous connaissez votre mot de passe postgres :

```powershell
# Se connecter à PostgreSQL
psql -U postgres

# Créer la base (une fois connecté)
CREATE DATABASE medverify;
\c medverify
CREATE EXTENSION IF NOT EXISTS postgis;
\q
```

### Vérifier l'installation

```powershell
psql -U postgres -d medverify -c "SELECT PostGIS_version();"
```

Vous devriez voir la version de PostGIS s'afficher.

## 🔧 Configuration du Backend

1. **Naviguer vers le dossier backend**

   ```powershell
   cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend
   ```

2. **Créer le fichier .env** (optionnel)

   Copiez `.env.example` en `.env` et modifiez si nécessaire :

   ```env
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=medverify
   DB_USERNAME=postgres
   DB_PASSWORD=VOTRE_MOT_DE_PASSE_POSTGRES
   ```

3. **Ou modifier directement application.yml**

   Éditez `src/main/resources/application.yml` :

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/medverify
       username: postgres
       password: VOTRE_MOT_DE_PASSE # ⬅️ Mettez votre mot de passe ici
   ```

4. **Lancer le backend**

   ```powershell
   mvn clean install
   mvn spring-boot:run
   ```

5. **Vérifier que ça fonctionne**
   - Ouvrez http://localhost:8080/actuator/health
   - Vous devriez voir : `{"status":"UP"}`
   - Documentation API : http://localhost:8080/swagger-ui.html

## 📱 Configuration du Frontend

1. **Naviguer vers le dossier frontend**

   ```powershell
   cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp
   ```

2. **Installer les dépendances**

   ```powershell
   npm install
   ```

3. **Créer le fichier .env**

   Créez un fichier `.env` à la racine de MedVerifyApp :

   ```env
   API_BASE_URL=http://10.0.2.2:8080/api/v1
   ```

   > **Note** : `10.0.2.2` est l'adresse localhost pour l'émulateur Android.
   > Pour un appareil physique, utilisez l'IP de votre PC (ex: `http://192.168.1.10:8080/api/v1`)

4. **Lancer sur Android**

   ```powershell
   # Assurez-vous qu'un émulateur Android est démarré ou qu'un appareil est connecté
   npm run android
   ```

5. **Lancer sur iOS** (Mac uniquement)
   ```bash
   cd ios
   pod install
   cd ..
   npm run ios
   ```

## ✅ Test de l'Application

### 1. Backend - Tester l'API

**Via Swagger UI** : http://localhost:8080/swagger-ui.html

1. Allez dans "Authentication" → `POST /api/v1/auth/login`
2. Cliquez "Try it out"
3. Body :
   ```json
   {
     "email": "admin@medverify.gw",
     "password": "Admin@123456"
   }
   ```
4. Cliquez "Execute"
5. Vous devriez recevoir un `accessToken`

**Via curl** :

```powershell
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@medverify.gw\",\"password\":\"Admin@123456\"}'
```

### 2. Frontend - Tester l'App

1. L'app devrait se lancer sur l'émulateur
2. Vous verrez l'écran de login
3. Connectez-vous avec :
   - Email : `admin@medverify.gw`
   - Mot de passe : `Admin@123456`

### 3. Test du Scanner (optionnel)

Pour tester le scanner de codes, vous aurez besoin d'un code Data Matrix GS1. Exemple :

```
(01)03401234567890(17)251231(10)ABC123(21)XYZ789
```

Générez un code Data Matrix avec ce contenu sur : https://products.aspose.app/barcode/generate/datamatrix

## 🐛 Dépannage

### Erreur : "Cannot connect to database"

**Vérifiez** :

1. PostgreSQL est démarré : cherchez "Services" dans Windows → Cherchez "postgresql" → Statut = "Running"
2. Le mot de passe dans `application.yml` est correct
3. La base `medverify` existe bien (vérifiez dans pgAdmin)

### Erreur : "PostGIS extension not found"

Exécutez dans pgAdmin :

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

### Erreur : "Port 8080 already in use"

Un autre service utilise le port 8080. Changez-le dans `application.yml` :

```yaml
server:
  port: 8081
```

### Erreur Frontend : "Unable to connect to development server"

1. Assurez-vous que Metro est en cours d'exécution
2. Redémarrez avec `npm start -- --reset-cache`
3. Vérifiez l'URL dans `.env`

### Erreur : "SDK location not found" (Android)

Créez `android/local.properties` :

```properties
sdk.dir=C:\\Users\\epifa\\AppData\\Local\\Android\\Sdk
```

## 📚 Prochaines Étapes

1. ✅ **Créer un compte utilisateur**
   - Dans l'app, cliquez "S'inscrire"
   - Remplissez le formulaire
2. ✅ **Tester la vérification**

   - Connectez-vous
   - Accédez au scanner
   - Scannez un code ou testez avec les médicaments en base

3. ✅ **Voir l'historique**
   - Consultez vos scans précédents
   - Vérifiez les résultats

## 🔗 Liens Utiles

- **API Docs** : http://localhost:8080/swagger-ui.html
- **Health Check** : http://localhost:8080/actuator/health
- **Métriques** : http://localhost:8080/actuator/metrics

## 📞 Support

En cas de problème, vérifiez :

1. Les logs du backend dans la console
2. Les logs de l'app React Native
3. Le fichier `README.md` principal pour plus de détails

