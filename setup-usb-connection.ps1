# Script de configuration de la connexion USB pour MedVerify
# À exécuter avant de lancer l'application mobile avec un téléphone connecté via USB

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Configuration USB - MedVerify" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que ADB est disponible
$adbCheck = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbCheck) {
    Write-Host "❌ ADB n'est pas trouvé dans le PATH" -ForegroundColor Red
    Write-Host "Veuillez installer Android SDK Platform Tools" -ForegroundColor Yellow
    exit 1
}

# Vérifier que le téléphone est connecté
Write-Host "Vérification de la connexion USB..." -ForegroundColor Yellow
$devices = adb devices 2>&1
$deviceCount = ($devices | Select-String "device$").Count

if ($deviceCount -eq 0) {
    Write-Host "❌ Aucun appareil Android connecté via USB" -ForegroundColor Red
    Write-Host ""
    Write-Host "Actions à effectuer :" -ForegroundColor Yellow
    Write-Host "1. Connectez votre téléphone via USB" -ForegroundColor White
    Write-Host "2. Activez le débogage USB sur le téléphone" -ForegroundColor White
    Write-Host "3. Autorisez le débogage USB (popup sur le téléphone)" -ForegroundColor White
    Write-Host "4. Réexécutez ce script" -ForegroundColor White
    exit 1
}

Write-Host "✅ $deviceCount appareil(s) détecté(s)" -ForegroundColor Green
Write-Host ""

# Afficher les appareils connectés
$devices | Where-Object { $_ -match "device$" } | ForEach-Object {
    Write-Host "  📱 $_" -ForegroundColor Cyan
}

Write-Host ""

# Supprimer les tunnels existants pour éviter les conflits
Write-Host "Nettoyage des tunnels existants..." -ForegroundColor Yellow
adb reverse --remove-all 2>$null

# Configurer le tunnel pour le backend
Write-Host "Configuration du tunnel pour le port 8080..." -ForegroundColor Yellow
$result = adb reverse tcp:8080 tcp:8080 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tunnel USB configuré : localhost:8080 → PC:8080" -ForegroundColor Green
}
else {
    Write-Host "❌ Erreur lors de la configuration du tunnel" -ForegroundColor Red
    Write-Host $result -ForegroundColor Red
    exit 1
}

Write-Host ""

# Vérifier les tunnels actifs
Write-Host "Tunnels actifs :" -ForegroundColor Yellow
adb reverse --list | ForEach-Object {
    Write-Host "  ✅ $_" -ForegroundColor Green
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  ✅ Configuration terminée !" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Vous pouvez maintenant lancer l'application mobile." -ForegroundColor Yellow
Write-Host "L'URL de l'API est configurée pour utiliser : http://localhost:8080/api/v1" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  Important : Réexécutez ce script si vous :" -ForegroundColor Yellow
Write-Host "   - Redéconnectez le téléphone" -ForegroundColor White
Write-Host "   - Redémarrez ADB" -ForegroundColor White
Write-Host "   - Redémarrez le téléphone" -ForegroundColor White
Write-Host ""





