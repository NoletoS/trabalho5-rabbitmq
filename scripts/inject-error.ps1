param(
    [string]$ManagementUrl = 'http://localhost:15672',
    [string]$User = $(if ($env:RABBITMQ_USER) { $env:RABBITMQ_USER } else { 'demo' }),
    [string]$Password = $(if ($env:RABBITMQ_PASS) { $env:RABBITMQ_PASS } else { 'demo123' })
)

$ErrorActionPreference = 'Stop'
$pair = '{0}:{1}' -f $User, $Password
$authorization = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{ Authorization = 'Basic ' + $authorization }
$body = @{
    properties = @{ delivery_mode = 2; content_type = 'application/json' }
    routing_key = 'review.product'
    payload = '{ JSON inválido para demonstrar a DLQ'
    payload_encoding = 'string'
} | ConvertTo-Json -Depth 4

$uri = "$ManagementUrl/api/exchanges/%2F/messages.topic/publish"
$result = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -ContentType 'application/json' -Body $body
if (-not $result.routed) {
    throw 'A mensagem de erro não foi roteada para reviews.queue.'
}
Write-Host 'Mensagem inválida publicada na fila de avaliações.'
Write-Host 'Aguarde o consumidor processá-la; depois confira messages.dlq na interface.'

