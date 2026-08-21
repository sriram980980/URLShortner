#!/bin/sh
# Vault Initialization Script
# URL Shortener Microservices - Secret Management Setup
# This script initializes Vault with necessary secrets for development

set -e

# Configuration
VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
VAULT_TOKEN="${VAULT_TOKEN:-dev-root-token}"
MAX_RETRIES=30
RETRY_DELAY=2

echo "=== Vault Initialization Script ==="
echo "VAULT_ADDR: $VAULT_ADDR"
echo "VAULT_TOKEN: $VAULT_TOKEN"

# Wait for Vault to be ready
echo "Waiting for Vault to be ready..."
retry_count=0
while [ $retry_count -lt $MAX_RETRIES ]; do
    if vault status > /dev/null 2>&1; then
        echo "Vault is ready!"
        break
    fi
    retry_count=$((retry_count + 1))
    echo "Attempt $retry_count/$MAX_RETRIES - Waiting for Vault..."
    sleep $RETRY_DELAY
done

if [ $retry_count -eq $MAX_RETRIES ]; then
    echo "ERROR: Vault did not become ready in time"
    exit 1
fi

# Enable KV v2 secrets engine at 'secret' path
echo "Enabling KV v2 secrets engine..."
vault secrets enable -path=secret kv-v2 2>/dev/null || echo "KV v2 already enabled"

# URL Service Configuration
echo "Configuring url-service secrets..."
vault kv put secret/url-service \
    redis.password="" \
    redis.host="redis" \
    redis.port="6379" \
    jwt.secret="url-shortner-jwt-secret-key-2024-development-only" \
    jwt.expiration="86400" \
    database.url="jdbc:mysql://mysql:3306/url_shortener" \
    database.username="root" \
    database.password="password"

# Analytics Service Configuration
echo "Configuring analytics-service secrets..."
vault kv put secret/analytics-service \
    elasticsearch.host="elasticsearch" \
    elasticsearch.port="9200" \
    elasticsearch.username="" \
    elasticsearch.password="" \
    kafka.bootstrap.servers="kafka:9092" \
    kafka.consumer.group="analytics-service-group"

# API Gateway Configuration
echo "Configuring api-gateway secrets..."
vault kv put secret/api-gateway \
    jwt.secret="url-shortner-jwt-secret-key-2024-development-only" \
    jwt.expiration="86400" \
    cors.allowed.origins="http://localhost,http://localhost:4200,http://localhost:80" \
    rate.limit.enabled="true" \
    rate.limit.requests="1000" \
    rate.limit.window.seconds="60"

# Config Server Configuration
echo "Configuring config-server secrets..."
vault kv put secret/config-server \
    git.uri="https://github.com/your-org/url-shortener-config.git" \
    git.branch="main" \
    git.username="" \
    git.password=""

# Common Application Secrets
echo "Configuring common application secrets..."
vault kv put secret/application \
    app.name="URL Shortener" \
    app.version="1.0.0" \
    logging.level="INFO" \
    actuator.enabled="true" \
    zipkin.url="http://zipkin:9411" \
    zipkin.enabled="true"

# Enable audit logging
echo "Enabling audit logging..."
vault audit enable file file_path=/vault/logs/audit.log 2>/dev/null || echo "File audit already enabled"

# List all secrets to verify
echo ""
echo "=== Vault Secrets Verification ==="
echo "Available secret paths:"
vault kv list secret/ 2>/dev/null || echo "Could not list secrets"

echo ""
echo "=== Vault Initialization Complete ==="
echo "Development Vault setup finished successfully!"
echo "Root Token: $VAULT_TOKEN"
echo "Address: $VAULT_ADDR"
echo ""
echo "NOTE: This script uses dev mode tokens. DO NOT use in production!"
