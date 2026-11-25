// Ecommerce API on Azure Container Apps, started only for demos (scripts/demo-up.ps1) and stopped otherwise.
// The existing MySQL flexible server stays; a nightly job stops the API and MySQL in case either was left running
// (Azure also restarts a MySQL server automatically after 30 days stopped).

param location string = resourceGroup().location
param prefix string = 'ecommerce'

@description('Public image, e.g. ghcr.io/ansh5025/ecommerce-api:<commit sha>.')
param image string

@description('Name of the existing MySQL flexible server in this resource group.')
param mysqlServerName string = 'ansh-ecommerce'

@secure()
param dbUrl string
param dbUsername string
@secure()
param dbPassword string

param corsAllowedOrigins string = 'https://ecommerce.anshrai.in'
param sessionCookieSameSite string = 'none'
param sessionCookieSecure string = 'true'

@description('Principal of the GitHub Actions identity that rolls out new images.')
param deployerPrincipalId string = ''

@description('UTC cron for the nightly stop. Default is 01:00 IST.')
param autoStopCron string = '30 19 * * *'

param customDomain string = ''
param customDomainCertificateId string = ''

var appName = '${prefix}-api'

resource mysql 'Microsoft.DBforMySQL/flexibleServers@2023-12-30' existing = {
  name: mysqlServerName
}

resource logs 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${prefix}-logs'
  location: location
  properties: {
    sku: { name: 'PerGB2018' }
    retentionInDays: 30
  }
}

// Pinned to workload-profiles mode: newer subscriptions otherwise get the restricted 'Express' mode.
resource env 'Microsoft.App/managedEnvironments@2026-07-01' = {
  name: '${prefix}-env'
  location: location
  properties: {
    environmentMode: 'WorkloadProfiles'
    workloadProfiles: [
      { name: 'Consumption', workloadProfileType: 'Consumption' }
    ]
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logs.properties.customerId
        sharedKey: logs.listKeys().primarySharedKey
      }
    }
  }
}

resource app 'Microsoft.App/containerApps@2026-07-01' = {
  name: appName
  location: location
  properties: {
    environmentId: env.id
    workloadProfileName: 'Consumption'
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: {
        external: true
        targetPort: 8080
        transport: 'http'
        allowInsecure: false
        customDomains: empty(customDomainCertificateId) ? [] : [
          { name: customDomain, certificateId: customDomainCertificateId, bindingType: 'SniEnabled' }
        ]
      }
      secrets: [
        { name: 'db-url', value: dbUrl }
        { name: 'db-password', value: dbPassword }
      ]
    }
    template: {
      containers: [
        {
          name: 'api'
          image: image
          resources: { cpu: json('0.5'), memory: '1Gi' }
          env: [
            { name: 'DB_URL', secretRef: 'db-url' }
            { name: 'DB_USERNAME', value: dbUsername }
            { name: 'DB_PASSWORD', secretRef: 'db-password' }
            { name: 'CORS_ALLOWED_ORIGINS', value: corsAllowedOrigins }
            { name: 'SERVER_SERVLET_SESSION_COOKIE_SAME_SITE', value: sessionCookieSameSite }
            { name: 'SERVER_SERVLET_SESSION_COOKIE_SECURE', value: sessionCookieSecure }
            { name: 'SERVER_FORWARD_HEADERS_STRATEGY', value: 'native' }
          ]
          probes: [
            {
              // No actuator in this app, so check that the port is accepting connections.
              type: 'Startup'
              tcpSocket: { port: 8080 }
              initialDelaySeconds: 15
              periodSeconds: 10
              failureThreshold: 30
            }
          ]
        }
      ]
      // Sessions live in memory, so one replica keeps users signed in.
      scale: { minReplicas: 1, maxReplicas: 1 }
    }
  }
}

resource autoStop 'Microsoft.App/jobs@2026-07-01' = {
  name: '${prefix}-autostop'
  location: location
  identity: { type: 'SystemAssigned' }
  properties: {
    environmentId: env.id
    workloadProfileName: 'Consumption'
    configuration: {
      triggerType: 'Schedule'
      replicaTimeout: 900
      replicaRetryLimit: 1
      scheduleTriggerConfig: {
        cronExpression: autoStopCron
        parallelism: 1
        replicaCompletionCount: 1
      }
    }
    template: {
      containers: [
        {
          name: 'stop'
          image: 'mcr.microsoft.com/azure-cli:latest'
          resources: { cpu: json('0.25'), memory: '0.5Gi' }
          command: ['/bin/bash', '-c']
          args: [
            'az login --identity --output none && v=$(az provider show -n Microsoft.App --query "resourceTypes[?resourceType==\'containerApps\'].apiVersions[0]" -o tsv) && (az rest --method post --url "\${ARM_URL%/}$APP_ID/stop?api-version=$v" || true); state=$(az mysql flexible-server show --ids "$MYSQL_ID" --query state -o tsv); if [ "$state" != "Stopped" ]; then az mysql flexible-server stop --ids "$MYSQL_ID" || true; fi'
          ]
          env: [
            { name: 'ARM_URL', value: environment().resourceManager }
            { name: 'APP_ID', value: app.id }
            { name: 'MYSQL_ID', value: mysql.id }
          ]
        }
      ]
    }
  }
}

var contributorRoleId = 'b24988ac-6180-42a0-ab88-20f7382dd24c'

resource autoStopRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(resourceGroup().id, '${prefix}-autostop', contributorRoleId)
  properties: {
    principalId: autoStop.identity.principalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', contributorRoleId)
  }
}

// Lets GitHub Actions (OIDC) roll out new images to this app only.
resource deployerRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = if (!empty(deployerPrincipalId)) {
  name: guid(app.id, deployerPrincipalId, contributorRoleId)
  scope: app
  properties: {
    principalId: deployerPrincipalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', contributorRoleId)
  }
}

output appName string = app.name
output fqdn string = app.properties.configuration.ingress.fqdn
output verificationId string = app.properties.customDomainVerificationId
output environmentId string = env.id
