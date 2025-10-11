# 🔄 Redémarrage du Backend - CORS Fixed

## ✅ Changement appliqué

Le CORS a été mis à jour pour autoriser les connexions depuis :

- `http://localhost:3000` (frontend web)
- `http://localhost:19006` (Expo web)
- `http://192.168.1.16:8080` ⬅️ **NOUVEAU** (votre IP locale)
- `http://10.0.2.2:8080` ⬅️ **NOUVEAU** (émulateur Android)

---

## 🔄 Redémarrer le Backend

### Option 1 : Si le backend tourne dans un terminal

1. Aller dans le terminal du backend
2. Appuyer sur `Ctrl+C`
3. Relancer :

```bash
cd medverify-backend
mvn spring-boot:run
```

---

### Option 2 : Si le backend tourne en arrière-plan

#### Trouver le processus :

```bash
netstat -ano | findstr :8080
```

Vous verrez quelque chose comme :

```
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    31184
```

Le nombre à la fin (31184) est le PID.

#### Tuer le processus :

```powershell
taskkill /PID 31184 /F
```

#### Relancer le backend :

```bash
cd medverify-backend
mvn spring-boot:run
```

---

### Option 3 : Redémarrer via IntelliJ IDEA / Eclipse

Si vous utilisez un IDE :

1. Cliquer sur le bouton Stop (carré rouge)
2. Cliquer sur le bouton Run (triangle vert)

---

## 🧪 Vérifier que le backend a redémarré

Dans les logs, vous devriez voir :

```
Started MedverifyBackendApplication in X.XXX seconds
```

---

## 📱 Tester l'app mobile

Une fois le backend redémarré :

1. **Sur votre téléphone**, ouvrir l'app Expo Go
2. **Scanner le QR code** (ou recharger si déjà ouvert)
3. **Essayer de vous connecter** avec vos identifiants

---

## 🎯 Test backend depuis le navigateur

Pour confirmer que le backend fonctionne :

```
http://192.168.1.16:8080/swagger-ui.html
```

Vous devriez voir la documentation Swagger.

---

## ⚡ Commande rapide (tout en un)

```powershell
# Tuer le processus sur le port 8080
$processId = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess
if ($processId) { Stop-Process -Id $processId -Force }

# Attendre 2 secondes
Start-Sleep -Seconds 2

# Relancer le backend
cd medverify-backend
mvn spring-boot:run
```

---

**Une fois le backend redémarré, essayez de vous connecter dans l'app mobile ! 🚀**
