# Script de configuration du pare-feu pour MedVerify Backend
# Exécuter en tant qu'administrateur

Write-Host "Configuration du pare-feu Windows pour MedVerify..." -ForegroundColor Cyan

# Vérifier si le script est exécuté en tant qu'administrateur
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "ERREUR : Ce script doit être exécuté en tant qu'administrateur !" -ForegroundColor Red
    Write-Host "Faites un clic droit sur le fichier et sélectionnez 'Exécuter en tant qu'administrateur'" -ForegroundColor Yellow
    pause
    exit 1
}

# Ajouter la règle pour le port 8080
Write-Host "Ajout de la règle pour le port 8080..." -ForegroundColor Yellow

try {
    netsh advfirewall firewall add rule name="MedVerify Backend Port 8080" dir=in action=allow protocol=TCP localport=8080
    Write-Host "✓ Règle ajoutée avec succès !" -ForegroundColor Green
}
catch {
    Write-Host "✗ Erreur lors de l'ajout de la règle" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Vérifier que la règle a été ajoutée
Write-Host "`nVérification de la règle..." -ForegroundColor Yellow
netsh advfirewall firewall show rule name="MedVerify Backend Port 8080"

Write-Host "`n✓ Configuration terminée !" -ForegroundColor Green
Write-Host "Le port 8080 est maintenant accessible depuis votre réseau local." -ForegroundColor Cyan
Write-Host "`nTestez maintenant la connexion depuis votre téléphone." -ForegroundColor Yellow

pause




