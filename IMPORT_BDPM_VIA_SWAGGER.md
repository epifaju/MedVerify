# 🚀 Import BDPM via Swagger - Méthode la Plus Simple

## ✅ Méthode Recommandée (Interface Graphique)

### **Étape 1 : Ouvrir Swagger** (2 secondes)

**Dans votre navigateur** (Chrome, Firefox, Edge) :

```
http://localhost:8080/swagger-ui.html
```

---

### **Étape 2 : Créer un compte admin** (30 secondes)

**1. Chercher** : `POST /api/v1/auth/register`

**2. Cliquer** sur "Try it out"

**3. Entrer ce JSON** :

```json
{
  "email": "import@medverify.gw",
  "password": "Import123!",
  "firstName": "Import",
  "lastName": "Admin",
  "phone": "+245999999999"
}
```

**4. Cliquer** sur "Execute"

**5. Vérifier** : Réponse 200 OK

---

### **Étape 3 : Se connecter** (20 secondes)

**1. Chercher** : `POST /api/v1/auth/login`

**2. Cliquer** sur "Try it out"

**3. Entrer** :

```json
{
  "email": "import@medverify.gw",
  "password": "Import123!"
}
```

**4. Cliquer** sur "Execute"

**5. Copier le `accessToken`** de la réponse

**6. Cliquer** sur le bouton **"Authorize"** en haut de la page Swagger

**7. Coller** : `Bearer VOTRE_TOKEN` (remplacer VOTRE_TOKEN)

**8. Cliquer** sur "Authorize" puis "Close"

---

### **Étape 4 : Promouvoir en ADMIN** (30 secondes)

Le compte créé est un USER normal. Il faut le promouvoir en ADMIN via SQL :

**PowerShell** :

```powershell
$env:PGPASSWORD='Malagueta7'
psql -U postgres -d medverify -c "UPDATE users SET role = 'ADMIN' WHERE email = 'import@medverify.gw';"
```

---

### **Étape 5 : Lancer l'import** (10 secondes)

**Retour dans Swagger** :

**1. Chercher** : `POST /api/v1/admin/bdpm/import`

**2. Cliquer** sur "Try it out"

**3. Cliquer** sur "Execute"

**4. Vérifier** : Réponse 202 Accepted

```json
{
  "message": "Import BDPM démarré. Consultez les logs..."
}
```

---

### **Étape 6 : Suivre la progression** (5-10 minutes)

**PowerShell** :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend
Get-Content logs\medverify.log -Wait -Tail 30
```

**Logs attendus** :

```
🚀 Starting BDPM full database import...
📊 Total medications: 15803, Pages: 1581
📥 Downloading page 1/1581
📥 Downloading page 2/1581
...
✅ BDPM import completed in 480s
📊 Statistics: 15803 imported, 0 updated, 0 errors
```

---

## 🎯 Alternative : Via SQL Directement

Si Swagger ne fonctionne pas, appelez directement le service Java :

**PowerShell** :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend

# Créer un petit script Java pour appeler l'import
$javaCode = @"
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class TriggerImport {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(com.medverify.MedVerifyApplication.class, args);
        com.medverify.service.BDPMImportService service = ctx.getBean(com.medverify.service.BDPMImportService.class);
        service.importFullBDPM();
    }
}
"@

# Puis appelez ce script
# (À compléter si nécessaire)
```

---

## ✅ Résumé des identifiants

| Compte | Email | Mot de passe |
|--------|-------|--------------|
| Nouveau compte | `import@medverify.gw` | `Import123!` |
| Ancien compte | `admin@medverify.gw` | `Admin@123456` (si fonctionne) |

---

## 🚀 Après l'import

**Vérifier** :

```powershell
$env:PGPASSWORD='Malagueta7'
psql -U postgres -d medverify -c "SELECT COUNT(*) FROM medications WHERE cip13 IS NOT NULL;"
```

**Scanner Hélicidine** → AUTHENTIC ✅

---

**⏱️ Temps total : 10-15 minutes avec Swagger**

**Utilisez Swagger, c'est la méthode la plus fiable ! 🎯**







