# 🔧 Solution : Lancer l'application avec le profil local

## Problème

L'application ne trouve pas le mot de passe PostgreSQL car le profil `local` n'est pas actif.

## Solutions

### Solution 1 : Via ligne de commande Maven (recommandé)

**Windows PowerShell :**

```powershell
cd medverify-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Windows CMD :**

```cmd
cd medverify-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Linux/Mac :**

```bash
cd medverify-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Solution 2 : Définir la variable d'environnement

**Windows PowerShell :**

```powershell
$env:DB_PASSWORD="Malagueta7"
cd medverify-backend
mvn spring-boot:run
```

**Windows CMD :**

```cmd
set DB_PASSWORD=Malagueta7
cd medverify-backend
mvn spring-boot:run
```

### Solution 3 : Modifier application.yml directement (temporaire pour test)

⚠️ **ATTENTION** : Ne jamais commiter le mot de passe dans application.yml !

Modifier `medverify-backend/src/main/resources/application.yml` :

```yaml
password: ${DB_PASSWORD:Malagueta7}
```

Remplacez `Malagueta7` par votre vrai mot de passe PostgreSQL.

## Vérification

Après avoir lancé l'application, vous devriez voir :

```
Started MedVerifyApplication in X.XXX seconds
```

Et non plus l'erreur :

```
The server requested SCRAM-based authentication, but no password was provided.
```

## Note

Le fichier `application-local.yml` contient déjà le mot de passe `Malagueta7`.
Vérifiez que c'est bien le bon mot de passe de votre base PostgreSQL.
