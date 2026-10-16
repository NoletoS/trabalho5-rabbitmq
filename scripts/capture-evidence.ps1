param(
    [string]$ManagementUrl = 'http://localhost:15672',
    [string]$User = $(if ($env:RABBITMQ_USER) { $env:RABBITMQ_USER } else { 'demo' }),
    [string]$Password = $(if ($env:RABBITMQ_PASS) { $env:RABBITMQ_PASS } else { 'demo123' })
)

$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$folder = Join-Path $project 'evidencias'
New-Item -ItemType Directory -Path $folder -Force | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$pair = '{0}:{1}' -f $User, $Password
$authorization = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{ Authorization = 'Basic ' + $authorization }

$queues = Invoke-RestMethod -Uri "$ManagementUrl/api/queues/%2F" -Headers $headers
$summary = $queues | Select-Object name, messages, messages_ready, messages_unacknowledged, consumers
$summary | Format-Table -AutoSize | Out-String | Set-Content -LiteralPath (Join-Path $folder "filas-$stamp.txt") -Encoding utf8
$summary | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $folder "filas-$stamp.json") -Encoding utf8

Push-Location $project
try {
    docker compose ps | Out-File -LiteralPath (Join-Path $folder "containers-$stamp.txt") -Encoding utf8
    docker compose logs --no-color --tail=100 generator review-consumer support-consumer |
        Out-File -LiteralPath (Join-Path $folder "logs-$stamp.txt") -Encoding utf8
} finally {
    Pop-Location
}
Write-Host "Evidências textuais salvas em $folder"
Write-Host 'Faça também uma captura da aba Queues na interface do RabbitMQ e salve nesta pasta.'

