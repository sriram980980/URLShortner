# URL Shortener - Microservices Architecture

A comprehensive microservices-based URL shortening platform built with Spring Cloud, featuring distributed tracing, centralized logging, caching, and messaging. This monorepo contains all services, infrastructure configurations, and supporting components.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Service Details](#service-details)
- [Local Development](#local-development)
- [API Reference](#api-reference)
- [Monitoring and Observability](#monitoring-and-observability)
- [Configuration Management](#configuration-management)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## Architecture Overview

The URL Shortener platform follows a microservices architecture pattern with the following key components:

### Core Services

| Service | Port | Purpose |
|---------|------|---------|
| **API Gateway** | 8080 | Entry point for all client requests, routing, and authentication |
| **URL Service** | 8081 | Core URL shortening, expansion, and retrieval logic |
| **Analytics Service** | 8082 | Event processing and analytics data collection |
| **Config Server** | 8888 | Centralized configuration management |
| **Discovery Server (Eureka)** | 8761 | Service registration and discovery |
| **Frontend** | 80 | Angular-based web application UI |

### Infrastructure Services

| Service | Port | Purpose |
|---------|------|---------|
| **Vault** | 8200 | Secrets management and encryption |
| **Redis** | 6379 | In-memory caching with hybrid persistence |
| **Kafka** | 9092 | Event streaming and asynchronous messaging |
| **Zookeeper** | 2181 | Kafka coordinator and configuration management |
| **Elasticsearch** | 9200 | Distributed search and log storage |
| **Logstash** | 5000, 5044 | Log aggregation and processing |
| **Kibana** | 5601 | Log analysis and visualization |
| **Zipkin** | 9411 | Distributed tracing and span visualization |

### Startup Order

Services start in the following dependency order:

1. **Vault** - Secrets management (independent)
2. **Config Server** - Waits for Vault
3. **Discovery Server (Eureka)** - Waits for Config Server
4. **Parallel Start**:
   - URL Service
   - Analytics Service
   - API Gateway
   All wait for Discovery Server
5. **Frontend** - Waits for API Gateway
6. **Infrastructure** (background):
   - Redis, Kafka, Zookeeper, Elasticsearch, Logstash, Kibana, Zipkin

---

## Prerequisites

### System Requirements
- Docker Desktop 4.10+ (includes Docker Engine and Docker Compose)

### Development Tools
- **Java**: JDK 21 or later
- **Maven**: 3.9.0 or later
- **Node.js**: 20.x LTS
- **Angular CLI**: 17.x
- **Git**: 2.30 or later

### Quick Install

**Windows (via Chocolatey)**:
```bash
choco install openjdk21 maven nodejs docker-desktop
npm install -g @angular/cli@17
```

**macOS (via Homebrew)**:
```bash
brew install openjdk@21 maven node docker
npm install -g @angular/cli@17
```

**Linux (Ubuntu/Debian)**:
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven nodejs docker.io
npm install -g @angular/cli@17
```

---

## Quick Start

### 1. Start Infrastructure

```bash
# Clone and enter project
git clone https://github.com/your-org/url-shortener.git
cd URLShortner

# Start all Docker services
docker compose up -d

# Wait 30-60 seconds for services to initialize
docker compose ps

# View logs (optional)
docker compose logs -f
```

### 2. Build Spring Services

```bash
# Build all Spring modules
mvn clean install -pl config-server,discovery-server,url-service,analytics-service,api-gateway

# Or build specific module
mvn clean install -pl url-service
```

### 3. Run Spring Services

In separate terminals:

```bash
# Terminal 1 - Config Server
cd config-server && mvn spring-boot:run

# Terminal 2 - Discovery Server
cd discovery-server && mvn spring-boot:run

# Terminal 3 - URL Service
cd url-service && mvn spring-boot:run

# Terminal 4 - Analytics Service
cd analytics-service && mvn spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway && mvn spring-boot:run
```

### 4. Start Frontend

```bash
# Terminal 6 - Frontend
cd frontend
npm install
ng serve --open
```

### 5. Access Services

| Service | URL |
|---------|-----|
| Frontend (dev) | http://localhost:4200 |
| Frontend (prod) | http://localhost:80 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| Kibana Logs | http://localhost:5601 |
| Zipkin Traces | http://localhost:9411 |
| Vault | http://localhost:8200 (Token: dev-root-token) |

---

## Service Details

### API Gateway (Port 8080)
**Purpose**: Single entry point with routing, authentication, and rate limiting.
- Request routing to microservices
- JWT authentication/authorization
- Rate limiting and circuit breakers
- CORS handling
- Request/response logging

**Health Check**: `curl http://localhost:8080/actuator/health`

### URL Service (Port 8081)
**Purpose**: Core business logic for URL shortening.
- Generate unique short codes (Base62)
- Store URL mappings
- Cache frequently accessed URLs in Redis
- Publish events to Kafka
- Redirect to original URLs

**Health Check**: `curl http://localhost:8081/actuator/health`

### Analytics Service (Port 8082)
**Purpose**: Process events and compute analytics.
- Consume URL access events from Kafka
- Store analytics data in Elasticsearch
- Compute statistics (clicks, geography, referrers)
- Provide analytics endpoints

**Health Check**: `curl http://localhost:8082/actuator/health`

### Config Server (Port 8888)
**Purpose**: Centralized configuration management.
- Serve application properties
- Support multiple profiles (dev, test, prod)
- Integrate with Vault for secrets

**Health Check**: `curl http://localhost:8888/actuator/health`

### Discovery Server - Eureka (Port 8761)
**Purpose**: Service registration and discovery.
- Auto-register microservices
- Track service availability
- Client-side load balancing
- Health checks

**Dashboard**: http://localhost:8761

---

## Local Development

### Option 1: Full Docker Compose (Recommended)

```bash
docker compose up -d
# All services run in containers
# View logs: docker compose logs -f
# Stop: docker compose down
```

### Option 2: Local Spring Services + Docker Infrastructure

```bash
# Start only infrastructure
docker compose up -d redis kafka zookeeper elasticsearch logstash kibana zipkin vault

# Run Spring services locally (see Quick Start section above)
```

### Common Development Tasks

```bash
# Run tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View logs
docker compose logs -f url-service

# Connect to Redis
docker exec -it url-shortner-redis redis-cli

# List Kafka topics
docker exec url-shortner-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# Check Elasticsearch health
curl -X GET http://localhost:9200/_cluster/health
```

---

## API Reference

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
All endpoints (except health) require JWT token:
```
Authorization: Bearer {jwt_token}
```

### Core Endpoints

#### Shorten URL
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "originalUrl": "https://www.example.com/very/long/url",
    "customCode": "custom",
    "expiresAt": "2024-12-31T23:59:59Z",
    "tags": ["important"]
  }'
```

**Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "originalUrl": "https://www.example.com/very/long/url",
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "createdAt": "2024-01-20T10:30:00Z",
  "expiresAt": "2024-12-31T23:59:59Z"
}
```

#### Redirect to Original URL
```bash
curl -L http://localhost:8080/abc123
```
Returns 301/302 redirect to original URL.

#### Get URL Details
```bash
curl -X GET http://localhost:8080/api/v1/urls/abc123 \
  -H "Authorization: Bearer {token}"
```

#### Get URL Analytics
```bash
curl -X GET http://localhost:8080/api/v1/analytics/abc123 \
  -H "Authorization: Bearer {token}"
```

**Response**:
```json
{
  "shortCode": "abc123",
  "totalClicks": 1523,
  "uniqueClicks": 892,
  "lastAccessTime": "2024-01-20T14:25:00Z",
  "geography": {
    "US": 650,
    "UK": 120,
    "DE": 45
  }
}
```

#### List User URLs
```bash
curl -X GET "http://localhost:8080/api/v1/urls?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

#### Update URL
```bash
curl -X PUT http://localhost:8080/api/v1/urls/abc123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "expiresAt": "2025-12-31T23:59:59Z",
    "tags": ["updated"]
  }'
```

#### Delete URL
```bash
curl -X DELETE http://localhost:8080/api/v1/urls/abc123 \
  -H "Authorization: Bearer {token}"
```

---

## Monitoring and Observability

### Logging - ELK Stack
- **Kibana**: http://localhost:5601
- **Index Pattern**: `url-shortner-logs-*`
- **View Logs**: Dashboard -> Discover -> Select index pattern

### Distributed Tracing - Zipkin
- **URL**: http://localhost:9411
- **View Traces**: Click "Find Traces"
- **Filter**: Select service from dropdown

### Metrics - Prometheus
- **Endpoint**: http://localhost:8080/actuator/prometheus
- **Key Metrics**:
  - `http_requests_total` - Total HTTP requests
  - `http_request_duration_seconds` - Request duration
  - `redis_commands_total` - Redis operations
  - `jvm_memory_used_bytes` - JVM memory

### Health Checks
```bash
# Check service health
curl http://localhost:8080/actuator/health

# Check Redis
docker exec url-shortner-redis redis-cli ping

# Check Kafka
docker exec url-shortner-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Check Elasticsearch
curl -X GET http://localhost:9200/_cluster/health
```

---

## Configuration Management

### Environment Variables

Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

Key variables:
```
REDIS_HOST=localhost
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
ELASTICSEARCH_URIS=http://localhost:9200
VAULT_TOKEN=dev-root-token
BASE_URL=http://localhost:8080
```

### Application Profiles

Run with specific profile:
```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Available profiles:
- `dev` - Development (debug logging, H2 DB)
- `test` - Testing (in-memory databases)
- `prod` - Production (optimized, Vault enabled)

### Vault Integration

Secrets in development mode are auto-initialized. For production, configure:
```yaml
vault:
  enabled: true
  host: vault.example.com
  port: 8200
  token: ${VAULT_TOKEN}
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find and kill process
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9

# Or change port in docker-compose.yml
```

### Service Connection Errors
```bash
# Check service status
docker compose ps

# View service logs
docker compose logs SERVICE_NAME

# Restart service
docker compose restart SERVICE_NAME

# Check Docker network
docker network inspect url-shortner-net
```

### Redis Issues
```bash
# Check Redis status
docker exec url-shortner-redis redis-cli ping

# Clear Redis cache (development only)
docker exec url-shortner-redis redis-cli FLUSHDB

# View Redis logs
docker logs url-shortner-redis
```

### Kafka Issues
```bash
# Check Kafka status
docker exec url-shortner-kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# List topics
docker exec url-shortner-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# Restart Kafka
docker restart url-shortner-kafka url-shortner-zookeeper
```

### High Memory Usage
```bash
# Check Docker stats
docker stats

# Reduce heap size in docker-compose.yml
JAVA_OPTS: -Xmx256m -Xms128m

# Clean up Docker
docker system prune -a
```

### Build Failures
```bash
# Clean and rebuild
mvn clean install

# Check Maven version
mvn -v

# Update Maven dependencies
mvn dependency:update-snapshots
```

---

## Contributing

### Code Standards
- Follow Spring Boot best practices
- Use meaningful names
- Add Javadoc for public APIs
- Write unit tests (>80% coverage)
- Run `mvn clean install` before committing

### Pull Request Process
1. Create feature branch: `git checkout -b feature/description`
2. Make changes and commit: `git commit -am "Description"`
3. Push: `git push origin feature/description`
4. Create Pull Request on GitHub
5. Wait for CI/CD pipeline
6. Request code review
7. Merge after approval

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=UrlServiceTest

# Generate coverage report
mvn test jacoco:report
```


---

**Last Updated**: August 2024
**Version**: 1.0.0
