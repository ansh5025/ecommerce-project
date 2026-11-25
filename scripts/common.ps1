# Shared helpers for the ecommerce Azure scripts. Dot-source this file; don't run it directly.
$ErrorActionPreference = 'Stop'
$ResourceGroupDefault = 'ecommerce'
$AppName = 'ecommerce-api'
$MySqlServer = 'ansh-ecommerce'

function Assert-Az([string]$What) { if ($LASTEXITCODE -ne 0) { throw "$What failed (exit code $LASTEXITCODE)." } }

# Windows PowerShell 5.1 turns stderr from native commands into errors under 'Stop'; callers check $LASTEXITCODE.
function Invoke-Quiet([scriptblock]$Command) {
    $saved = $ErrorActionPreference; $ErrorActionPreference = 'Continue'
    try { & $Command 2>$null } finally { $ErrorActionPreference = $saved }
}

# Windows PowerShell 5.1 emits a JSON array as one object; ForEach-Object unrolls it into items.
function From-Json($text) { ($text | Out-String) | ConvertFrom-Json | ForEach-Object { $_ } }

function Assert-SignedIn {
    Invoke-Quiet { az account show --output none } | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Not signed in to Azure. Run 'az login' first." }
}

function Start-MySql([string]$ResourceGroup) {
    $state = (az mysql flexible-server show --resource-group $ResourceGroup --name $MySqlServer --query state --output tsv).Trim()
    Assert-Az 'Reading the MySQL server state'
    if ($state -ne 'Ready') {
        Write-Host "  Starting MySQL $MySqlServer (usually 3-5 minutes)..."
        az mysql flexible-server start --resource-group $ResourceGroup --name $MySqlServer --output none
        Assert-Az 'Starting MySQL'
    }
}

function Stop-MySql([string]$ResourceGroup) {
    $state = (az mysql flexible-server show --resource-group $ResourceGroup --name $MySqlServer --query state --output tsv).Trim()
    Assert-Az 'Reading the MySQL server state'
    if ($state -eq 'Ready') {
        Write-Host "  Stopping MySQL $MySqlServer..."
        az mysql flexible-server stop --resource-group $ResourceGroup --name $MySqlServer --output none
        Assert-Az 'Stopping MySQL'
    }
}

# The CLI has no start/stop for container apps, so call the management API directly.
function Invoke-AppAction([string]$ResourceGroup, [ValidateSet('start', 'stop')][string]$Action) {
    $id = (az containerapp show --resource-group $ResourceGroup --name $AppName --query id --output tsv).Trim()
    Assert-Az "Finding $AppName"
    $arm = (az cloud show --query endpoints.resourceManager --output tsv).Trim().TrimEnd('/')
    $version = (az provider show --namespace Microsoft.App --query "resourceTypes[?resourceType=='containerApps'].apiVersions[0]" --output tsv).Trim()
    Write-Host "  $Action $AppName"
    az rest --method post --url "$arm$id/$($Action)?api-version=$version" --output none
    Assert-Az "$Action $AppName"
}

function Get-PublicUrl([string]$ResourceGroup) {
    $binding = From-Json (Invoke-Quiet { az containerapp hostname list --resource-group $ResourceGroup --name $AppName --query "[?certificateId!=null] | [0].name" --output json })
    if ($binding) { return "https://$binding" }
    return "https://$((az containerapp show --resource-group $ResourceGroup --name $AppName --query properties.configuration.ingress.fqdn --output tsv).Trim())"
}

# The API has no health endpoint; any answer below 500 means Spring Boot is up (protected routes return 401/403).
function Wait-ApiUp([string]$Url, [int]$TimeoutSeconds = 420) {
    Write-Host "  Waiting for $Url to answer..."
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try { Invoke-WebRequest "$Url/api/auth/logout" -Method Post -UseBasicParsing -TimeoutSec 10 | Out-Null; return $true } catch {
            $status = [int]$_.Exception.Response.StatusCode
            if ($status -gt 0 -and $status -lt 500) { return $true }
        }
        Start-Sleep -Seconds 10
    }
    return $false
}
