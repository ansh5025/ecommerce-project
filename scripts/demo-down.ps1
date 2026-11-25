# Stops the ecommerce demo: the API, then MySQL. Only MySQL storage is billed while stopped.
# Usage: .\scripts\demo-down.ps1
param([string]$ResourceGroup = 'ecommerce')
. "$PSScriptRoot\common.ps1"

Assert-SignedIn
Write-Host 'Stopping the ecommerce demo'
Invoke-AppAction $ResourceGroup 'stop'
Stop-MySql $ResourceGroup
Write-Host "`nStopped. Run .\scripts\demo-up.ps1 next time." -ForegroundColor Green
