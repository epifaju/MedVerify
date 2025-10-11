# Script pour suivre les logs du backend en temps réel

Write-Host "===== SUIVI DES LOGS BACKEND MEDVERIFY =====" -ForegroundColor Cyan
Write-Host "Appuyez sur Ctrl+C pour arrêter" -ForegroundColor Yellow
Write-Host ""

Get-Content -Path "medverify-backend\logs\medverify.log" -Tail 10 -Wait

