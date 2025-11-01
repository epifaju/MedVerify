# 🔍 Instructions Debug Immédiat

## Le problème

Le réseau fonctionne (health check OK), mais les requêtes POST ne parviennent pas au backend.

## Solution immédiate

### 1. Ouvrir l'écran Debug dans l'app

Dans l'application mobile:
1. Allez sur l'écran de **Login**
2. Cherchez le bouton **"🔧 Debug Réseau"** en bas de l'écran
3. Appuyez dessus

### 2. Lancer les tests automatiques

Dans l'écran Debug:
1. Appuyez sur **"Lancer les tests"**
2. Observez les résultats

### 3. Interpréter les résultats

**Si tous les tests échouent avec "Network Error":**
- Problème de timeout ou de configuration Axios
- Possible problème de certificat SSL (mais on utilise HTTP)

**Si les tests passent:**
- Le problème est ailleurs dans le code
- Vérifier les logs dans le terminal Metro

**Si seuls certains tests échouent:**
- Le problème est spécifique à cet endpoint

---

## Alternative: Vérifier les logs Metro

Dans le terminal Metro où `npm start` tourne, regardez les logs:

Recherchez:
- Erreurs de timeout
- Erreurs de certificat
- Messages de blocage

---

## Test manuel avec Postman (mobile)

Si Debug screen ne fonctionne pas:

1. Installer **Postman** sur le téléphone
2. Nouvelle requête:
   - Method: `POST`
   - URL: `http://192.168.1.16:8080/api/v1/auth/login`
   - Headers: `Content-Type: application/json`
   - Body:
     ```json
     {
       "email": "epifaju@admin.com",
       "password": "votre_mot_de_passe"
     }
     ```
3. Envoyer la requête

Si ça fonctionne dans Postman:
- ✅ Le backend est OK
- ❌ Le problème est dans l'application React Native

---

## Prochaine étape

Envoyez-moi:
1. Les résultats de l'écran Debug
2. OU un screenshot des logs Metro



