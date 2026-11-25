# Starts the ecommerce demo before an interview: MySQL first (3-5 minutes), then the API.
# Usage: .\scripts\demo-up.ps1
param([string]$ResourceGroup = 'ecommerce')
. "$PSScriptRoot\common.ps1"

Assert-SignedIn
Write-Host 'Starting the ecommerce demo'
Start-MySql $ResourceGroup
Invoke-AppAction $ResourceGroup 'start'
$url = Get-PublicUrl $ResourceGroup
if (Wait-ApiUp $url) {
    Write-Host "`nReady: https://ecommerce.anshrai.in (API $url)" -ForegroundColor Green
} else {
    Write-Warning "Started, but $url isn't answering yet. Logs: az containerapp logs show -g $ResourceGroup -n $AppName"
}
Write-Host 'Afterwards run .\scripts\demo-down.ps1 (a nightly job also stops everything at 01:00 IST).'
