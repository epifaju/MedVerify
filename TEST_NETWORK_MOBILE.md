# 🔍 Test de Connectivité Mobile

## Problème
L'application mobile ne peut pas se connecter au backend malgré:
- ✅ Backend qui répond depuis le PC
- ✅ Configuration correcte
- ✅ Pare-feu configuré

## Tests à Effectuer

### 1. Tester depuis le Navigateur Mobile
Sur votre téléphone Android (RMX3201):

1. Ouvrez Chrome ou navigateur
2. Allez sur: `http://192.168.1.16:8080/actuator/health`
3. Si ça fonctionne → Réponse: `{"status":"UP"}`
4. Si erreur → Problème réseau

### 2. Vérifier le WiFi

Assurez-vous que:
- Le téléphone est connecté au **même WiFi** que le PC
- Pas de WiFi invité isolé
- Pas de "AP Isolation" activé sur le routeur

### 3. Tester avec adb reverse (Alternative)

Si vous utilisez un émulateur Android, exécutez:
```bash
adb reverse tcp:8080 tcp:8080
```

Puis dans l'app, utilisez `localhost` au lieu de l'IP.

### 4. Vérifier l'IP

Sur votre PC:
```bash
ipconfig | Select-String "IPv4"
```
Note: Si l'IP a changé, mettre à jour `constants.ts`

Sur votre téléphone:
- Paramètres → WiFi → Appuyez sur votre réseau
- Notez l'adresse IP
- Doit être sur le même réseau (192.168.1.x)

### 5. Tester avec Postman (sur mobile)

Installer Postman mobile et tester:
```
POST http://192.168.1.16:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "epifaju@admin.com",
  "password": "votre_mot_de_passe"
}
```

## Solutions Possibles

### A. WiFi Isolé
Si le WiFi a l'isolation activée:
- Désactiver "AP Isolation" sur le routeur
- OU utiliser un hotspot USB Windows
- OU utiliser un émulateur

### B. Firewall Antivirus
Certains antivirus bloquent les connexions:
- Ajouter une exception dans l'antivirus
- Désactiver temporairement pour tester

### C. Utiliser Hotspot Windows
1. Paramètres Windows → Réseau → Hotspot mobile
2. Activer le hotspot
3. Connecter le téléphone au hotspot Windows
4. Utiliser l'IP du hotspot (généralement 192.168.137.1)

### D. Utiliser USB Debugging
Si développement avec émulateur:
```bash
adb reverse tcp:8080 tcp:8080
```
Puis utiliser `localhost` dans l'app.

## Diagnostic Rapide

Faites ce test sur le téléphone dans le navigateur:
```
http://192.168.1.16:8080/swagger-ui.html
```

Si ça charge → L'API est accessible  
Si erreur → Problème réseau/WiFi

## Prochaine Étape

Envoyez-moi le résultat du test navigateur mobile!

