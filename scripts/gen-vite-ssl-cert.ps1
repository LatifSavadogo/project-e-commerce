# Génère certs/vite-dev-key.pem + certs/vite-dev.pem avec SAN = localhost + 127.0.0.1 + votre IP LAN.
# Nécessite OpenSSL (ex. Git for Windows : C:\Program Files\Git\usr\bin\openssl.exe).
# Usage :
#   .\scripts\gen-vite-ssl-cert.ps1
#   .\scripts\gen-vite-ssl-cert.ps1 -LanIp 192.168.11.106

param(
  [string] $LanIp = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$certs = Join-Path $root "certs"
New-Item -ItemType Directory -Force -Path $certs | Out-Null

if (-not $LanIp) {
  try {
    $LanIp = @(
      Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
      Where-Object { $_.IPAddress -match '^(192\.168\.|10\.)' } |
      Select-Object -ExpandProperty IPAddress -First 1
    )
  } catch { }
}
if (-not $LanIp) {
  Write-Host "Impossible de deviner l'IP LAN. Passez -LanIp 192.168.x.x"
  exit 1
}

$keyOut = Join-Path $certs "vite-dev-key.pem"
$certOut = Join-Path $certs "vite-dev.pem"
$cnf = Join-Path $certs "_openssl-dev.cnf"

@"
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = localhost

[v3_req]
subjectAltName = @alt_names
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
IP.2 = $LanIp
"@ | Set-Content -Encoding ascii -Path $cnf

$openssl = Get-Command openssl -ErrorAction SilentlyContinue
if (-not $openssl) {
  Write-Host "openssl introuvable. Installez Git for Windows ou ajoutez OpenSSL au PATH."
  Remove-Item $cnf -ErrorAction SilentlyContinue
  exit 1
}

& openssl req -x509 -newkey rsa:2048 -sha256 -days 365 -nodes `
  -keyout $keyOut -out $certOut -config $cnf -extensions v3_req

Remove-Item $cnf -ErrorAction SilentlyContinue
Write-Host "OK — certificat pour https://$LanIp`:5173 (et localhost). Relancez : npm run dev:https"
Write-Host "Sur le téléphone : accepter l’avertissement « non sécurisé » (certificat non reconnu)."
